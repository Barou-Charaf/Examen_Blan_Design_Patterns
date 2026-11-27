package org.example;

import org.example.model.*;
import org.example.observer.*;
import org.example.strategy.Identity;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StateObserverTest {
    @org.junit.jupiter.api.BeforeEach
    void authorize() { org.example.security.SecurityContext.setRole(org.example.security.Role.ADMIN); }

    @org.junit.jupiter.api.AfterEach
    void clearAuthorization() { org.example.security.SecurityContext.clear(); }

    private NeuralNetwork network() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(1));
        network.addLayer(new OutputLayer(1, new Identity()), new double[][]{{1}});
        return network;
    }

    @Test void guardsOperationsAndNotifiesTransitions() {
        NeuralNetwork network = network();
        Dashboard dashboard = new Dashboard();
        network.addObserver(dashboard);
        network.addObserver(dashboard);
        assertEquals("Construction", network.getStateName());
        assertThrows(IllegalStateException.class, () -> network.predict(new double[]{2}));
        network.fit(() -> {
            assertEquals("Training", network.getStateName());
            assertThrows(IllegalStateException.class, () -> network.predict(new double[]{2}));
            assertThrows(IllegalStateException.class, () -> network.train(() -> {}));
            assertThrows(IllegalStateException.class,
                    () -> network.addLayer(new HiddenLayer(1, new Identity()), new double[][]{{1}}));
            network.getLayers().get(1).getNeurons().get(0).setBias(1);
        });
        assertEquals("Trained", network.getStateName());
        assertArrayEquals(new double[]{3}, network.predict(new double[]{2}));
        assertEquals(List.of("Training", "Trained"), dashboard.getHistory());
        network.removeObserver(dashboard);
        network.train(() -> {});
        assertEquals(2, dashboard.getHistory().size());
    }

    @Test void failedTrainingReturnsToConstructionAndPropagatesFailure() {
        NeuralNetwork network = network();
        Dashboard dashboard = new Dashboard();
        network.addObserver(dashboard);
        RuntimeException failure = new RuntimeException("Échec apprentissage");
        assertSame(failure, assertThrows(RuntimeException.class,
                () -> network.train(() -> { throw failure; })));
        assertEquals("Construction", network.getStateName());
        assertEquals(List.of("Training", "Construction"), dashboard.getHistory());
        assertThrows(IllegalStateException.class, () -> network.predict(new double[]{1}));
        network.train(() -> {});
        assertEquals("Trained", network.getStateName());
    }

    @Test void failingObserverDoesNotPreventTrainingOrOtherNotifications() {
        NeuralNetwork network = network();
        network.addObserver(state -> { throw new IllegalStateException("Dashboard indisponible"); });
        Dashboard dashboard = new Dashboard();
        network.addObserver(dashboard);
        network.train(() -> {});
        assertEquals(List.of("Training", "Trained"), dashboard.getHistory());
        assertEquals("Trained", network.getStateName());
    }

    @Test void invalidTrainingDoesNotChangeState() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(1));
        assertThrows(IllegalStateException.class, () -> network.train(() -> {}));
        assertThrows(NullPointerException.class, () -> network.train(null));
        assertEquals("Construction", network.getStateName());
    }
}
