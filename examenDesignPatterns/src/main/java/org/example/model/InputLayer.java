package org.example.model;

import org.example.strategy.Identity;

import java.util.Objects;

public class InputLayer extends Layer {
    public InputLayer(int size) {
        super(size, new Identity());
    }

    public void setInputs(double[] inputs) {
        Objects.requireNonNull(inputs, "Le vecteur d'entrée est obligatoire");
        if (inputs.length != size()) {
            throw new IllegalArgumentException("La taille du vecteur doit être " + size());
        }
        for (double input : inputs) {
            if (!Double.isFinite(input)) {
                throw new IllegalArgumentException("Les entrées doivent être finies");
            }
        }
        for (int i = 0; i < size(); i++) {
            getNeurons().get(i).setOutput(inputs[i]);
        }
    }

    @Override
    public double[] forward() {
        return getNeurons().stream().mapToDouble(Neuron::getOutput).toArray();
    }
}
