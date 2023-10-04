package com.nmu.evos.execute;

import com.nmu.evos.simulator.Command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class Main {
    RobotInterface robotInterface;

    public Main() throws IOException {
        RobotInterface.showPorts();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Port num: ");
        int port = scanner.nextInt();

        robotInterface = new RobotInterface(port);

        testRa();

        robotInterface.closePort();
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