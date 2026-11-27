package org.example;

import org.example.adapter.ActivationAdapter;
import org.example.legacy.LegacyActivation;
import org.example.legacy.LegacySigmoid;
import org.example.model.InputLayer;
import org.example.model.NeuralNetwork;
import org.example.model.OutputLayer;
import org.example.strategy.Identity;
import org.example.strategy.Sigmoid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivationAdapterTest {
    private static double[] predictAfterTraining(NeuralNetwork network, double[] input) {
        if (network.getStateName().equals("Construction")) {
            network.train(() -> {}); // Paramètres fixés par le test, aucun apprentissage.
        }
        return network.predict(input);
    }

    private static final double EPSILON = 1e-12;

    @Test
    void delegatesToTheProvidedLegacyFunctionWithoutChangingItsInputOrResult() {
        double[] received = new double[1];
        int[] calls = new int[1];
        LegacyActivation legacy = value -> {
            received[0] = value;
            calls[0]++;
            return 2 * value + 1;
        };

        assertEquals(-4, new ActivationAdapter(legacy).activate(-2.5), EPSILON);
        assertEquals(-2.5, received[0], EPSILON);
        assertEquals(1, calls[0]);
    }

    @Test
    void adaptsLegacySigmoidForPositiveNegativeAndZeroInputs() {
        ActivationAdapter adapter = new ActivationAdapter(new LegacySigmoid());
        assertEquals(0.5, adapter.activate(0), EPSILON);
        assertEquals(0.7310585786300049, adapter.activate(1), EPSILON);
        assertEquals(0.2689414213699951, adapter.activate(-1), EPSILON);
    }

    @Test
    void networkCanMixNativeAndAdaptedActivationsAndReplaceThem() {
        NeuralNetwork network = new NeuralNetwork(new InputLayer(2));
        OutputLayer output = new OutputLayer(2, new Sigmoid());
        output.getNeurons().get(0).setBias(0.1);
        output.getNeurons().get(1).setBias(0.1);
        output.getNeurons().get(1).setActivation(new ActivationAdapter(new LegacySigmoid()));
        network.addLayer(output, new double[][]{{0.5, 0.2}, {0.5, 0.2}});

        assertArrayEquals(new double[]{0.8455347349164652, 0.8455347349164652},
                predictAfterTraining(network, new double[]{2, 3}), EPSILON);

        output.getNeurons().get(1).setActivation(new Identity());
        assertArrayEquals(new double[]{0.8455347349164652, 1.7},
                predictAfterTraining(network, new double[]{2, 3}), EPSILON);
    }

    @Test
    void rejectsMissingLegacyFunction() {
        assertThrows(NullPointerException.class, () -> new ActivationAdapter(null));
    }
}
