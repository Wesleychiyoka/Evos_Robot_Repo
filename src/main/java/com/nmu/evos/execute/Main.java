package com.nmu.evos.execute;

import com.nmu.evos.ER;
import com.nmu.evos.Normalisation;
import com.nmu.evos.TanHActivation;
import com.nmu.evos.simulator.Command;
import com.nmu.evos.simulator.Point;
import com.nmu.evos.simulator.State;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import static com.nmu.evos.ER.sensor_range;

public class Main {
    RobotInterface robotInterface;

    public Main() throws IOException {
        RobotInterface.showPorts();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Port num: ");
        int port = scanner.nextInt();

        robotInterface = new RobotInterface(port);

        testNE();

        robotInterface.closePort();
    }

    public void testNE() throws IOException {
        String filename = "er/overlap_distance_10/best.er.20231027071846.com.nmu.evos.TanHActivation.com.nmu.evos.TanHActivation.nn";
        ER er = new ER();
        ER.Individual individual = er.loadBest(filename);
        Tracker tracker = new Tracker(er.start_end_pos[0], er.robot_initial_orientation, new Point(0, 0), new Point(150, 150));

        tracker.showVisualPosition(); // Shows where the robot is and oriented in simulation

        // Initial state
        double[] normalised_xy = Normalisation.normalise(new double[] {er.start_end_pos[0].x, er.start_end_pos[0].y}, 0, 150, -1, 1);
        double[] input = new double[11];
        input[0] = normalised_xy[0];
        input[1] = normalised_xy[1];
        double[] sensors = robotInterface.getSensorReading();
        System.arraycopy(Normalisation.normalise(sensors, sensor_range[0], sensor_range[1], -1, 1), 0, input, 2, sensors.length); // Copying normalised sensor values to 'input' from index 4 onwards
        for (int i  = 0; i < 40; i++) {
            double[] output = individual.genome.fire(input, new TanHActivation(), new TanHActivation());
            double[] denormalised_output = Normalisation.de_normalise(output, 8_000, 17_500, -1, 1);
            Command command = new Command((int) denormalised_output[0], (int) denormalised_output[1], 800);
            robotInterface.sendCommand(command, 800);

            State state = tracker.getApproximatePosition(command);
            normalised_xy = Normalisation.normalise(new double[] {state.sx, state.sy}, 0, 150, -1, 1);

            input[0] = normalised_xy[0];
            input[1] = normalised_xy[1];
            sensors = robotInterface.getSensorReading();

            System.arraycopy(Normalisation.normalise(sensors, sensor_range[0], sensor_range[1], -1, 1), 0, input, 2, sensors.length);
        }
    }

    public void testRa() throws IOException {
        ArrayList<Command> coms = new ArrayList<Command>();
        Command c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(8000, 0, 1000);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(16000, 8000, 400);
        coms.add(c);
        c = new Command(16000, 8000, 400);
        coms.add(c);
        c = new Command(16000, 8000, 400);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        c = new Command(8000, 8000, 3000);
        coms.add(c);
        for (Command com : coms) {
            robotInterface.sendCommand(com, 800);

            double[] sensorReadings = robotInterface.getAmbientSensorReading();
            System.out.println("Sensor readings: " + Arrays.toString(sensorReadings));
        }
    }

    public void log(String filename, String info) throws FileNotFoundException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = formatter.format(new Date());

        PrintWriter writer = new PrintWriter(filename + "." + timestamp + ".log");
        writer.println(info);
        writer.close();

    }

    public static void main(String[] args) throws IOException {
        new Main();
    }
}