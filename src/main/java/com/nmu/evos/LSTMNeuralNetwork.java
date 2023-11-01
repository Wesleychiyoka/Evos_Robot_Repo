package com.nmu.evos;

import java.util.Arrays;

public class LSTMNeuralNetwork implements NeuralNetwork {
    private final int inputSize;
    private final int hiddenSize;
    private final int outputSize;
    private final int WEIGHT_COUNT;

    // Define LSTM weight matrices and biases
    private final double[][] W_f;
    private final double[][] U_f;
    private final double[] b_f;
    private final double[][] W_i;
    private final double[][] U_i;
    private final double[] b_i;
    private final double[][] W_c;
    private final double[][] U_c;
    private final double[] b_c;
    private final double[][] W_o;
    private final double[][] U_o;
    private final double[] b_o;

    // Define output weight matrix and bias
    private final double[][] V;
    private final double[] c;

    public Activation hidden_layer_activation_function;
    public Activation output_layer_activation_function;

    public LSTMNeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.WEIGHT_COUNT =  4*(hiddenSize*inputSize) + 4*(hiddenSize*hiddenSize)+4*(hiddenSize)+(outputSize*hiddenSize)+outputSize;

        // Initialize weight matrices and biases
        W_f = new double[hiddenSize][inputSize];
        U_f = new double[hiddenSize][hiddenSize];
        b_f = new double[hiddenSize];

        W_i = new double[hiddenSize][inputSize];
        U_i = new double[hiddenSize][hiddenSize];
        b_i = new double[hiddenSize];

        W_c = new double[hiddenSize][inputSize];
        U_c = new double[hiddenSize][hiddenSize];
        b_c = new double[hiddenSize];

        W_o = new double[hiddenSize][inputSize];
        U_o = new double[hiddenSize][hiddenSize];
        b_o = new double[hiddenSize];

