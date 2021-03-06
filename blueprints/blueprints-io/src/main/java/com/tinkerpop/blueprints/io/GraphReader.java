package com.tinkerpop.blueprints.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public interface GraphReader {
    public void inputGraph(InputStream graphInputStream) throws IOException;
}
