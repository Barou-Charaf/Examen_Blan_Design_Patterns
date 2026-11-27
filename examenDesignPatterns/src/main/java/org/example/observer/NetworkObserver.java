package org.example.observer;

@FunctionalInterface
public interface NetworkObserver {
    void update(String stateName);
}
