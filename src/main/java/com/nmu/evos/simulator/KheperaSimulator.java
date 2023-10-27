
package com.nmu.evos.simulator;

import java.util.*;

public class KheperaSimulator
{
    public final int numInterpolations = 5; 
    public final int sensorAngle = 7;
    public final int obstacleDangerDistance = 20;
    public static final int robotRadius = 7;
    public static final int obstacleRadius = 3;
    public final int targetRadius = 2;

    MotionSimulatorPatched msp;
    public SensorReadingSimulator srs;
    State startState;
    ArrayList<Point> obstacles;

    ArrayList<Command> commands;
    ArrayList<KheperaState> kstates;
    ArrayList<State> states;
    //ArrayList[][] obstacleQuad;

//    Integer MapSize = 200;
//    Integer QuadSize = 10;

    public KheperaSimulator(State start)
    {
        msp = new MotionSimulatorPatched();
        startState = start;
        srs = new SensorReadingSimulator(sensorAngle, obstacleDangerDistance, robotRadius, obstacleRadius); 
        this.obstacles = new ArrayList<Point>();
        this.commands = new ArrayList<Command>();
        this.kstates = new ArrayList<KheperaState>();
        this.states = new ArrayList<State>();
        states.add(start);
        ArrayList<ArrayList<Point>> yValues = new ArrayList<ArrayList<Point>>();
    }

    public KheperaSimulator(ArrayList<Point> obstacles, State start)
    {
        msp = new MotionSimulatorPatched();
        startState = start;
        srs = new SensorReadingSimulator(sensorAngle, obstacleDangerDistance, robotRadius, obstacleRadius); 
        this.obstacles = obstacles;
        this.commands = new ArrayList<Command>();
        this.kstates = new ArrayList<KheperaState>();
        this.states = new ArrayList<State>();
        states.add(start);
        ArrayList<ArrayList<Point>> yValues;
        //populateQuads(obstacles);        
    }

//    public void populateQuads(ArrayList<Point> obstacles)
//    {
//        Integer x;
//        Integer y;
//        Boolean busy = true;
//        while(busy)
//        {
//            busy = false;
//            obstacleQuad = new ArrayList[(2*MapSize)/QuadSize][(2*MapSize)/QuadSize];
//            for(Point obstacle : obstacles)
//            {
//                x = ((Double)(Math.floor((obstacle.x+(MapSize/2))/QuadSize))).intValue();
//                y = ((Double)(Math.floor((obstacle.y+(MapSize/2))/QuadSize))).intValue();
//                if(x>MapSize/QuadSize || y>MapSize/QuadSize)
//                {
//                    MapSize = MapSize*2;
//                    busy=true;
//                    break;
//                }
//                if(obstacleQuad[x][y]==null)
//                    obstacleQuad[x][y] = new ArrayList();
//                //obstacleQuads.get(x).get(y).add(obstacle);
//                obstacleQuad[x][y].add(obstacle);
//            }
//            
//        }
//    }
    
    public ArrayList<KheperaState> getKheperaState(ArrayList<Command> commands)
    {
        if(kstates.size()==0)
        {
            KheperaState ks = new KheperaState();
            ks.position = startState;
            ks.sensorReadings = srs.getSensorReadings((int) startState.sx, (int) startState.sy, startState.sa, obstacles);
            ks.collision = false;
            this.kstates.add(ks);
        }

        Integer difference = 0;

        if(commands.size() > 0)
            difference = commands.size()-(states.size()-1);

        for(int x = difference; x>0; x--)
        {            
            Command command = commands.get(commands.size()-x);           
            this.commands.add(command);
            State prevState = states.get(states.size() - 1);
            this.states.add(msp.getMovement(prevState, command));
            State s = states.get(states.size() - 1);
            KheperaState ks = new KheperaState();
            ks.position = s;
            //ArrayList<Point> obstacleCheck = getObstacles(s, prevState);
            ks.sensorReadings = srs.getSensorReadings((int) s.sx, (int) s.sy, s.sa, obstacles);
            ks.collision = checkCollisionOnPath(prevState, command, msp, obstacles, ks.sensorReadings) || checkForCollision(s, obstacles, ks.sensorReadings);
            this.kstates.add(ks);
        }

        return this.kstates;
    }

