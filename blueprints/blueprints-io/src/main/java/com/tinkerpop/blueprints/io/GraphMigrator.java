package com.tinkerpop.blueprints.io;

import com.tinkerpop.blueprints.Graph;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * GraphMigrator takes the data in one graph and pipes it to another graph.
 *
 * @author Alex Averbuch (alex.averbuch@gmail.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class GraphMigrator {

    /**
     * Pipe the data from one graph to another graph.  It is important that the reader and writer utilize the
     * same format.
     *
     * @param fromGraph the graph to take data from
     * @param toGraph   the graph to take data to
     * @param reader reads from the graph written by the writer
     * @param writer writes the graph to be read by the reader
     * @throws IOException        thrown if there is an error in steam between the two graphs
     */
    public static void migrateGraph(final Graph fromGraph, final Graph toGraph,
                                    final GraphReader reader, final GraphWriter writer) throws IOException {

        final PipedInputStream inPipe = new PipedInputStream() {
            // Default is 1024
            protected static final int PIPE_SIZE = 1024;
        };

        final PipedOutputStream outPipe = new PipedOutputStream(inPipe) {
            public void close() throws IOException {
                while (inPipe.available() > 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                super.close();
            }
        };

        new Thread(new Runnable() {
            public void run() {
                try {
                    writer.outputGraph(outPipe);
                    outPipe.flush();
                    outPipe.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        reader.inputGraph(inPipe);
    }
}
