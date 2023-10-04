package com.nmu.evos;

import com.nmu.evos.simulator.*;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        new Main();
    }

    Main() {
        BasicVisualObstacleTest();
    }

    public void BasicVisualObstacleTest()
    {
        System.out.println("Test 1");
        State startState = new State(0,-20,0);

        ArrayList<Point> obstacles = new ArrayList<Point>();
        Point p = new Point(0, -5);
        obstacles.add(p);
        p = new Point(7, 7);
        obstacles.add(p);
        p = new Point(-5, 12);
        obstacles.add(p);
        p = new Point(15, 0);
        obstacles.add(p);
        p = new Point(-15, 0);
        obstacles.add(p);
        p = new Point(0, -12);
        obstacles.add(p);

//		p = new Point(49, 49);
//		obstacles.add(p);
//		p = new Point(40, 10);
//		obstacles.add(p);
//		p = new Point(10, 30);
//		obstacles.add(p);
//		p = new Point(20, 50);
//		obstacles.add(p);

        KheperaSimulator ks = new KheperaSimulator(obstacles, startState);

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


        ArrayList<KheperaState> states = ks.getKheperaState(coms);
        ArrayList<State> posstates = new ArrayList<State>();

        for (KheperaState kst : states)
        {
            System.out.println(kst.position.sx + " " + kst.position.sy + " " + kst.position.sa + " " + kst.collision);
            System.out.println("hi");
            posstates.add(kst.position);

            for (int i = 0; i < 9; i++)
            {
                System.out.print(kst.sensorReadings[i] + " ");

            }
            System.out.println();

        }


        VisualFrame vis = new VisualFrame(50, 50, 1000, 1000, obstacles, ks.obstacleRadius, new Point(90,90), new Point((int) startState.sx, (int) startState.sy) , ks.targetRadius, ks.robotRadius); //60, 60 is target location
        vis.setPath(posstates, "Number States: " + posstates.size());

        Thread t = new Thread(vis);

        t.start();

    }


    public void BasicObstacleTest()
    {
        State startState = new State(0,0,0);

        ArrayList<Point> obstacles = new ArrayList<Point>();
        Point p = new Point(10, 10);
        obstacles.add(p);
        p = new Point(50, 50);
        obstacles.add(p);
        p = new Point(40, 10);
        obstacles.add(p);
        p = new Point(10, 30);
        obstacles.add(p);
        p = new Point(20, 50);
        obstacles.add(p);

        KheperaSimulator ks = new KheperaSimulator(obstacles, startState);

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

        ArrayList<KheperaState> states = ks.getKheperaState(coms);

        for (KheperaState kst : states)
        {
            System.out.println(kst.position.sx + " " + kst.position.sy + " " + kst.position.sa + " " + kst.collision);


        }

    }


    public void BasicTest()
    {
        State startState = new State(0,0,0);

        KheperaSimulator ks = new KheperaSimulator(startState);

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

        ArrayList<KheperaState> states = ks.getKheperaState(coms);

        for (KheperaState kst : states)
        {
            System.out.println(kst.position.sx + " " + kst.position.sy + " " + kst.position.sa + " " + kst.collision);
        }

    }

    public void BasicVisualTest()
    {
        State startState = new State(0,0,90);

        KheperaSimulator ks = new KheperaSimulator(startState);

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

        ArrayList<KheperaState> states = ks.getKheperaState(coms);
        ArrayList<State> posstates = new ArrayList<State>();

        for (KheperaState kst : states)
        {
            System.out.println(kst.position.sx + " " + kst.position.sy + " " + kst.position.sa + " " + kst.collision);
            posstates.add(kst.position);
        }

        VisualFrame vis = new VisualFrame(0, 0, 1000, 1000, new ArrayList<Point>(), ks.obstacleRadius, new Point(60,60), new Point((int) startState.sx, (int) startState.sy) , ks.targetRadius, ks.robotRadius); //60, 60 is target location
        vis.setPath(posstates, "Number States: " + posstates.size());

        Thread t = new Thread(vis);

        t.start();
    }
}
