package org.example;

import org.example.model.Connection;
import org.example.model.HiddenLayer;
import org.example.model.InputLayer;
import org.example.model.NeuralNetwork;
import org.example.model.Neuron;
import org.example.model.OutputLayer;
import org.example.strategy.Identity;
import org.example.strategy.Sigmoid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NeuralNetworkTest {
    private static final double EPSILON = 1e-12;

    @Test
    void activationFunctionsHandleReferenceValues() {
        assertEquals(-2.5, new Identity().activate(-2.5));
        Sigmoid sigmoid = new Sigmoid();
        assertEquals(0.5, sigmoid.activate(0), EPSILON);
        assertEquals(0.7310585786300049, sigmoid.activate(1), EPSILON);
        assertEquals(0, sigmoid.activate(-1000), EPSILON);
        assertEquals(1, sigmoid.activate(1000), EPSILON);
    }

    @Test
    void neuronAddsBiasAndCanChangeActivation() {
        Neuron first = new Neuron(0, new Identity());
        Neuron second = new Neuron(0, new Identity());
        first.setOutput(2);
        second.setOutput(3);
        Neuron neuron = new Neuron(0.1, new Identity());
        Connection connection = neuron.connectFrom(first, 0.5);
        neuron.connectFrom(second, 0.2);

        assertSame(first, connection.getSource());
        assertSame(neuron, connection.getDestination());
        assertEquals(1.7, neuron.compute(), EPSILON);

        neuron.setActivation(new Sigmoid());
        assertEquals(0.8455347349164652, neuron.compute(), EPSILON);
    }

    @Test
    void inputLayerPassesValuesWithoutApplyingBiasOrActivation() {
        InputLayer input = new InputLayer(2);
        input.getNeurons().get(0).setBias(10);
        input.getNeurons().get(0).setActivation(new Sigmoid());
        input.setInputs(new double[]{-2, 3});
        assertArrayEquals(new double[]{-2, 3}, input.forward(), EPSILON);
    }

    @Test
    void predictionMatchesManualCalculationAndRefreshesEveryNeuron() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(2));
        HiddenLayer hidden = new HiddenLayer(2, new Identity());
        hidden.getNeurons().get(0).setBias(0.1);
        hidden.getNeurons().get(1).setBias(-0.2);
        network.addLayer(hidden, new double[][]{{0.5, 0.2}, {0.1, 0.4}});
        OutputLayer output = new OutputLayer(1, new Sigmoid());
        output.getNeurons().get(0).setBias(0.05);
        network.addLayer(output, new double[][]{{0.7, 0.3}});

        // Entrée [2, 3] -> couche cachée [1.7, 1.2] -> sigmoid(1.6).
        assertArrayEquals(new double[]{0.8320183851339245}, network.predict(new double[]{2, 3}), EPSILON);
        // Entrée [0, 0] -> couche cachée [0.1, -0.2] -> sigmoid(0.06).
        assertArrayEquals(new double[]{0.51499550161941}, network.predict(new double[]{0, 0}), EPSILON);
    }

    @Test
    void examTopologyHasTwentyFiveConnectionsAndTwoOutputs() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(3));
        network.addLayer(new HiddenLayer(5, new Identity()), new double[][]{
                {1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {0.5, 0.5, 0}, {0, 0.5, 0.5}
        });
        network.addLayer(new OutputLayer(2, new Identity()), new double[][]{
                {1, 0, 0, 0.5, 0}, {0, 0, 1, 0, 0.5}
        });
        int connections = network.getLayers().stream()
                .flatMap(layer -> layer.getNeurons().stream())
                .mapToInt(neuron -> neuron.getIncomingConnections().size()).sum();

        assertEquals(25, connections);
        assertArrayEquals(new double[]{1.75, 4.25}, network.predict(new double[]{1, 2, 3}), EPSILON);
    }

    @Test
    void propagationSupportsSeveralHiddenLayers() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(1));
        network.addLayer(new HiddenLayer(1, new Identity()), new double[][]{{0.5}});
        network.addLayer(new HiddenLayer(1, new Identity()), new double[][]{{0.4}});
        network.addLayer(new OutputLayer(1, new Identity()), new double[][]{{0.3}});
        assertArrayEquals(new double[]{0.12}, network.predict(new double[]{2}), EPSILON);
    }

    @Test
    void invalidWeightMatrixLeavesLayerUnconnected() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(2));
        OutputLayer output = new OutputLayer(2, new Identity());
        assertThrows(IllegalArgumentException.class,
                () -> network.addLayer(output, new double[][]{{0.5, 0.5}, {0.5, 1.1}}));
        assertEquals(1, network.getLayers().size());
        assertTrue(output.getNeurons().stream().allMatch(n -> n.getIncomingConnections().isEmpty()));
        assertThrows(IllegalArgumentException.class,
                () -> network.addLayer(output, new double[][]{{0.5}, {0.5}}));
        network.addLayer(output, new double[][]{{1, 0}, {0, 1}});
        assertArrayEquals(new double[]{2, 3}, network.predict(new double[]{2, 3}), EPSILON);
    }

    @Test
    void rejectsInvalidTopologyAndInput() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(1));
        assertThrows(IllegalStateException.class, () -> network.predict(new double[]{1}));
        assertThrows(IllegalArgumentException.class,
                () -> network.addLayer(new InputLayer(1), new double[][]{{1}}));
        network.addLayer(new OutputLayer(1, new Identity()), new double[][]{{1}});
        assertThrows(IllegalArgumentException.class, () -> network.predict(new double[]{1, 2}));
        assertThrows(IllegalArgumentException.class, () -> network.predict(new double[]{Double.NaN}));
        assertThrows(IllegalArgumentException.class,
                () -> network.addLayer(new HiddenLayer(1, new Identity()), new double[][]{{1}}));
        assertThrows(IllegalArgumentException.class, () -> new InputLayer(0));
    }

    @Test
    void weightsRespectExamBoundsAndCanBeUpdated() {
        Neuron source = new Neuron(0, new Identity());
        source.setOutput(2);
        Neuron destination = new Neuron(0, new Identity());
        Connection connection = destination.connectFrom(source, 0);
        assertEquals(0, destination.compute(), EPSILON);
        connection.setWeight(1);
        assertEquals(2, destination.compute(), EPSILON);
        for (double invalid : new double[]{-0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> connection.setWeight(invalid));
        }
        assertEquals(1, connection.getWeight());
    }
}
