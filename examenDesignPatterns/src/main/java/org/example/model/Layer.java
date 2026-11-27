package org.example.model;

import org.example.strategy.ActivationFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Layer {
    private final List<Neuron> neurons;

    protected Layer(int size, ActivationFunction activation) {
        if (size <= 0) {
            throw new IllegalArgumentException("Une couche doit contenir au moins un neurone");
        }
        Objects.requireNonNull(activation, "La fonction d'activation est obligatoire");
        List<Neuron> createdNeurons = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            createdNeurons.add(new Neuron(0, activation));
        }
        neurons = List.copyOf(createdNeurons);
    }

    public double[] forward() {
        double[] outputs = new double[size()];
        for (int i = 0; i < size(); i++) {
            outputs[i] = neurons.get(i).compute();
        }
        return outputs;
    }

    public int size() {
        return neurons.size();
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }
}
