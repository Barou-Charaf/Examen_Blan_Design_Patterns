package org.example;

import org.example.model.*;
import org.example.security.*;
import org.example.strategy.Identity;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
import static org.junit.jupiter.api.Assertions.*;

class AopTest {
    @AfterEach void clearContext() { SecurityContext.clear(); }

    private NeuralNetwork network() {
        SecurityContext.setRole(Role.ADMIN);
        NeuralNetwork network = new NeuralNetwork(new InputLayer(1));
        network.addLayer(new OutputLayer(1, new Identity()), new double[][]{{1}});
        return network;
    }

    @Test void anonymousAccessIsDeniedBeforeOperation() {
        NeuralNetwork network = network();
        SecurityContext.clear();
        assertThrows(SecurityException.class, () -> network.predict(new double[]{1}));
        assertThrows(SecurityException.class, () -> network.train(() -> fail("Ne doit pas être appelé")));
        SecurityContext.setRole(Role.ADMIN);
        assertEquals("Construction", network.getStateName());
    }

    @Test void userCanPredictButCannotTrainOrReadMutableLayers() {
        NeuralNetwork network = network();
        network.fit(() -> {});
        SecurityContext.setRole(Role.USER);
        assertArrayEquals(new double[]{2}, network.predict(new double[]{2}));
        assertThrows(SecurityException.class, () -> network.fit(() -> fail("Interdit")));
        assertThrows(SecurityException.class, network::getLayers);
        assertEquals("Trained", network.getStateName());
    }

    @Test void securityDoesNotReplaceStateValidation() {
        NeuralNetwork network = network();
        SecurityContext.setRole(Role.USER);
        assertThrows(IllegalStateException.class, () -> network.predict(new double[]{1}));
    }

    @Test void loggingInterceptsInternalCallsAndDeniedOperations() {
        List<String> messages = new ArrayList<>();
        Logger logger = Logger.getLogger("org.example.audit");
        Handler handler = new Handler() {
            public void publish(LogRecord record) { messages.add(record.getMessage()); }
            public void flush() {}
            public void close() {}
        };
        logger.addHandler(handler);
        try {
            NeuralNetwork network = network();
            network.fit(() -> {});
            SecurityContext.clear();
            assertThrows(SecurityException.class, () -> network.predict(new double[]{1}));
            assertTrue(messages.contains("ENTREE NeuralNetwork.train(..)"));
            assertTrue(messages.contains("SUCCES NeuralNetwork.setState(..)"));
            assertTrue(messages.contains("ECHEC NeuralNetwork.predict(..) : SecurityException"));
            assertTrue(messages.stream().anyMatch(m -> m.startsWith("DUREE NeuralNetwork.predict(..)")));
        } finally {
            logger.removeHandler(handler);
        }
    }
}
