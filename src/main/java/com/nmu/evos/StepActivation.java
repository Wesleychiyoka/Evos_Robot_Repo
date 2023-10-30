package com.nmu.evos;

public class StepActivation implements Activation {
    public double calculate(double x) {
        if (x >= 0) return 1;
        else return 0;
    }

    @Override
    public double derivative(double x) {
        return 1;
    }

    @Override
    public String toString() {
        return StepActivation.class.getCanonicalName();
    }
}
