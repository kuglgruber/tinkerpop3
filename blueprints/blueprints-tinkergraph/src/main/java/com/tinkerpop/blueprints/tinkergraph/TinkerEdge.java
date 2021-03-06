package com.tinkerpop.blueprints.tinkergraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.computer.GraphComputer;
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.StringFactory;

import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class TinkerEdge extends TinkerElement implements Edge {

    private final Vertex inVertex;
    private final Vertex outVertex;

    protected TinkerEdge(final String id, final Vertex outVertex, final String label, final Vertex inVertex, final TinkerGraph graph) {
        super(id, label, graph);
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        this.graph.edgeIndex.autoUpdate(Property.Key.LABEL, this.label, null, this);
    }

    private TinkerEdge(final TinkerEdge edge, final TinkerGraphComputer.State state, final String centricId, final TinkerVertexMemory vertexMemory) {
        super(edge.id, edge.label, edge.graph);
        this.state = state;
        this.inVertex = edge.inVertex;
        this.outVertex = edge.outVertex;
        this.properties = edge.properties;
        this.vertexMemory = vertexMemory;
        this.centricId = centricId;
    }

    public <V> void setProperty(final String key, final V value) {
        ElementHelper.validateProperty(key, value);
        if (TinkerGraphComputer.State.STANDARD == this.state) {
            final Property oldProperty = super.getProperty(key);
            this.properties.put(key, new TinkerProperty<>(this, key, value));
            this.graph.edgeIndex.autoUpdate(key, value, oldProperty.isPresent() ? oldProperty.get() : null, this);
        } else if (TinkerGraphComputer.State.CENTRIC == this.state) {
            if (this.vertexMemory.getComputeKeys().containsKey(key))
                this.vertexMemory.setProperty(this, key, value);
            else
                throw GraphComputer.Exceptions.providedKeyIsNotAComputeKey(key);
        } else {
            throw GraphComputer.Exceptions.adjacentElementPropertiesCanNotBeWritten();
        }
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.IN))
            return this.inVertex;
        else if (direction.equals(Direction.OUT))
            return this.outVertex;
        else
            throw Element.Exceptions.bothIsNotSupported();
    }

    public void remove() {
        if (!this.graph.edges.containsKey(this.getId()))
            throw Element.Exceptions.elementHasAlreadyBeenRemovedOrDoesNotExist(Edge.class, this.getId());

        final TinkerVertex outVertex = (TinkerVertex) this.getVertex(Direction.OUT);
        final TinkerVertex inVertex = (TinkerVertex) this.getVertex(Direction.IN);
        if (null != outVertex && null != outVertex.outEdges) {
            final Set<Edge> edges = outVertex.outEdges.get(this.getLabel());
            if (null != edges)
                edges.remove(this);
        }
        if (null != inVertex && null != inVertex.inEdges) {
            final Set<Edge> edges = inVertex.inEdges.get(this.getLabel());
            if (null != edges)
                edges.remove(this);
        }

        this.graph.edgeIndex.removeElement(this);
        this.graph.edges.remove(this.getId());
        this.properties.clear();
    }

    public TinkerEdge createClone(final TinkerGraphComputer.State state, final String centricId, final TinkerVertexMemory vertexMemory) {
        return new TinkerEdge(this, state, centricId, vertexMemory);
    }

    public String toString() {
        return StringFactory.edgeString(this);

    }
}
