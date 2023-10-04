package com.nmu.evos.execute;

import com.fazecast.jSerialComm.SerialPort;
import com.nmu.evos.simulator.Command;

import java.io.*;

public class RobotInterface
{

    private SerialPort port;
    private InputStream in;
    private OutputStream out;
    private boolean go;

    private class realWorldInteruptThread extends Thread
    {
        @Override
        public void run() {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            try {
                in.readLine();
                go = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    public RobotInterface(String portName) {
//        System.out.println("Attempting to connect to port: " + portName + ". You sure about this?");
//
//        try {
//            connect(portName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("Connection successful.");
//    }

    public RobotInterface(int portNum) {
        System.out.println("Attempting to connect to port: " + portNum + ". You sure about this?");

        port = SerialPort.getCommPorts()[portNum];
        setupPort();
        System.out.println("Connection successful.");
    }

    public static void showPorts() {
        System.out.println("Available ports: ");
        System.out.println();

        SerialPort[] ports = SerialPort.getCommPorts();

        int i = 0;
        for (SerialPort p : ports) {
            System.out.println("[" + i + "] " + p.getSystemPortName());
            i++;
        }
        System.out.println();
    }

    private void setupPort() {
        port.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, port.getWriteTimeout());
        port.openPort();

        in = port.getInputStream();
        out = port.getOutputStream();

        System.out.println(in == null);
        System.out.println(out == null);
    }

//    void connect ( String portName ) throws Exception
//    {
//        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
//        ports.nextElement();
//        ports.nextElement();
//        ports.nextElement();
//        ports.nextElement();
//        ports.nextElement();
//        ports.nextElement();
//        ports.nextElement();
//
//        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
//        if ( portIdentifier.isCurrentlyOwned() )
//        {
//            System.out.println("Error: Port is currently in use");
//        }
//        else
//        {
//            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
//
//            if ( commPort instanceof SerialPort )
//            {
//                SerialPort serialPort = (SerialPort) commPort;
//                serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
//
//                in = serialPort.getInputStream();
//                out = serialPort.getOutputStream();
//            }
//            else
//            {
//                System.out.println("Error: Only serial ports are handled by this example.");
//            }
//        }
//    }

    public void sendString(String string) {
        try {
            out.write((string + "\n").getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closePort() {
        try {
            stop();

            out.close();
            in.close();
            port.closePort();

            System.out.println("Closed serial port.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(Command command, int waitTime) {
        try {
            sendString(command.toString());
//            System.out.println("Sleep for: " + waitTime);
            if (waitTime > 0)
                Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            System.out.println("Could not sleep");
            e.printStackTrace();
        }
    }

    public void stop() {
        String message = "D,l0,l0";
        sendString(message);

        message = "M";
        sendString(message);
    }

    public double[] getSensorReading() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String message = "N";
        sendString(message);
        String line = "";

        while (!line.startsWith("n"))
            line = reader.readLine();

        String[] splitResponse = line.split(",");
        double[] intSensorValues = new double[11];
        //Exclude the initial o and the final timestamp
//        String temp = "";
        for (int i = 1; i < splitResponse.length - 1; i++) {
            intSensorValues[i - 1] = Integer.parseInt(splitResponse[i]);
//            temp += intSensorValues[i - 1] + "\t";
        }

//        System.out.println(temp);

        return intSensorValues;
    }

    public double[] getAmbientSensorReading() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String message = "O";
        sendString(message);
        String line = "";

        while (!line.startsWith("o"))
            line = reader.readLine();

        String[] splitResponse = line.split(",");
        double[] intSensorValues = new double[11];

        for (int i = 1; i < splitResponse.length - 1; i++)
            intSensorValues[i - 1] = Integer.parseInt(splitResponse[i]);

        return intSensorValues;
    }

    private double[] normaliseSensorReadings(double[] readings) {
        double[] normalised = new double[2];
        normalised[0] = (readings[0] - 2100) / 1850.0; //range : 2100 - 3950
        normalised[1] = (readings[1] - 2450) / 1500.0; //range : 2500 - 3950
        return normalised;
    }

    public double[] getFrontSensorReadings() {
        try {
            double[] sensors = getSensorReading();
            double[] frontSensors = new double[]{sensors[9], sensors[10]};
            double[] normalised = normaliseSensorReadings(frontSensors);
            //NOW WE FLIP THE VALUES BECAUSE OF THE ORDER OF THE SENSOR VALUES
            //AND WE DO A (1-Number) SORT OF FLIP THING TOO
            System.out.println(1-normalised[1] + ", " + (1-normalised[0]));
            return new double[]{1-normalised[1], 1-normalised[0]};
//            System.out.println(normalised[0] + ", " + normalised[1]);
//            return new double[]{normalised[0], normalised[1]};
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
