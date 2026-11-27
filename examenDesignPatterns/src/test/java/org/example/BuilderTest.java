package org.example;

import org.example.builder.NeuralNetworkBuilder;
import org.example.model.NeuralNetwork;
import org.example.security.*;
import org.example.strategy.Identity;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {
    @BeforeEach void authorize() { SecurityContext.setRole(Role.ADMIN); }
    @AfterEach void clear() { SecurityContext.clear(); }

    @Test void buildsFullyConnectedNetworkInConstructionState() {
        NeuralNetwork network = new NeuralNetworkBuilder().addInputLayer(3)
                .addHiddenLayer(5, new Identity(), 0.5)
                .addOutputLayer(2, new Identity(), 0.5).build();
        assertEquals("Construction", network.getStateName());
        assertEquals(25, network.getLayers().stream().flatMap(l -> l.getNeurons().stream())
                .mapToInt(n -> n.getIncomingConnections().size()).sum());
        assertThrows(IllegalStateException.class, () -> network.predict(new double[]{1, 2, 3}));
        network.train(() -> {});
        assertArrayEquals(new double[]{7.5, 7.5}, network.predict(new double[]{1, 2, 3}));
    }

    @Test void supportsExplicitWeightsWithoutHiddenLayerAndCopiesValues() {
        double[][] weights = {{0.5, 0.2}};
        NeuralNetwork network = new NeuralNetworkBuilder().addInputLayer(2)
                .addOutputLayer(1, new Identity(), weights).build();
        weights[0][0] = 0;
        network.train(() -> {});
        assertArrayEquals(new double[]{1.6}, network.predict(new double[]{2, 3}), 1e-12);
    }

    @Test void rejectsIncompleteOutOfOrderAndRepeatedConstruction() {
        NeuralNetworkBuilder builder = new NeuralNetworkBuilder();
        assertThrows(IllegalStateException.class, builder::build);
        assertThrows(IllegalStateException.class, () -> builder.addHiddenLayer(1, new Identity(), 0.5));
        builder.addInputLayer(1);
        assertThrows(IllegalStateException.class, builder::build);
        assertThrows(IllegalStateException.class, () -> builder.addInputLayer(2));
        builder.addOutputLayer(1, new Identity(), 1);
        assertThrows(IllegalStateException.class, () -> builder.addHiddenLayer(1, new Identity(), 1));
        builder.build();
        assertThrows(IllegalStateException.class, builder::build);
        assertThrows(IllegalStateException.class, () -> builder.addInputLayer(1));
    }

    @Test void invalidLayerCanBeCorrectedBeforeBuilding() {
        NeuralNetworkBuilder builder = new NeuralNetworkBuilder().addInputLayer(2);
        assertThrows(IllegalArgumentException.class, () -> builder.addHiddenLayer(0, new Identity(), 0.5));
        assertThrows(IllegalArgumentException.class, () -> builder.addOutputLayer(1, new Identity(), Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> builder.addOutputLayer(1, new Identity(), new double[][]{{1}}));
        assertEquals(2, builder.addOutputLayer(1, new Identity(), 1).build().getLayers().size());
    }

    @Test void buildingRequiresAdminThroughTheSecurityAspect() {
        NeuralNetworkBuilder builder = new NeuralNetworkBuilder().addInputLayer(1)
                .addOutputLayer(1, new Identity(), 1);
        SecurityContext.setRole(Role.USER);
        assertThrows(SecurityException.class, builder::build);
        SecurityContext.clear();
        assertThrows(SecurityException.class, () -> new NeuralNetworkBuilder().addInputLayer(1));
        SecurityContext.setRole(Role.ADMIN);
        assertEquals("Construction", builder.build().getStateName());
    }
}
