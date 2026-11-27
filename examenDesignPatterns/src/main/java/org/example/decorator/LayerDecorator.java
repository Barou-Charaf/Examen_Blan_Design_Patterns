package org.example.decorator;

import org.example.model.Layer;

public abstract class LayerDecorator extends Layer {
    protected final Layer wrapped;

    protected LayerDecorator(Layer wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    public Layer getWrappedLayer() { return wrapped; }

    @Override
    public Layer getBaseLayer() { return wrapped.getBaseLayer(); }

    @Override
    public double[] forward() { return wrapped.forward(); }
}
