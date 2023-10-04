
package com.nmu.evos.simulator;

public class Command
{
	public int left;
	public int right;
	public int time;
	public long timestamp;
	
	//Please note:
	//max_motor_speed=17500.0 (positive or negative)
	//min_motor_speed=8000.0	(positive or negative)
	//max_motor_time=3100
	//min_motor_time=300
	//preferred_motor_time=800
	
	public Command(int l, int r, int t)
	{
		this.left = l;
		this.right = r;
		this.time = t;
	}
	
	public Command(int l, int r, int t, long timestamp)
	{
		this.left = l;
		this.right = r; 
		this.time = t;
		this.timestamp = timestamp;
	}
}
