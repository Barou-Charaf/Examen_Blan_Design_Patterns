package org.example.state;

public interface NetworkState {
    String getName();
    default void checkPrediction() {
        throw new IllegalStateException("La prédiction nécessite un réseau entraîné");
    }
    default void checkTraining() {}
    default void checkConstruction() {
        throw new IllegalStateException("La structure ne peut changer qu'en Construction");
    }
}
