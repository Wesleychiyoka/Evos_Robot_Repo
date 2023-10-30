package com.nmu.evos;

public interface Activation {
    double calculate(double x);
    double derivative(double x);
    default <T> void set(T t) {
        throw new UnsupportedOperationException("Method not implemented.");
    }
}
