package com.nmu.evos;

public class LinearActivation implements Activation {
    @Override
    public double calculate(double x) {
        return x;
    }

    @Override
    public double derivative(double x) {
        return 1;
    }

    @Override
    public String toString() {
        return LinearActivation.class.getCanonicalName();
    }
}
