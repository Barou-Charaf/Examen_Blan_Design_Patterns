package org.example.state;

public final class TrainingState implements NetworkState {
    @Override public String getName() { return "Training"; }
    @Override public void checkTraining() {
        throw new IllegalStateException("Un entraînement est déjà en cours");
    }
}
