package com.nmu.evos.simulator;

public class Point
{
	public double x;
	public double y;

	public Point(double xin, double yin)
	{
		this.x = xin;
		this.y = yin;
	}

	@Override
	public Point clone() {
		return new Point(x, y);
	}
}
