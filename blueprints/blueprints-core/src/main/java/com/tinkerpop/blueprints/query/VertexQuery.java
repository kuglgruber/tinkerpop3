package com.tinkerpop.blueprints.query;

import com.tinkerpop.blueprints.Compare;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import java.util.function.BiPredicate;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Luca Garulli (http://www.orientechnologies.com)
 * @author Daniel Kuppitz (daniel.kuppitz@shoproach.com)
 */
public interface VertexQuery extends Query {

    public VertexQuery direction(final Direction direction);

    public VertexQuery labels(final String... labels);

    public VertexQuery adjacents(final Vertex... vertices);

    @Override
    public VertexQuery has(final String key);

    @Override
    public VertexQuery hasNot(final String key);

    @Override
    public VertexQuery has(final String key, final BiPredicate biPredicate, final Object value);

    @Override
    public <T extends Comparable<?>> VertexQuery interval(final String key, final T startValue, final T endValue);

    @Override
    public VertexQuery limit(final int limit);

    public long count();

    public Iterable<Edge> edges();

    public Iterable<Vertex> vertices();

    // Default

    @Override
    public default VertexQuery has(final String key, final Object value) {
        return this.has(key, Compare.EQUAL, value);
    }

}
