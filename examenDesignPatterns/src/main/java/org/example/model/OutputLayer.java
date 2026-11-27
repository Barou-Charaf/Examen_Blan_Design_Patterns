package org.example.model;

import org.example.strategy.ActivationFunction;

public class OutputLayer extends Layer {
    public OutputLayer(int size, ActivationFunction activation) {
        super(size, activation);
    }
}
