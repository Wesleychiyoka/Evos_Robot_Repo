package com.nmu.evos;

import java.util.Arrays;


public class SFFNeuralNetwork implements NeuralNetwork {
    public boolean print_neurons = false;
    public StringBuilder info = new StringBuilder();
    public boolean print_hidden_layer_neurons_only = false;
    private final int I;
    private final int J;
    private final int K;
    private final double[] b;
    private final int WEIGHT_COUNT;
    // Input to Hidden layer weights
    private final double[][] v;
    // Hidden to Output layer weights
    private final double[][] w;

    private double[][] no;

    public Activation hidden_layer_activation_function;
    public Activation output_layer_activation_function;

    public SFFNeuralNetwork(int i, int j, int k, double[] b) {
        if (b.length != 2) throw new IllegalArgumentException("Unexpected number of biases");
        this.I = i;
        this.J = j;
        this.K = k;
        this.b = b;
        this.WEIGHT_COUNT = (I + 1) * J + (J + 1) * K;
        this.no = new double[3][];
        v = new double[I + 1][J];
        w = new double[J + 1][K];
    }

    public double[] fire(double[] z, Activation hidden_layer_activation_function, Activation output_layer_activation_function) {
        if (z.length != I) throw new IllegalArgumentException("Unexpected number of inputs");
        info = new StringBuilder();
        no = new double[3][];
        no[0] = z;
        this.hidden_layer_activation_function = hidden_layer_activation_function;
        this.output_layer_activation_function = output_layer_activation_function;

        if (print_neurons && !print_hidden_layer_neurons_only) {
            //System.out.println("Input layer neurons: " + Arrays.toString(z));
            info.append("Input layer neurons: ").append(Arrays.toString(z)).append("\n");
        }
        double[] y = new double[J];
        int bias_index = 0;
        for (int j = 0; j < J; j++) {
            double x = 0;
            for (int i = 0; i < I; i++) {
                x += v[i][j] * z[i];
            }
            x += b[bias_index] * v[I][j];
            try {
                hidden_layer_activation_function.set(j);
            } catch (UnsupportedOperationException ignored) {
            }
            y[j] = hidden_layer_activation_function.calculate(x);

            if (print_neurons) {
                if (j == 0) {
                    //System.out.print("Hidden layer neurons: ");
                    info.append("Hidden layer neurons: ");
                }
                //System.out.print(y[j] + " ");
                info.append(y[j]).append(" ");
                if (j == J - 1) {
                    //System.out.println();
                    info.append("\n");
                }
            }
        }

        no[1] = y;

        bias_index++;
        double[] o = new double[K];
        for (int k = 0; k < K; k++) {
            double x = 0;
            for (int j = 0; j < J; j++) {
                x += w[j][k] * y[j];
            }
            x += b[bias_index] * w[J][k];
            o[k] = output_layer_activation_function.calculate(x);

            if (print_neurons && !print_hidden_layer_neurons_only) {
                if (k == 0) {
                    //System.out.print("Output layer neurons: ");
                    info.append("Output layer neurons: ");
                }
                //System.out.print(o[k] + " ");
                info.append(o[k]).append(" ");
                if (k == K - 1) {
                    //System.out.println("\n+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=");
                    info.append("\n+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=");
                }
            }
        }

        no[2] = o.clone();

        //System.out.println(getFormattedTable(true));

        return o;
    }

    public void setWeights(double[] weights) {
        if (weights.length != WEIGHT_COUNT) throw new IllegalArgumentException("Unexpected number of weights");
        int index = 0;
        for (int i = 0; i < I + 1; i++) {
            for (int j = 0; j < J; j++) {
                v[i][j] = weights[index++];
            }
        }
        for (int j = 0; j < J + 1; j++) {
            for (int k = 0; k < K; k++) {
                w[j][k] = weights[index++];
            }
        }
    }

    public double[] getWeights() {
        double[] weights = new double[WEIGHT_COUNT];
        int index = 0;
        for (int i = 0; i < I + 1; i++) {
            for (int j = 0; j < J; j++) {
                weights[index++] = v[i][j];
            }
        }
        for (int j = 0; j < J + 1; j++) {
            for (int k = 0; k < K; k++) {
                weights[index++] = w[j][k];
            }
        }
        return weights;
    }

    public double[][][] getWeightTable() {
        double[][] input_to_hidden = new double[I + 1][J];
        for (int i = 0; i < I + 1; i++) {
            for (int j = 0; j < J; j++) {
                input_to_hidden[i][j] = v[i][j];
            }
        }
        double[][] hidden_to_output = new double[J + 1][K];
        for (int j = 0; j < J + 1; j++) {
            for (int k = 0; k < K; k++) {
                hidden_to_output[j][k] = w[j][k];
            }
        }

        return new double[][][] {input_to_hidden, hidden_to_output};
    }

    @Override
    public int countWeights() {
        return WEIGHT_COUNT;
    }

    @Override
    public String toString() {
        return Arrays.toString(getWeights());
    }
}
