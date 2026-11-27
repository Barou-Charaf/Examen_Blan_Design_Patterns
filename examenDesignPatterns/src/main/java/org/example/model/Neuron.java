package org.example.model;

import org.example.strategy.ActivationFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Neuron {
    private double bias;
    private double output;
    private ActivationFunction activation;
    private final List<Connection> incomingConnections = new ArrayList<>();

    public Neuron(double bias, ActivationFunction activation) {
        setBias(bias);
        setActivation(activation);
    }

    public double compute() {
        double sum = bias;
        for (Connection connection : incomingConnections) {
            sum += connection.getWeightedInput();
        }
        output = activation.activate(sum);
        return output;
    }

    public Connection connectFrom(Neuron source, double weight) {
        Connection connection = new Connection(source, this, weight);
        incomingConnections.add(connection);
        return connection;
    }

    public List<Connection> getIncomingConnections() {
        return List.copyOf(incomingConnections);
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        if (!Double.isFinite(bias)) {
            throw new IllegalArgumentException("Le biais doit être fini");
        }
        this.bias = bias;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        if (!Double.isFinite(output)) {
            throw new IllegalArgumentException("La valeur du neurone doit être finie");
        }
        this.output = output;
    }

    public void setActivation(ActivationFunction activation) {
        this.activation = Objects.requireNonNull(activation, "La fonction d'activation est obligatoire");
    }
}
