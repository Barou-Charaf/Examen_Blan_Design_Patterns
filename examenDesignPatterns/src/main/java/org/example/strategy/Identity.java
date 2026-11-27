package org.example.strategy;

public class Identity implements ActivationFunction {
    @Override
    public double activate(double value) {
        return value;
    }
}
