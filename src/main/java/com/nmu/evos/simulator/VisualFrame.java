
package com.nmu.evos.simulator;

import com.nmu.evos.execute.Tracker;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class VisualFrame extends Frame implements Runnable, KeyListener, WindowListener {
    private Tracker.VisualGrid visualGrid = null;
    Thread t;
    Image buffer;
    Graphics bg;
    int x = 0;
    int y = 0;
    int angle = 0;

    int Scale = 1;
    int BaseUnit = 200;

    boolean done = false;

    int pointer = 0;

    private ArrayList<State> path;
    private ArrayList<State> goalPath;
    private ArrayList<State> patternStates;
    private String message;
    private ArrayList<Integer> timeSequence;
    private double speedUpFactor;
    private ArrayList<Point> obstacles;
    private Point target;
    private double targetSize;
    private double obstacleSize;
    private double robotSize;

    public VisualFrame(int left, int right, int width, int height, ArrayList<Point> obstacles, double obstacleSize, Point target, Point start, double targetSize, double robotSize) {
        this.obstacles = obstacles;
        this.obstacleSize = obstacleSize*2;
        this.robotSize = robotSize*2;
        this.target = target;
        this.targetSize = targetSize*2;
        this.speedUpFactor = 1;
        this.path = new ArrayList<State>();
        this.patternStates = new ArrayList<State>();
        this.message = "";
        addKeyListener(this);
        addWindowListener(this);
        setBounds(left, right, width, height);
        setVisible(true);
        
        this.x = (int)start.x;
        this.y = (int)start.y;
        
       
    }

    public VisualFrame(int left, int right, int width, int height, ArrayList<Point> obstacles, double obstacleSize, Point target, Point start, double targetSize, double robotSize, Tracker.Grid[][] grids) {
        this.obstacles = obstacles;
        this.obstacleSize = obstacleSize*2;
        this.robotSize = robotSize*2;
        this.target = target;
        this.targetSize = targetSize*2;
        this.speedUpFactor = 1;
        this.path = new ArrayList<State>();
        this.patternStates = new ArrayList<State>();
        this.message = "";
        this.visualGrid = new Tracker.VisualGrid(grids, this);
        addKeyListener(this);
        addWindowListener(this);
        setBounds(left, right, width, height);
        setVisible(true);

        this.x = (int)start.x;
        this.y = (int)start.y;


    }

    public void setSpeedUpFactor(double factor) {
        this.speedUpFactor = factor;
    }

    public void setPath(ArrayList<State> path, String message) {
        this.path = path;
        this.message = message;
    }

    public void setGoalPath(ArrayList<State> path, String message) {
        this.goalPath = path;
    }

    public void setPatternStates(ArrayList<State> patternStates) {
        this.patternStates = patternStates;
    }

    public void run() {
        this.drawGraphics();
    }

    public void resetPointer() {
        this.pointer = 0;
        this.Scale = 1;
    }

    public void drawGraphics() {
        while (!this.done) {
        	
        	
            try {
               /* if (this.timeSequence != null && this.pointer < this.path.size() && this.pointer < this.timeSequence.size()) {
//	    		   Thread.sleep(200L);
                    Thread.sleep((int) (this.timeSequence.get(this.pointer) / this.speedUpFactor));
                } else {
                    Thread.sleep(800L);
                }*/
                
                Thread.sleep(800L);
            } catch (Exception e) {
                System.out.println("Error");
            }
            

            if (this.pointer < this.path.size()) {
            	//System.out.println(pointer + " " + this.path.size());
                this.pointer++;
               // System.out.println(pointer + " " + this.path.size());
                setTitle(this.pointer + "");
            }
            else
            {
            	this.done = true;
            }

            repaint();
           
        }
    }

    public void paint(Graphics g) {
    	
    	
        this.buffer = createImage(getWidth(), getHeight());
        this.bg = this.buffer.getGraphics();

        this.bg.setColor(Color.GRAY);
        this.bg.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        this.bg.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());

        this.bg.drawString("0", getWidth() / 2, getHeight() / 2);

        this.bg.drawString(this.message, 50, 50);

        // draw pattern points
        this.bg.setColor(Color.green);
        this.drawPatternStates(this.patternStates);

        this.bg.setColor(Color.GRAY);

        for (int k = 1; k * (this.BaseUnit / this.Scale) < getWidth() / 2; k++) {
            this.bg.drawLine(getWidth() / 2 + k * (this.BaseUnit / this.Scale), getHeight() / 2 + 3, getWidth() / 2 + k * (this.BaseUnit / this.Scale), getHeight() / 2 - 3);
            this.bg.drawString(k * 100 + "", getWidth() / 2 + k * (this.BaseUnit / this.Scale), getHeight() / 2);
        }

        for (int k = 1; k * (this.BaseUnit / this.Scale) < getWidth() / 2; k++) {
            this.bg.drawLine(getWidth() / 2 - k * (this.BaseUnit / this.Scale), getHeight() / 2 + 3, getWidth() / 2 - k * (this.BaseUnit / this.Scale), getHeight() / 2 - 3);
            this.bg.drawString(-1 * k * 100 + "", getWidth() / 2 - k * (this.BaseUnit / this.Scale), getHeight() / 2);
        }

        for (int k = 1; k * (this.BaseUnit / this.Scale) < getHeight() / 2; k++) {
            this.bg.drawLine(getWidth() / 2 - 3, getHeight() / 2 + k * (this.BaseUnit / this.Scale), getWidth() / 2 + 3, getHeight() / 2 + k * (this.BaseUnit / this.Scale));
            this.bg.drawString(-1 * k * 100 + "", getWidth() / 2, getHeight() / 2 + k * (this.BaseUnit / this.Scale));
        }

        for (int k = 1; k * (this.BaseUnit / this.Scale) < getHeight() / 2; k++) {
            this.bg.drawLine(getWidth() / 2 - 3, getHeight() / 2 - k * (this.BaseUnit / this.Scale), getWidth() / 2 + 3, getHeight() / 2 - k * (this.BaseUnit / this.Scale));
            this.bg.drawString(k * 100 + "", getWidth() / 2, getHeight() / 2 - k * (this.BaseUnit / this.Scale));
        }

        // draw goal path
        if (this.goalPath != null) {
            this.bg.setColor(Color.pink);
            this.drawPath(this.goalPath);
        }

        // draw path
        this.bg.setColor(new Color(0.0F, 0.0F, 1.0F, 0.5F));
        this.drawPath(this.path);

        this.bg.setColor(Color.red);
        this.bg.fillOval(this.x - (int)(robotSize / this.Scale) + getWidth() / 2, -1 * this.y - (int)(robotSize/  this.Scale) + getHeight() / 2, (int)(robotSize /  this.Scale) * 2, (int)(robotSize /  this.Scale) * 2);
        this.bg.drawLine(this.x + getWidth() / 2, -1 * this.y + getHeight() / 2, (int) (this.x + getWidth() / 2 + 40.0D / this.Scale * Math.cos(this.angle * 1.0D / 360.0D * 2.0D * 3.141592653589793D)), (int) (-1 * this.y + getHeight() / 2 - 40.0D / this.Scale * Math.sin(this.angle * 1.0D / 360.0D * 2.0D * 3.141592653589793D)));

        this.bg.setColor(new Color(0.0F, 1.0F, 0.0F, 0.5F));
        for (Point p : obstacles)
            this.bg.fillOval((int)p.x*2 / this.Scale - (int) (obstacleSize / this.Scale) + getWidth() / 2, -(int)p.y*2 / this.Scale - (int) (obstacleSize / this.Scale) + getHeight() / 2, (int)(obstacleSize / this.Scale) * 2, (int) (obstacleSize / this.Scale) * 2);

        this.bg.setColor(new Color(1.0F, 0.0F, 1.0F, 0.5F));
        this.drawPathPoints(this.path);

        this.bg.setColor(new Color(0.0F, 0.0F, 1.0F, 0.5F));
        this.bg.fillOval((int)target.x*2 / this.Scale - (int) targetSize + getWidth() / 2, -(int)target.y*2 / this.Scale - (int)obstacleSize   + getHeight() / 2, (int)obstacleSize  * 2, (int) obstacleSize  * 2);

        //region CUSTOM METHOD CALL
        Optional.ofNullable(visualGrid).ifPresent(vg -> vg.drawGrids(bg, Scale));
        //endregion

        this.bg.setColor(new Color(0.0F, 1.0F, 0.0F, 1.0F));
        g.drawImage(this.buffer, 0, 0, null);
    }

    private void drawPatternStates(ArrayList<State> patternStates) {
        for (int i = 0; i < patternStates.size(); i++) {
            int x = ((int) patternStates.get(i).sx / this.Scale * this.BaseUnit / 100);
            int y = ((int) patternStates.get(i).sy / this.Scale * this.BaseUnit / 100);
            drawCircle(x, y, this.Scale * this.BaseUnit / 100);
        }
    }

    private void drawCircle(int x, int y, int radius) {
        this.bg.fillOval(x - radius + getWidth() / 2, y - radius + getHeight() / 2, radius * 2, radius * 2);
    }

    public void update(Graphics g) {
        paint(g);
    }

    private void drawObstacles() {

    }

    private void drawTarget() {

    }

    private void drawPathPoints(ArrayList<State> path) {
        for (State state : path) {
            this.bg.fillOval((int)state.sx*2 / this.Scale - 4 + getWidth() / 2, -(int)state.sy*2 / this.Scale - 4 + getHeight() / 2, 4, 4);
        }
    }

    private void drawPath(ArrayList<State> path) {
        int prevx = 0;
        int prevy = 0;

        for (int k = 0; k < this.pointer; k++) {
            State s = (State) path.get(k);

            this.x = ((int) s.sx / this.Scale * this.BaseUnit / 100);
            this.y = ((int) s.sy / this.Scale * this.BaseUnit / 100);
            this.angle = ((int) s.sa + 90);

            if (k == 0) {
                prevx = this.x;
                prevy = this.y;
            }

            this.bg.drawLine(prevx + getWidth() / 2, -1 * prevy + getHeight() / 2, this.x + getWidth() / 2, -1 * this.y + getHeight() / 2);

            prevx = this.x;
            prevy = this.y;
        }

        if ((Math.abs(this.x) > getWidth() / 2) || (Math.abs(this.y) > getHeight() / 2)) {
            this.Scale += 1;
        }
    }

    public void setTimeSequence(ArrayList<Integer> times) {
        this.timeSequence = times;
    }


    public void close() {
        this.done = true;
        setVisible(false);
        dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == KeyEvent.VK_ESCAPE) {
            this.close();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        this.close();
        
    }

    @Override
    public void windowClosed(WindowEvent e) {
        this.close();
       
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
