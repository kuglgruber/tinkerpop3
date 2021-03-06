package com.tinkerpop.blueprints.query.util;

import com.tinkerpop.blueprints.Contains;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.query.GraphQuery;

import java.util.Arrays;
import java.util.function.BiPredicate;

/**
 * For those graph engines that do not support the low-level querying of the vertices or edges, then
 * {@link DefaultGraphQuery} can be used. {@link DefaultGraphQuery} assumes, at minimum, that
 * {@link com.tinkerpop.blueprints.query.GraphQuery#vertices()} and
 * {@link com.tinkerpop.blueprints.query.GraphQuery#edges()} is implemented by the respective
 * {@link com.tinkerpop.blueprints.Graph}.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class DefaultGraphQuery extends DefaultQuery implements GraphQuery {

    public GraphQuery ids(final Object... ids) {
        this.hasContainers.add(new HasContainer(Property.Key.ID, Contains.IN, Arrays.asList(ids)));
        return this;
    }

    public GraphQuery has(final String key) {
        super.has(key);
        return this;
    }

    public GraphQuery hasNot(final String key) {
        super.hasNot(key);
        return this;
    }

    public GraphQuery has(final String key, final Object value) {
        super.has(key, value);
        return this;
    }

    public GraphQuery hasNot(final String key, final Object value) {
        super.hasNot(key, value);
        return this;
    }

    public GraphQuery has(final String key, final BiPredicate biPredicate, final Object value) {
        super.has(key, biPredicate, value);
        return this;
    }

    public <T extends Comparable<?>> GraphQuery interval(final String key, final T startValue, final T endValue) {
        super.interval(key, startValue, endValue);
        return this;
    }

    public GraphQuery limit(final int limit) {
        super.limit(limit);
        return this;
    }

    public abstract Iterable<Edge> edges();

    public abstract Iterable<Vertex> vertices();

}
