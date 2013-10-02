package com.tinkerpop.gremlin.pipes;

import com.tinkerpop.gremlin.pipes.util.Holder;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class AbstractPipe<S, E> implements Pipe<S, E> {

    protected final Pipeline pipeline;
    protected ExpandableIterator<Holder<S>> starts;
    private Holder<E> nextEnd;
    protected Holder<E> currentEnd;
    protected boolean available;

    public <P extends Pipeline> AbstractPipe(P pipeline) {
        this.pipeline = pipeline;
    }

    public Pipe setStarts(final Iterator<Holder<S>> starts) {
        this.starts = new ExpandableIterator<>(starts);
        return this;
    }

    public void addStart(final Holder<S> start) {
        this.starts.add(start);
    }

    public Holder<E> next() {
        if (this.available) {
            this.available = false;
            return (this.currentEnd = this.nextEnd);
        } else {
            return (this.currentEnd = this.processNextStart());
        }
    }

    public boolean hasNext() {
        if (this.available)
            return true;
        else {
            try {
                this.nextEnd = this.processNextStart();
                return (this.available = true);
            } catch (final NoSuchElementException e) {
                return (this.available = false);
            }
        }
    }

    public <P extends Pipeline> P getPipeline() {
        return (P) this.pipeline;
    }

    protected abstract Holder<E> processNextStart() throws NoSuchElementException;


}