    public KheperaState getNextKheperaState(Command command) {
        if(kstates.size() == 0)
        {
            KheperaState ks = new KheperaState();
            ks.position = startState;
            ks.sensorReadings = srs.getSensorReadings((int) startState.sx, (int) startState.sy, startState.sa, obstacles);
            ks.collision = false;
            this.kstates.add(ks);
        }

        this.commands.add(command);
        State prevState = states.get(states.size() - 1);
        states.add(msp.getMovement(prevState, command));
        State s = states.get(states.size() - 1);
        KheperaState ks = new KheperaState();
        ks.position = s;
        ks.sensorReadings = srs.getSensorReadings((int) s.sx, (int) s.sy, s.sa, obstacles);
        ks.collision = checkCollisionOnPath(prevState, command, msp, obstacles, ks.sensorReadings) || checkForCollision(s, obstacles, ks.sensorReadings);
        kstates.add(ks);

        return ks;
    }

    private boolean checkCollisionOnPath(State start, Command netCommand, MotionSimulatorPatched sim, ArrayList<Point> obstacleList, double[] sensorReadings) {
        int increment = netCommand.time / numInterpolations;
        int time = increment;
        if (checkForCollision(start, obstacleList, sensorReadings)) return true;
        for (int i = 0; i < numInterpolations; i++) {
            ArrayList<Command> commands = new ArrayList<Command>();
            commands.add(new Command(netCommand.left, netCommand.right, time));
            ArrayList<State> states = sim.getPath(start, commands);            
            if (checkForCollision(states.get(1), obstacleList, sensorReadings)) return true;
            time += increment;
        }
        return false;
    }

    private boolean checkForCollision(State stateOnPath, ArrayList<Point> obstacleList, double[] sensorReadings) {
        sensorReadings = srs.getSensorReadings((int)stateOnPath.sx, (int)stateOnPath.sy, stateOnPath.sa, obstacleList);
        for(Double d : sensorReadings) {
            if (d < 3900 && d>3500) return true;
        }
        for (Point p : obstacleList) {
            if (Math.pow(p.x - stateOnPath.sx, 2) + Math.pow(p.y - stateOnPath.sy, 2) <= Math.pow(robotRadius + obstacleRadius, 2))
            {            	
                return true;
            }
        }
        return false;
    }
//    public ArrayList<Point> getObstacles(State curS, State prevS)
//    {
//        ArrayList<Point> newObstacles = new ArrayList<Point>();
//
//        Integer botXf = ((Double)(Math.floor((curS.sx+(MapSize/2))/QuadSize))).intValue();
//        Integer botYf = ((Double)(Math.floor((curS.sy+(MapSize/2))/QuadSize))).intValue();
//        Integer botXs = ((Double)(Math.floor((prevS.sx+(MapSize/2))/QuadSize))).intValue();
//        Integer botYs = ((Double)(Math.floor((prevS.sy+(MapSize/2))/QuadSize))).intValue();
//        if(botXf>botXs)
//        {
//            Integer temp = botXf;
//            botXf = botXs;
//            botXs = temp;
//        }
//        if(botYf>botYs)
//        {
//            Integer temp = botYf;
//            botYf = botYs;
//            botYs = temp;
//        }
//
//        for(int x = botXf-1; x<botXs+2; x++)
//        {
//            if(x<MapSize/QuadSize&&x>-1)
//            {
//                for(int y = botYf-1; y<botYs+2; y++)
//                {
//                    if(y<MapSize/QuadSize&&y>-1)
//                    {
//                        if(obstacleQuad[x][y]!=null)
//                            newObstacles.addAll((ArrayList<Point>)obstacleQuad[x][y]);
//                    }
//                }
//            }
//        }
//
//        return newObstacles;
//    }
}
