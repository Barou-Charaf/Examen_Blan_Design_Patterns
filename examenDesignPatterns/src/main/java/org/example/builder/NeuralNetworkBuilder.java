package org.example.builder;

import org.example.model.*;
import org.example.security.RequiresRole;
import org.example.security.Role;
import org.example.strategy.ActivationFunction;

import java.util.Arrays;

public class NeuralNetworkBuilder {
    private NeuralNetwork network;
    private boolean outputAdded;
    private boolean built;

    @RequiresRole(Role.ADMIN)
    public NeuralNetworkBuilder addInputLayer(int size) {
        checkNotBuilt();
        if (network != null) {
            throw new IllegalStateException("La couche d'entrée est déjà définie");
        }
        network = new NeuralNetwork(new InputLayer(size));
        return this;
    }

    @RequiresRole(Role.ADMIN)
    public NeuralNetworkBuilder addHiddenLayer(int size, ActivationFunction activation, double[][] weights) {
        checkCanAddLayer();
        network.addLayer(new HiddenLayer(size, activation), weights);
        return this;
    }

    @RequiresRole(Role.ADMIN)
    public NeuralNetworkBuilder addHiddenLayer(int size, ActivationFunction activation, double weight) {
        return addHiddenLayer(size, activation, uniformWeights(size, weight));
    }

    @RequiresRole(Role.ADMIN)
    public NeuralNetworkBuilder addOutputLayer(int size, ActivationFunction activation, double[][] weights) {
        checkCanAddLayer();
        network.addLayer(new OutputLayer(size, activation), weights);
        outputAdded = true;
        return this;
    }

    @RequiresRole(Role.ADMIN)
    public NeuralNetworkBuilder addOutputLayer(int size, ActivationFunction activation, double weight) {
        return addOutputLayer(size, activation, uniformWeights(size, weight));
    }

    @RequiresRole(Role.ADMIN)
    public NeuralNetwork build() {
        checkNotBuilt();
        if (network == null || !outputAdded) {
            throw new IllegalStateException("Une couche d'entrée et une couche de sortie sont nécessaires");
        }
        built = true;
        return network;
    }

    private double[][] uniformWeights(int size, double weight) {
        checkCanAddLayer();
        if (size <= 0) {
            throw new IllegalArgumentException("Une couche doit contenir au moins un neurone");
        }
        if (!Double.isFinite(weight) || weight < 0 || weight > 1) {
            throw new IllegalArgumentException("Le poids doit être compris entre 0 et 1");
        }
        var layers = network.getLayers();
        int sourceSize = layers.get(layers.size() - 1).size();
        double[][] weights = new double[size][sourceSize];
        for (double[] row : weights) {
            Arrays.fill(row, weight);
        }
        return weights;
    }

    private void checkCanAddLayer() {
        checkNotBuilt();
        if (network == null) {
            throw new IllegalStateException("Définir d'abord la couche d'entrée");
        }
        if (outputAdded) {
            throw new IllegalStateException("La couche de sortie doit rester la dernière");
        }
    }

    private void checkNotBuilt() {
        if (built) {
            throw new IllegalStateException("Ce builder a déjà construit son réseau");
        }
    }
}
