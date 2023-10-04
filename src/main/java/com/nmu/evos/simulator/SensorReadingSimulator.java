package com.nmu.evos.simulator;

import javafx.util.Pair;

import java.util.*;
import java.io.File;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;


public class SensorReadingSimulator {
    private double sensorAngle, obstacleDangerDistance, robotRadius, obstacleRadius;
    
    private BasicNetwork NN;
    private Random rand;
    
    private static double[] angles = {127.8, 74.9, 41.4, 12.6, -12.6, -41.4, -74.9, -127.8, 180};
    private static double[] xCoords = {-.049, -0.063, -0.045, -0.015, 0.015, 0.045, 0.063, 0.049, 0};
    private static double[] yCoords = {-0.038, 0.017, 0.051, 0.067, 0.067, 0.051, 0.017, -0.038, -0.052};


    public SensorReadingSimulator(double sensorAngle, double obstacleDangerDistance, double robotRadius, double obstacleRadius) {
        this.sensorAngle = sensorAngle;
        this.obstacleDangerDistance = obstacleDangerDistance;
        this.robotRadius = robotRadius;
        this.obstacleRadius = obstacleRadius;
        
        rand = new Random();
        File f = new File("NNSingle.eg");
        
        
        NN = ((BasicNetwork)EncogDirectoryPersistence.loadObject(f));
    }

    public double[] getSensorReadings(int robotX, int robotY, double robotA, ArrayList<Point> courseObstacles) {
        int maxDistance = 20;
        int maxAngle = 32;
        int nrSensors = 9;
        

        double[] readings = new double[nrSensors];
        for (int x = 0; x < nrSensors; x++)
            readings[x] = rand.nextInt(40);

        for(Point p : courseObstacles) {

            double distance = distance(new double[]{robotX, robotY}, new double[]{p.x, p.y});
            if ( distance < maxDistance) {

                for (int x = 0; x < nrSensors; x++) {
                    Pair<Double, Double> rotated = rotateXY(xCoords[x], yCoords[x], robotA);
                    double sensorX = rotated.getKey();
                    double sensorY = rotated.getValue();
                    double sensorTheta = formatAngle(angles[x] + robotA);

                    double deltaX = p.x - (robotX + sensorX);
                    double deltaY = p.y - (robotY + sensorY);

                    int quadrant = getQuadrant(deltaX, deltaY);
                    double angle = Math.toDegrees(Math.atan(Math.abs(deltaY) / Math.abs(deltaX)));
                    switch (quadrant) {
                        case 1:
                            angle = 90 - angle;
                            break;
                        case 2:
                            angle = 90 + angle;
                            break;
                        case 3:
                            angle = 270 - angle;
                            break;
                        case 4:
                            angle = 270 + angle;
                            break;
                    }

                    double finalAngle = Math.abs(sensorTheta-angle);
                    if (finalAngle < maxAngle) {
                        double[] input = new double[2];
                        input[0] = NormalizeZeroOne(distance + 5, 0, maxDistance); //800-1100, 1800-2000, 3000-3900, 3000
//                        input[0] = NormalizeZeroOne(distance, 0, maxDistance); //800-1100, 1800-2000, 3000-3900, 3000
                        input[1] = NormalizeZeroOne(finalAngle, 0, maxAngle);
                        double[] output = new double[1];
                        NN.compute(input, output);
//                        double newReading = deNormalizeZeroOne(output[0], 0, 4200);
                        double newReading = deNormalizeZeroOne(output[0], 0, 3900);
                        readings[x] = (readings[x] < newReading) ? newReading : readings[x];
                    }
                }
            }
        }

        return readings;
      
    }
    
    double distance(double[] pointA, double[] pointB) {
        double d1 = pointA[0] - pointB[0];
        double d2 = pointA[1] - pointB[1];

        return Math.sqrt(d1 * d1 + d2 * d2);
    }
    
    public static Pair<Double, Double> rotateXY(double x, double y, double angle) {
        double rad = Math.toRadians(angle);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return new Pair(cos * x - sin * y, sin * x + cos * y);
    }
    
     public static double formatAngle(double angle) {
        while (angle > 360) {
            angle -= 360;
        }

        while (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public static int getQuadrant(double x, double y) {
        if (x >= 0) {
            if (y >= 0)
                return 4;
            else
                return 3;
        } else {
            if (y >= 0)
                return 1;
            else
                return 2;
        }
    }
    
    public static double NormalizeZeroOne(double val, double low, double high) {
        return (val - low) / (high - low);
    }

    public static int deNormalizeZeroOne(double val, double low, double high) {
        return (int) (val * (high-low) + low);
    }

  
}
