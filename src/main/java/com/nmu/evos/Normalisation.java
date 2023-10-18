package com.nmu.evos;

import java.util.function.Function;

public class Normalisation {
    public boolean normalise = false;
    public boolean de_normalise = false;
    private final Function<double[], double[]> normaliser;
    private final Function<double[], double[]> de_normaliser;
    public Normalisation(Function<double[], double[]> normaliser, Function<double[], double[]> de_normaliser) {
        this.normaliser = normaliser;
        this.de_normaliser = de_normaliser;
    }
    public double[] c_norm(double[] input) {
        return normalise ? normaliser.apply(input) : input;
    }
    public double[] c_de_norm(double[] input) {
        return de_normalise ? de_normaliser.apply(input) : input;
    }
    public static double normalise(double x, double dl, double dh, double nl, double nh) {
        return (x - dl) * (nh - nl) / (dh - dl) + nl;
    }
    public static double de_normalise(double x, double dl, double dh, double nl, double nh) {
        return ((dl - dh) * x - nh * dl + dh * nl) / (nl - nh);
    }
    public static double[] normalise(double[] x, double dl, double dh, double nl, double nh) {
        double[] x_normalised = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            x_normalised[i] = (x[i] - dl) * (nh - nl) / (dh - dl) + nl;
        }
        return x_normalised;
    }
    public static double[] de_normalise(double[] x, double dl, double dh, double nl, double nh) {
        double[] x_normalised = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            x_normalised[i] = ((dl - dh) * x[i] - nh * dl + dh * nl) / (nl - nh);
        }
        return x_normalised;
    }
}
