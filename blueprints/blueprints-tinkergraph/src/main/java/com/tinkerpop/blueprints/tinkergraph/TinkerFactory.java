package com.tinkerpop.blueprints.tinkergraph;

import com.tinkerpop.blueprints.Property;
import com.tinkerpop.blueprints.Vertex;

import java.util.Optional;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerFactory {
    public static TinkerGraph createClassic() {
        final TinkerGraph graph = TinkerGraph.open(Optional.empty());

        final Vertex marko = graph.addVertex(Property.Key.ID, 1, "name", "marko", "age", 29);
        final Vertex vadas = graph.addVertex(Property.Key.ID, 2, "name", "vadas", "age", 27);
        final Vertex lop = graph.addVertex(Property.Key.ID, 3, "name", "lop", "lang", "java");
        final Vertex josh = graph.addVertex(Property.Key.ID, 4, "name", "josh", "age", 32);
        final Vertex ripple = graph.addVertex(Property.Key.ID, 5, "name", "ripple", "lang", "java");
        final Vertex peter = graph.addVertex(Property.Key.ID, 6, "name", "peter", "age", 35);
        marko.addEdge("knows", vadas, Property.Key.ID, 7, "weight", 0.5f);
        marko.addEdge("knows", josh, Property.Key.ID, 8, "weight", 1.0f);
        marko.addEdge("created", lop, Property.Key.ID, 9, "weight", 0.4f);
        josh.addEdge("created", ripple, Property.Key.ID, 10, "weight", 1.0f);
        josh.addEdge("created", lop, Property.Key.ID, 11, "weight", 0.4f);
        peter.addEdge("created", lop, Property.Key.ID, 12, "weight", 0.2f);

        return graph;
    }
}
