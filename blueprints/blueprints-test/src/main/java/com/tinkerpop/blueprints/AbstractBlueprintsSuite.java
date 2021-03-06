package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.strategy.GraphStrategy;
import com.tinkerpop.blueprints.util.GraphFactory;
import com.tinkerpop.blueprints.util.StreamFactory;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * Base Blueprints test suite from which different classes of tests can be exposed to implementers.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public abstract class AbstractBlueprintsSuite extends Suite {

    /**
     * The GraphProvider instance that will be used to generate a Graph instance.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface GraphProviderClass {
        public Class<? extends GraphProvider> value();
    }

    public AbstractBlueprintsSuite(final Class<?> klass, final RunnerBuilder builder, final Class<?>[] testsToExecute) throws InitializationError {
        super(builder, klass, testsToExecute);

        // figures out what the implementer assigned as the GraphProvider class and make it available to tests.
        final Class graphProviderClass = getGraphProviderClass(klass);
        try {
            GraphManager.set((GraphProvider) graphProviderClass.newInstance());
        } catch (Exception ex) {
            throw new InitializationError(ex);
        }
    }

    private static Class<? extends GraphProvider> getGraphProviderClass(Class<?> klass) throws InitializationError {
        GraphProviderClass annotation = klass.getAnnotation(GraphProviderClass.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a GraphProviderClass annotation", klass.getName()));
        }
        return annotation.value();
    }

    public static Consumer<Graph> assertVertexEdgeCounts(final int expectedVertexCount, final int expectedEdgeCount) {
        return (g) -> {
            assertEquals(expectedVertexCount, StreamFactory.stream(g.query().vertices()).count());
            assertEquals(expectedEdgeCount, StreamFactory.stream(g.query().edges()).count());
        };
    }

    /**
     * Those developing Blueprints implementations must provide a GraphProvider implementation so that the
     * BlueprintsStandardSuite knows how to instantiate their implementations.
     */
    public static interface GraphProvider {

        /**
         * Creates a new {@link Graph} instance using the default {@link Configuration} from
         * {@link #newGraphConfiguration()}.
         */
        default public Graph newTestGraph() {
            return newTestGraph(newGraphConfiguration());
        }

        /**
         * Creates a new Graph instance from the Configuration object using GraphFactory.
         */
        default public Graph newTestGraph(final Configuration config) {
            return newTestGraph(config, Optional.empty());
        }

        /**
         * Creates a new Graph instance from the Configuration object using GraphFactory.
         */
        default public Graph newTestGraph(final Configuration config, final Optional<? extends GraphStrategy> strategy) {
            return GraphFactory.open(config ,strategy);
        }

        /**
         * Creates a new base Configuration object that can construct a Graph instance from GraphFactory.
         */
        default public Configuration newGraphConfiguration() {
            return newGraphConfiguration(Collections.<String, Object>emptyMap());
        }

        /**
         * Clears a graph of all data and settings.  Implementations will have different ways of handling this.  For
         * a brute force approach, implementers can simply delete data directories provided in the configuration.
         * Implementers may choose a more elegant approach if it exists.
         */
        public void clear(final Graph g, final Configuration configuration) throws Exception;

        /**
         * Converts an identifier from a test to an identifier accepted by the Graph instance.  Test that try to
         * utilize an Element identifier will pass it to this method before usage.
         */
        default public Object convertId(final Object id) {
            return id;
        }

        /**
         * Converts an label from a test to an label accepted by the Graph instance.  Test that try to
         * utilize a label will pass it to this method before usage.
         */
        default public String convertLabel(final String label) {
            return label;
        }

        /**
         * When implementing this method ensure that the BlueprintsStandardSuite can override any settings EXCEPT the
         * "blueprints.graph" setting which should be defined by the implementer.
         *
         * @param configurationOverrides Settings to override defaults with.
         */
        public Configuration newGraphConfiguration(final Map<String, Object> configurationOverrides);
    }

    /**
     * A basic GraphProvider which simply requires the implementer to supply their base configuration for their
     * Graph instance.  Minimally this is just the setting for "blueprints.graph".
     */
    public static abstract class AbstractGraphProvider implements GraphProvider {

        public abstract Map<String, Object> getBaseConfiguration();

        @Override
        public Configuration newGraphConfiguration(final Map<String, Object> configurationOverrides) {
            final Configuration conf = new BaseConfiguration();
            getBaseConfiguration().entrySet().stream()
                    .forEach(e -> conf.setProperty(e.getKey(), e.getValue()));

            // assign overrides but don't allow blueprints.graph setting to be overridden.  the test suite should
            // not be able to override that.
            configurationOverrides.entrySet().stream()
                    .filter(c -> !c.getKey().equals("blueprints.graph"))
                    .forEach(e -> conf.setProperty(e.getKey(), e.getValue()));
            return conf;
        }
    }

    /**
     * Holds the GraphProvider specified by the implementer using the BlueprintSuite.
     */
    public static class GraphManager {
        private static GraphProvider graphProvider;

        public static GraphProvider set(final GraphProvider graphProvider) {
            final GraphProvider old = GraphManager.graphProvider;
            GraphManager.graphProvider = graphProvider;
            return old;
        }

        public static GraphProvider get() {
            return graphProvider;
        }
    }
}