        V = new double[outputSize][hiddenSize];
        c = new double[outputSize];
    }

    // Activation function (sigmoid)
    private double sigmoid(double x) {
        // Implement the sigmoid activation function for an array of values
        // For example, you can use a loop to apply the sigmoid function to each element.
        double result = 0;
        result = 1.0 / (1.0 + Math.exp(-x));
        return result;
    }

    private double tanh(double x) {
        // Implement the hyperbolic tangent (tanh) activation function for an array of values
        // For example, you can use a loop to apply the tanh function to each element.
        double result = 0;
        result = Math.tanh(x);
        return result;
    }

    private double[] dot(double[][] matrix, double[] vector) {
        // Implement the dot product between a matrix and a vector
        // For example, you can use loops to perform the dot product.
        int numRows = matrix.length;
        int numCols = matrix[0].length;
        double[] result = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            double sum = 0.0;
            for (int j = 0; j < numCols; j++) {
                sum += matrix[i][j] * vector[j];
            }
            result[i] = sum;
        }
        return result;
    }

    // Forward pass through LSTM
    public double[] lstmForward(double[] x, double[] hPrev, double[] cPrev) {
        double[] forgetGate = new double[hiddenSize];
        double[] inputGate = new double[hiddenSize];
        double[] cellUpdate = new double[hiddenSize];
        double[] cNext = new double[hiddenSize];
        double[] outputGate = new double[hiddenSize];
        double[] hNext = new double[hiddenSize];

        // Implement the LSTM forward pass here (use your weight matrices and biases)

        return hNext;
    }

    public double[] fire(double[] input, Activation hiddenLayerActivation, Activation outputLayerActivation) {
        double[] h = new double[hiddenSize];
        double[] c = new double[hiddenSize];

        for (int t = 0; t < input.length; t++) {
            // Forward pass through LSTM for the current time step
            double[] x = new double[inputSize]; // Create an input vector x

            // Assign values to the input vector x based on your input data
            // For example:
            // x[0] = input[t]; // Adjust this to match your input data

            double[] forgetGate = new double[hiddenSize];
            double[] inputGate = new double[hiddenSize];
            double[] cellUpdate = new double[hiddenSize];
            double[] cNext = new double[hiddenSize];
            double[] outputGate = new double[hiddenSize];

            for (int i = 0; i < hiddenSize; i++) {
                forgetGate[i] = sigmoid(W_f[i][0] * x[0] + U_f[i][0] * h[0] + b_f[i]);
                inputGate[i] = sigmoid(W_i[i][0] * x[0] + U_i[i][0] * h[0] + b_i[i]);
                cellUpdate[i] = tanh(W_c[i][0] * x[0] + U_c[i][0] * h[0] + b_c[i]);
                cNext[i] = forgetGate[i] * c[i] + inputGate[i] * cellUpdate[i];
                outputGate[i] = sigmoid(W_o[i][0] * x[0] + U_o[i][0] * h[0] + b_o[i]);
            }

            for (int i = 0; i < hiddenSize; i++) {
                cNext[i] = forgetGate[i] * c[i] + inputGate[i] * cellUpdate[i];
            }

            double[] hNext = new double[hiddenSize];
            for (int i = 0; i < hiddenSize; i++) {
                hNext[i] = outputGate[i] *  Math.tanh(cNext[i]);
            }

            h = hNext;
            c = cNext;
        }

        // Calculate output using weight matrix V and bias c
        double[] output = new double[outputSize];

        // Implement the calculation of the output based on your architecture
        // For example:
        // for (int i = 0; i < outputSize; i++) {
        //     output[i] = dot(V[i], h) + c[i]; // Adjust this based on your architecture
        // }

        return output;
    }

    public void setWeights(double[] weights) {

        if (weights.length != WEIGHT_COUNT) throw new IllegalArgumentException("Unexpected number of weights " + weights.length + " " + WEIGHT_COUNT);
        int index = 0;
        // Update weights for W_f, U_f, and b_f
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                W_f[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                U_f[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            b_f[i] = weights[index++];
        }

        // Update weights for W_i, U_i, and b_i

        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                W_i[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                U_i[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            b_i[i] = weights[index++];
        }

        // Update weights for W_c, U_c, and b_c
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                W_c[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                U_c[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            b_c[i] = weights[index++];
        }

        // Update weights for W_o, U_o, and b_o

        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                W_o[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                U_o[i][j] = weights[index++];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            b_o[i] = weights[index++];
        }

        // Update weights for V
        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                V[i][j] = weights[index++];
            }
        }

        // Update bias c for the output layer
        for (int i = 0; i < outputSize; i++) {
            c[i] = weights[index++];
        }
    }

    public double[] getWeights() {
        double[] weights = new double[WEIGHT_COUNT];
        int index = 0;
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights[index++] = W_f[i][j] ;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights[index++] = U_f[i][j] ;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            weights[index++] = b_f[i];
        }

        // Update weights for W_i, U_i, and b_i

        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights[index++] = W_i[i][j];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
               weights[index++] = U_i[i][j];
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            weights[index++] = b_i[i] ;
        }

        // Update weights for W_c, U_c, and b_c
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights[index++] = W_c[i][j] ;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights[index++] = U_c[i][j] ;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            weights[index++] = b_c[i] ;
        }

        // Update weights for W_o, U_o, and b_o

        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights[index++] = W_o[i][j] ;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights[index++] = U_o[i][j] ;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            weights[index++] =b_o[i] ;
        }

        // Update weights for V
        for (int i = 0; i < outputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
               weights[index++] =  V[i][j] ;
            }
        }

        // Update bias c for the output layer
        for (int i = 0; i < outputSize; i++) {
            weights[index++] = c[i];
        }
        return weights;
    }

    // Implement methods to set and get weights

    @Override
    public int countWeights() {
        return WEIGHT_COUNT;
    }

    @Override
    public String toString() {
        // Implement a string representation of the weights
        return Arrays.toString(getWeights());  // Update this
    }
}
