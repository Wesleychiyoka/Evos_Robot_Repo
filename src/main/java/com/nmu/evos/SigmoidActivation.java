package com.nmu.evos;

public class SigmoidActivation implements Activation {
    public double calculate(double x) {
        return 1 / (1 + Math.pow(Math.E, -x));
    }

    @Override
    public double derivative(double x) {
        return x * (1 - x);
    }

    @Override
    public String toString() {
        return SigmoidActivation.class.getCanonicalName();
    }
}
