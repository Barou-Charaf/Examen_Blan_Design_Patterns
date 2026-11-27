package org.example.adapter;

import org.example.legacy.LegacyActivation;
import org.example.strategy.ActivationFunction;

import java.util.Objects;

public class ActivationAdapter implements ActivationFunction {
    private final LegacyActivation legacy;

    public ActivationAdapter(LegacyActivation legacy) {
        this.legacy = Objects.requireNonNull(legacy, "L'ancienne fonction d'activation est obligatoire");
    }

    @Override
    public double activate(double value) {

        return legacy.calculate(value);
    }
}
