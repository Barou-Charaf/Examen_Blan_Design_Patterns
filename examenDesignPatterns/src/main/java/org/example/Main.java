package org.example;

import org.example.observer.Dashboard;
import org.example.security.Role;
import org.example.security.SecurityContext;
import org.example.decorator.TimingDecorator;
import org.example.adapter.ActivationAdapter;
import org.example.legacy.LegacySigmoid;
import org.example.model.NeuralNetwork;
import org.example.builder.NeuralNetworkBuilder;
import org.example.model.OutputLayer;
import org.example.strategy.Sigmoid;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        SecurityContext.setRole(Role.ADMIN);
        try {
            NeuralNetwork network = new NeuralNetworkBuilder()
                    .addInputLayer(3)
                    .addHiddenLayer(5, new Sigmoid(), 0.5)
                    .addOutputLayer(2, new Sigmoid(), 0.5)
                    .build();
            OutputLayer outputLayer = (OutputLayer) network.getLayers().get(2);
            outputLayer.getNeurons().get(1).setActivation(new ActivationAdapter(new LegacySigmoid()));

            network.addObserver(new Dashboard());
            network.fit(() -> System.out.println("Démonstration du cycle de vie, sans apprentissage numérique"));

            TimingDecorator timedOutput = new TimingDecorator(outputLayer);
            network.decorateLayer(timedOutput);

            SecurityContext.setRole(Role.USER);
            double[] input = {0.2, 0.4, 0.6};
            System.out.println("Entrée : " + Arrays.toString(input));
            System.out.println("Sortie (Sigmoid, ancienne Sigmoid adaptée) : "
                    + Arrays.toString(network.predict(input)));
            System.out.println("Durée de la couche de sortie : " + timedOutput.getLastDurationNanos() + " ns");
            try {
                network.train(() -> {});
            } catch (SecurityException denied) {
                System.out.println(denied.getMessage());
            }
        } finally {
            SecurityContext.clear();
        }
    }
}
