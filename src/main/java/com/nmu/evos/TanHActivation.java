package com.nmu.evos;

public class TanHActivation implements Activation {
    @Override
    public double calculate(double x) {
        double ex = Math.exp(x);
        double emx = Math.exp(-x);
        return (ex - emx) / (ex + emx);
    }

    @Override
    public double derivative(double x) {
        return 0;
    }

    @Override
    public String toString() {
        return TanHActivation.class.getCanonicalName();
    }
}
