package org.example.model;

import org.example.state.*;
import org.example.observer.NetworkObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeuralNetwork {
    private NetworkState state = new ConstructionState();
    private final List<NetworkObserver> observers = new ArrayList<>();
    private final InputLayer inputLayer;
    private final List<Layer> layers = new ArrayList<>();

    public NeuralNetwork(InputLayer inputLayer) {
        this.inputLayer = Objects.requireNonNull(inputLayer, "La couche d'entrée est obligatoire");
        layers.add(inputLayer);
    }

    // weights[destination][source] relie deux couches consécutives.
    public void addLayer(Layer layer, double[][] weights) {
        state.checkConstruction();
        Objects.requireNonNull(layer, "La couche est obligatoire");
        Objects.requireNonNull(weights, "Les poids sont obligatoires");
        Layer previous = layers.get(layers.size() - 1);
        if (layer instanceof InputLayer || previous instanceof OutputLayer || layers.contains(layer)) {
            throw new IllegalArgumentException("Ordre attendu : entrée, couches cachées, sortie");
        }
        if (layer.getNeurons().stream().anyMatch(n -> !n.getIncomingConnections().isEmpty())) {
            throw new IllegalArgumentException("La couche est déjà connectée");
        }
        if (weights.length != layer.size()) {
            throw new IllegalArgumentException("Une ligne de poids est nécessaire par neurone destination");
        }
        for (double[] row : weights) {
            if (row == null || row.length != previous.size()) {
                throw new IllegalArgumentException("Un poids est nécessaire par neurone source");
            }
            for (double weight : row) {
                Connection.validateWeight(weight);
            }
        }
        for (int destination = 0; destination < layer.size(); destination++) {
            for (int source = 0; source < previous.size(); source++) {
                layer.getNeurons().get(destination).connectFrom(
                        previous.getNeurons().get(source), weights[destination][source]);
            }
        }
        layers.add(layer);
    }

    public double[] predict(double[] input) {
        state.checkPrediction();
        if (!(layers.get(layers.size() - 1) instanceof OutputLayer)) {
            throw new IllegalStateException("Le réseau doit posséder une couche de sortie");
        }
        inputLayer.setInputs(input);
        double[] output = inputLayer.forward();
        for (int i = 1; i < layers.size(); i++) {
            output = layers.get(i).forward();
        }
        return output;
    }

    public String getStateName() {
        return state.getName();
    }

    public void addObserver(NetworkObserver observer) {
        Objects.requireNonNull(observer, "L'observateur est obligatoire");
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removeObserver(NetworkObserver observer) {
        observers.remove(observer);
    }

    private void setState(NetworkState nextState) {
        state = nextState;
        for (NetworkObserver observer : List.copyOf(observers)) {
            try {
                observer.update(state.getName());
            } catch (RuntimeException exception) {
                System.err.println("Notification impossible : " + exception.getMessage());
            }
        }
    }

    // L'algorithme d'apprentissage est fourni par l'appelant, pas simulé ici.
    public void train(Runnable trainingOperation) {
        Objects.requireNonNull(trainingOperation, "L'opération d'entraînement est obligatoire");
        state.checkTraining();
        if (!(layers.get(layers.size() - 1) instanceof OutputLayer)) {
            throw new IllegalStateException("Le réseau doit posséder une couche de sortie");
        }
        setState(new TrainingState());
        boolean completed = false;
        try {
            trainingOperation.run();
            completed = true;
        } finally {
            setState(completed ? new TrainedState() : new ConstructionState());
        }
    }

    public void fit(Runnable trainingOperation) {
        train(trainingOperation);
    }

    public List<Layer> getLayers() {
        return List.copyOf(layers);
    }
}
