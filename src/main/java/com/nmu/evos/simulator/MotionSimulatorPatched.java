package com.nmu.evos.simulator;

import java.util.ArrayList;

public class MotionSimulatorPatched
{
	private NNSim nnsim;
        
        private int prevLeft;
	private int prevRight;

	public MotionSimulatorPatched()
	{
		this.nnsim = new NNSim();
	}
        
        public State getMovement(State prevState, Command cur)
	{
		double globalx = prevState.sx;
		double globaly = prevState.sy;
		double globalt = prevState.sa;

		double[] NN = this.nnsim.getOutput(prevLeft, prevRight, cur.left, cur.right, cur.time);

		double[] NNAlt = this.nnsim.getOutput(prevRight, prevLeft, cur.right, cur.left, cur.time);

		double trad = globalt * 3.141592653589793D / 180.0D;
		double delxN = NN[0] * Math.cos(trad) - NN[1] * Math.sin(trad);
		double delyN = NN[0] * Math.sin(trad) + NN[1] * Math.cos(trad);

		// Fix for angular drift when attempting to go in a straight line
		double tcorrection = (NN[2] + NNAlt[2]) / 2;

//			 Fix for robot drift
		if(Math.abs(delxN + delyN) < 0.55) {
			delxN = 0;
			delyN = 0;
		}

		globalx += delxN;
		globaly += delyN;
		globalt += NN[2] - tcorrection;
		// Fix for robot angle output
		globalt = globalt + (globalt < 0 ? 1 : -1)*(360.0 * Math.floor(Math.abs(globalt / 360.0)));
		if (globalt < -180){
			globalt = 180 - (Math.abs(globalt) - 180);
		} else if (globalt > 180) {
			globalt = -(360 - globalt);
		}

		prevLeft = cur.left;
		prevRight = cur.right;
		return new State(globalx, globaly, globalt);
	}


	public ArrayList<State> getPath(State origin, ArrayList<Command> commands)
	{
		ArrayList<State> thePath = new ArrayList<State>();

		double globalx = origin.sx;
		double globaly = origin.sy;
		double globalt = origin.sa;

		thePath.add(origin);

		int prevleft = 0;
		int prevright = 0;

		for (int i = 0; i < commands.size(); i++)
		{
			Command cur = commands.get(i);

			double[] NN = this.nnsim.getOutput(prevleft, prevright, cur.left, cur.right, cur.time);

            double[] NNAlt = this.nnsim.getOutput(prevright, prevleft, cur.right, cur.left, cur.time);

			double trad = globalt * 3.141592653589793D / 180.0D;
			double delxN = NN[0] * Math.cos(trad) - NN[1] * Math.sin(trad);
			double delyN = NN[0] * Math.sin(trad) + NN[1] * Math.cos(trad);

			// Fix for angular drift when attempting to go in a straight line
            double tcorrection = (NN[2] + NNAlt[2]) / 2;

//			 Fix for robot drift
			if(Math.abs(delxN + delyN) < 0.55) {
				delxN = 0;
				delyN = 0;
			}

            globalx += delxN;
            globaly += delyN;
            globalt += NN[2] - tcorrection;
			// Fix for robot angle output
            globalt = globalt + (globalt < 0 ? 1 : -1)*(360.0 * Math.floor(Math.abs(globalt / 360.0)));
            if (globalt < -180){
                globalt = 180 - (Math.abs(globalt) - 180);
            } else if (globalt > 180) {
                globalt = -(360 - globalt);
            }

			thePath.add(new State(globalx, globaly, globalt));

			prevleft = cur.left;
			prevright = cur.right;
		}

		return thePath;
	}
}