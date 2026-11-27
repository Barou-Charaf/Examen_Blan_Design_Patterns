package org.example;

import org.example.observer.Dashboard;
import org.example.decorator.TimingDecorator;
import org.example.adapter.ActivationAdapter;
import org.example.legacy.LegacySigmoid;
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
        OutputLayer outputLayer = new OutputLayer(2, new Sigmoid());
        outputLayer.getNeurons().get(1).setActivation(new ActivationAdapter(new LegacySigmoid()));
        network.addLayer(outputLayer, new double[][]{
                {0.5, 0.5, 0.5, 0.5, 0.5},
                {0.5, 0.5, 0.5, 0.5, 0.5}
        });

        network.addObserver(new Dashboard());
        network.fit(() -> System.out.println("Démonstration du cycle de vie, sans apprentissage numérique"));

        TimingDecorator timedOutput = new TimingDecorator(outputLayer);
        network.decorateLayer(timedOutput);

        double[] input = {0.2, 0.4, 0.6};
        System.out.println("Entrée : " + Arrays.toString(input));
        System.out.println("Sortie (Sigmoid, ancienne Sigmoid adaptée) : "
                + Arrays.toString(network.predict(input)));
        System.out.println("Durée de la couche de sortie : " + timedOutput.getLastDurationNanos() + " ns");
    }
}
