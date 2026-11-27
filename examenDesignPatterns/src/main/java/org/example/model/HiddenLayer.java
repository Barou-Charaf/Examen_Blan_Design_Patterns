package org.example.model;

import org.example.strategy.ActivationFunction;

public class HiddenLayer extends Layer {
    public HiddenLayer(int size, ActivationFunction activation) {
        super(size, activation);
    }
}
