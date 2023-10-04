package com.nmu.evos.simulator;

import java.util.ArrayList;

public class MotionSimulator
{
    private NNSim nnsim;

    public MotionSimulator()
    {
        this.nnsim = new NNSim();
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

            double trad = globalt * 3.141592653589793D / 180.0D;
            double delxN = NN[0] * Math.cos(trad) - NN[1] * Math.sin(trad);
            double delyN = NN[0] * Math.sin(trad) + NN[1] * Math.cos(trad);

            globalx += delxN;
            globaly += delyN;
            globalt += NN[2];

            thePath.add(new State(globalx, globaly, globalt));

            prevleft = cur.left;
            prevright = cur.right;
        }

        return thePath;
    }
}