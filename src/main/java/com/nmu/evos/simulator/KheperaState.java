package com.nmu.evos.simulator;


public class KheperaState
{
	public State position;  //x, y location and angle
	public double[] sensorReadings;   //9 sensor readings in order: left_90, left_45, front_left, front_right, right_45, right_90,  back_right, back,  back_left
	public boolean collision; //true if robot is currently in a collision state
}


              