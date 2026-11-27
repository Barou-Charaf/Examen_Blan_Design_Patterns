package org.example;

import org.example.model.HiddenLayer;
import org.example.model.InputLayer;
import org.example.model.NeuralNetwork;
import org.example.model.OutputLayer;
import org.example.strategy.Sigmoid;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(3));
        network.addLayer(new HiddenLayer(5, new Sigmoid()), new double[][]{
                {0.5, 0.5, 0.5},
                {0.5, 0.5, 0.5},
                {0.5, 0.5, 0.5},
                {0.5, 0.5, 0.5},
                {0.5, 0.5, 0.5}
        });
        network.addLayer(new OutputLayer(2, new Sigmoid()), new double[][]{
                {0.5, 0.5, 0.5, 0.5, 0.5},
                {0.5, 0.5, 0.5, 0.5, 0.5}
        });

        double[] input = {0.2, 0.4, 0.6};
        System.out.println("Entrée : " + Arrays.toString(input));
        System.out.println("Sortie : " + Arrays.toString(network.predict(input)));
    }
}
