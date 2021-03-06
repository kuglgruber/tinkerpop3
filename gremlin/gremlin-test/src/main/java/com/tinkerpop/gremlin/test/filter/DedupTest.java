package com.tinkerpop.gremlin.test.filter;

import com.tinkerpop.blueprints.util.StreamFactory;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class DedupTest {

    public void testCompliance() {
        assertTrue(true);
    }

    public void g_V_both_dedup_name(final Iterator<String> pipe) {
        System.out.println("Testing: " + pipe);
        final List<String> names = StreamFactory.stream(pipe).collect(Collectors.toList());
        assertEquals(6, names.size());
        assertTrue(names.contains("marko"));
        assertTrue(names.contains("vadas"));
        assertTrue(names.contains("lop"));
        assertTrue(names.contains("josh"));
        assertTrue(names.contains("ripple"));
        assertTrue(names.contains("peter"));
        assertFalse(pipe.hasNext());
    }

    public void g_V_both_dedupXlangX_name(final Iterator<String> pipe) {
        System.out.println("Testing: " + pipe);
        final List<String> names = StreamFactory.stream(pipe).collect(Collectors.toList());
        assertEquals(2, names.size());
        assertTrue(names.contains("marko") || names.contains("peter") || names.contains("josh") || names.contains("vadas"));
        assertTrue(names.contains("lop") || names.contains("ripple"));
        assertFalse(pipe.hasNext());
    }
}
