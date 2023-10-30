package com.nmu.evos;

public interface NeuralNetwork {
    double[] fire(double[] z, Activation hidden_layer_activation_function, Activation output_layer_activation_function);

    void setWeights(double[] weights);

    double[] getWeights();

    int countWeights();
}
