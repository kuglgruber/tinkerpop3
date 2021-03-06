package com.tinkerpop.gremlin.test.sideeffect;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class LinkTest {

    public void testCompliance() {
        assertTrue(true);
    }

    public void test_g_v1_asXaX_outXcreatedX_inXcreatedX_linkBothXcocreator_aX(final Iterator<Vertex> pipe) {
        System.out.println("Testing: " + pipe);
        List<Vertex> cocreators = new ArrayList<Vertex>();
        List<Object> ids = new ArrayList<Object>();
        while (pipe.hasNext()) {
            Vertex vertex = pipe.next();
            cocreators.add(vertex);
            ids.add(vertex.getId());
        }
        assertEquals(cocreators.size(), 3);
        assertTrue(ids.contains("1"));
        assertTrue(ids.contains("6"));
        assertTrue(ids.contains("4"));

        for (Vertex vertex : cocreators) {
            if (vertex.getId().equals("1")) {
                assertEquals(vertex.query().direction(Direction.OUT).labels("cocreator").count(), 4);
                assertEquals(vertex.query().direction(Direction.IN).labels("cocreator").count(), 4);
            } else {
                assertEquals(vertex.query().direction(Direction.OUT).labels("cocreator").count(), 1);
                assertEquals(vertex.query().direction(Direction.IN).labels("cocreator").count(), 1);
            }
        }
    }
}
