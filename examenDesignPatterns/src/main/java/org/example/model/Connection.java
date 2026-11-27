package org.example.model;

import java.util.Objects;

public class Connection {
    private final Neuron source;
    private final Neuron destination;
    private double weight;

    public Connection(Neuron source, Neuron destination, double weight) {
        this.source = Objects.requireNonNull(source, "Le neurone source est obligatoire");
        this.destination = Objects.requireNonNull(destination, "Le neurone destination est obligatoire");
        setWeight(weight);
    }

    public double getWeightedInput() {
        return source.getOutput() * weight;
    }

    public Neuron getSource() {
        return source;
    }

    public Neuron getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        validateWeight(weight);
        this.weight = weight;
    }

    static void validateWeight(double weight) {
        if (!Double.isFinite(weight) || weight < 0 || weight > 1) {
            throw new IllegalArgumentException("Le poids doit être compris entre 0 et 1");
        }
    }
}
