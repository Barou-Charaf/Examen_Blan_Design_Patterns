package org.example.state;

public final class ConstructionState implements NetworkState {
    @Override public String getName() { return "Construction"; }
    @Override public void checkConstruction() {}
}
