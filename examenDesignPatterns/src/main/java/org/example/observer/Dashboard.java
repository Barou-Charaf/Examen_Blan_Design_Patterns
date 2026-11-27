package org.example.observer;

import java.util.ArrayList;
import java.util.List;

public class Dashboard implements NetworkObserver {
    private final List<String> history = new ArrayList<>();

    @Override
    public void update(String stateName) {
        history.add(stateName);
        System.out.println("Tableau de bord : " + stateName);
    }

    public List<String> getHistory() {
        return List.copyOf(history);
    }
}
