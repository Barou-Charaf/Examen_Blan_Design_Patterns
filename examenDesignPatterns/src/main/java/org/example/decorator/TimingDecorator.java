package org.example.decorator;

import org.example.model.Layer;

public class TimingDecorator extends LayerDecorator {
    private long lastDurationNanos;

    public TimingDecorator(Layer wrapped) {
        super(wrapped);
    }

    @Override
    public double[] forward() {
        long start = System.nanoTime();
        try {
            return super.forward();
        } finally {
            lastDurationNanos = System.nanoTime() - start;
        }
    }

    public long getLastDurationNanos() { return lastDurationNanos; }
}
