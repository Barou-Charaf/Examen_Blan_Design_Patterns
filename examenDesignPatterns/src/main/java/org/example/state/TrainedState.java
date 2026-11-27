package org.example.state;

public final class TrainedState implements NetworkState {
    @Override public String getName() { return "Trained"; }
    @Override public void checkPrediction() {}
}
