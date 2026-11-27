package org.example.legacy;

// Exemple d'une ancienne API, indépendante de l'interface ActivationFunction.
public class LegacySigmoid implements LegacyActivation {
    @Override
    public double calculate(double value) {
        return 1.0 / (1.0 + Math.exp(-value));
    }
}
