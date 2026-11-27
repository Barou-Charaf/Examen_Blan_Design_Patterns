package org.example;

import org.example.decorator.TimingDecorator;
import org.example.model.*;
import org.example.strategy.Identity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DecoratorTest {
    @org.junit.jupiter.api.BeforeEach
    void authorize() { org.example.security.SecurityContext.setRole(org.example.security.Role.ADMIN); }

    @org.junit.jupiter.api.AfterEach
    void clearAuthorization() { org.example.security.SecurityContext.clear(); }

    @Test void decoratesAllLayerTypesAndPreservesConnections() {
        InputLayer input = new InputLayer(1);
        TimingDecorator timedInput = new TimingDecorator(input);
        NeuralNetwork network = new NeuralNetwork(timedInput);
        HiddenLayer hidden = new HiddenLayer(1, new Identity());
        network.addLayer(new TimingDecorator(hidden), new double[][]{{0.5}});
        OutputLayer output = new OutputLayer(1, new Identity());
        TimingDecorator timedOutput = new TimingDecorator(output);
        network.addLayer(timedOutput, new double[][]{{1}});
        network.train(() -> {});
        assertArrayEquals(new double[]{2}, network.predict(new double[]{4}));
        TimingDecorator nested = new TimingDecorator(timedOutput);
        network.decorateLayer(nested);
        assertArrayEquals(new double[]{3}, network.predict(new double[]{6}));
        assertSame(output.getNeurons(), nested.getNeurons());
        assertSame(hidden.getNeurons().get(0), output.getNeurons().get(0)
                .getIncomingConnections().get(0).getSource());
        assertTrue(timedInput.getLastDurationNanos() >= 0);
        assertTrue(nested.getLastDurationNanos() >= 0);
    }

    @Test void decoratedLayersKeepTopologyAndStateConstraints() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(1));
        assertThrows(IllegalArgumentException.class, () -> network.addLayer(
                new TimingDecorator(new InputLayer(1)), new double[][]{{1}}));
        TimingDecorator output = new TimingDecorator(new OutputLayer(1, new Identity()));
        network.addLayer(output, new double[][]{{1}});
        assertThrows(IllegalArgumentException.class, () -> network.addLayer(
                new HiddenLayer(1, new Identity()), new double[][]{{1}}));
        network.train(() -> assertThrows(IllegalStateException.class,
                () -> network.decorateLayer(new TimingDecorator(output))));
        assertThrows(IllegalArgumentException.class, () -> network.decorateLayer(
                new TimingDecorator(new OutputLayer(1, new Identity()))));
        assertThrows(NullPointerException.class, () -> new TimingDecorator(null));
    }

    @Test void forwardsOnceAndPreservesExceptions() {
        int[] calls = {0};
        RuntimeException failure = new RuntimeException("Échec du calcul");
        Layer layer = new HiddenLayer(1, new Identity()) {
            @Override public double[] forward() {
                calls[0]++;
                throw failure;
            }
        };
        TimingDecorator timed = new TimingDecorator(layer);
        assertSame(failure, assertThrows(RuntimeException.class, timed::forward));
        assertEquals(1, calls[0]);
        assertTrue(timed.getLastDurationNanos() >= 0);
    }
}
