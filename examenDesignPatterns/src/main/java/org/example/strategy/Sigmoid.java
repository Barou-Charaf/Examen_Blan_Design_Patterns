package org.example.strategy;

public class Sigmoid implements ActivationFunction {
    @Override
    public double activate(double value) {
        if (value >= 0) {
            return 1.0 / (1.0 + Math.exp(-value));
        }
        double exponential = Math.exp(value);
        return exponential / (1.0 + exponential);
    }
}
