package org.example.strategy;

@FunctionalInterface
public interface ActivationFunction {
    double activate(double value);
}
