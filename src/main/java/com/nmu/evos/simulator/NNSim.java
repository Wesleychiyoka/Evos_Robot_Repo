package com.nmu.evos.simulator;

import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;

public class NNSim
{
	BasicNetwork NNx;
	BasicNetwork NNy;
	BasicNetwork NNt;

		public NNSim()
	{
			this.NNx = ((BasicNetwork)EncogDirectoryPersistence.loadObject(new File("NNxuse.eg")));
			this.NNy = ((BasicNetwork)EncogDirectoryPersistence.loadObject(new File("NNyuse.eg")));
			this.NNt = ((BasicNetwork)EncogDirectoryPersistence.loadObject(new File("NNtuse.eg")));
	}

	public double[] getOutput(int prevleft, int prevright, int newleft, int newright, int time)
	{
		double[] retme = new double[3];
		double[] input = new double[5];

		input[0] = (prevleft / 35000.0D);
		input[1] = (prevright / 35000.0D);
		input[2] = (newleft / 35000.0D);
		input[3] = (newright / 35000.0D);
		input[4] = (time / 3100.0D);
 
		double[] predoutput = new double[1];
		this.NNx.compute(input, predoutput);
		double delx = predoutput[0];
		this.NNy.compute(input, predoutput);
		double dely = predoutput[0];
		this.NNt.compute(input, predoutput);
		double delt = predoutput[0];

		retme[0] = (delx * 45.0D);
		retme[1] = (dely * 64.0D);
		retme[2] = (delt * 783.0D);

		return retme;
	}
}
