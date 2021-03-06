package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.Graph.Features.EdgeFeatures;
import com.tinkerpop.blueprints.Graph.Features.EdgePropertyFeatures;
import com.tinkerpop.blueprints.Graph.Features.GraphFeatures;
import com.tinkerpop.blueprints.Graph.Features.GraphAnnotationFeatures;
import com.tinkerpop.blueprints.Graph.Features.VertexFeatures;
import com.tinkerpop.blueprints.Graph.Features.VertexAnnotationFeatures;
import com.tinkerpop.blueprints.Graph.Features.VertexPropertyFeatures;
import com.tinkerpop.blueprints.strategy.GraphStrategy;
import com.tinkerpop.blueprints.strategy.PartitionGraphStrategy;
import com.tinkerpop.blueprints.util.GraphFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Optional;

import static com.tinkerpop.blueprints.Graph.Features.GraphFeatures.FEATURE_COMPUTER;
import static com.tinkerpop.blueprints.Graph.Features.GraphFeatures.FEATURE_STRATEGY;
import static com.tinkerpop.blueprints.Graph.Features.GraphFeatures.FEATURE_TRANSACTIONS;
import static com.tinkerpop.blueprints.Graph.Features.VertexFeatures.FEATURE_USER_SUPPLIED_IDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

/**
 * Tests that do basic validation of proper Feature settings in Graph implementations.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
@RunWith(Enclosed.class)
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class FeatureSupportTest  {
    private static final String INVALID_FEATURE_SPECIFICATION = "Features for %s specify that %s is false, but the feature appears to be implemented.  Reconsider this setting or throw the standard Exception.";

    /**
     * Feature checks that test {@link Graph} functionality to determine if a feature should be on when it is marked
     * as not supported.
     */
    public static class GraphFunctionalityTest extends AbstractBlueprintsTest {

        /**
         * A {@link Graph} that does not support {@link GraphFeatures#FEATURE_COMPUTER} must call
         * {@link com.tinkerpop.blueprints.Graph.Exceptions#graphComputerNotSupported()}.
         */
        @Test
        @FeatureRequirement(featureClass = GraphFeatures.class, feature = FEATURE_COMPUTER, supported = false)
        public void ifAGraphCanComputeThenItMustSupportComputer() throws Exception {
            try {
                g.compute();
                fail(String.format(INVALID_FEATURE_SPECIFICATION, GraphFeatures.class.getSimpleName(), FEATURE_COMPUTER));
            } catch (UnsupportedOperationException e) {
                assertEquals(Graph.Exceptions.graphComputerNotSupported().getMessage(), e.getMessage());
            }
        }

        /**
         * A {@link Graph} that does not support {@link GraphFeatures#FEATURE_TRANSACTIONS} must call
         * {@link com.tinkerpop.blueprints.Graph.Exceptions#transactionsNotSupported()}.
         */
        @Test
        @FeatureRequirement(featureClass = GraphFeatures.class, feature = FEATURE_TRANSACTIONS, supported = false)
        public void ifAGraphConstructsATxThenItMustSupportTransactions() throws Exception {
            try {
                g.tx();
                fail(String.format(INVALID_FEATURE_SPECIFICATION, GraphFeatures.class.getSimpleName(), FEATURE_TRANSACTIONS));
            } catch (UnsupportedOperationException e) {
                assertEquals(Graph.Exceptions.transactionsNotSupported().getMessage(), e.getMessage());
            }
        }

        /**
         * A {@link Graph} that does not support {@link GraphFeatures#FEATURE_STRATEGY} must call
         * {@link com.tinkerpop.blueprints.Graph.Exceptions#graphStrategyNotSupported()}.
         */
        @Test
        @FeatureRequirement(featureClass = GraphFeatures.class, feature = FEATURE_STRATEGY, supported = false)
        public void ifAGraphAcceptsStrategyThenItMustSupportStrategy() throws Exception {
            try {
                g.strategy();
                fail(String.format(INVALID_FEATURE_SPECIFICATION, GraphFeatures.class.getSimpleName(), FEATURE_STRATEGY));
            } catch (UnsupportedOperationException e) {
                assertEquals(Graph.Exceptions.graphStrategyNotSupported().getMessage(), e.getMessage());
            }
        }

        /**
         * If given a non-empty {@link com.tinkerpop.blueprints.strategy.GraphStrategy} a graph that does not support
         * {@link Graph.Features.GraphFeatures#FEATURE_STRATEGY} should throw
         * {@link com.tinkerpop.blueprints.Graph.Exceptions#graphStrategyNotSupported()}.
         */
        @Test
        @FeatureRequirement(featureClass = GraphFeatures.class, feature = FEATURE_STRATEGY, supported = false)
        public void shouldThrowUnsupportedIfStrategyIsNonEmptyAndStrategyFeatureDisabled() {
            try {
                GraphFactory.open(config, Optional.<GraphStrategy>of(new PartitionGraphStrategy("k", "v")));
                fail(String.format(INVALID_FEATURE_SPECIFICATION, GraphFeatures.class.getSimpleName(), FEATURE_STRATEGY));
            } catch (UnsupportedOperationException ex) {
                assertEquals(Graph.Exceptions.graphStrategyNotSupported().getMessage(), ex.getMessage());
            }

        }
    }

    /**
     * Feature checks that test {@link Vertex} functionality to determine if a feature should be on when it is marked
     * as not supported.
     */
    public static class VertexFunctionalityTest extends AbstractBlueprintsTest {

        @Test
        @FeatureRequirement(featureClass = VertexFeatures.class, feature = FEATURE_USER_SUPPLIED_IDS, supported = false)
        public void ifAnIdCanBeAssignedToVertexThenItMustSupportUserSuppliedIds() throws Exception {
            final Vertex v = g.addVertex(Property.Key.ID, BlueprintsStandardSuite.GraphManager.get().convertId(99999943835l));
            tryCommit(g, graph -> assertThat(String.format(INVALID_FEATURE_SPECIFICATION, VertexFeatures.class.getSimpleName(), FEATURE_USER_SUPPLIED_IDS),
                    v.getId().toString(),
                    is(not("99999943835"))));
        }
    }

    /**
     * Feature checks that test {@link Element} {@link Property} functionality to determine if a feature should be on
     * when it is marked as not supported.
     */
    @RunWith(Parameterized.class)
    public static class ElementPropertyFunctionalityTest extends AbstractBlueprintsTest {
        private static final String INVALID_FEATURE_SPECIFICATION = "Features for %s specify that %s is false, but the feature appears to be implemented.  Reconsider this setting or throw the standard Exception.";

        @Parameterized.Parameters(name = "{index}: supports{0}({1})")
        public static Iterable<Object[]> data() {
            return PropertyTest.PropertyFeatureSupportTest.data();
        }

        @Parameterized.Parameter(value = 0)
        public String featureName;

        @Parameterized.Parameter(value = 1)
        public Object value;

        @Test
        public void shouldEnableFeatureOnEdgeIfNotEnabled() throws Exception {
            assumeThat(g.getFeatures().supports(EdgePropertyFeatures.class, featureName), is(false));
            try {
                final Edge edge = createEdgeForPropertyFeatureTests();
                edge.setProperty("key", value);
                fail(String.format(INVALID_FEATURE_SPECIFICATION, EdgePropertyFeatures.class.getSimpleName(), featureName));
            } catch (UnsupportedOperationException e) {
                assertEquals(Property.Exceptions.dataTypeOfPropertyValueNotSupported(value).getMessage(), e.getMessage());
            }
        }

        @Test
        public void shouldEnableFeatureOnVertexIfNotEnabled() throws Exception {
            assumeThat(g.getFeatures().supports(VertexPropertyFeatures.class, featureName), is(false));
            try {
                g.addVertex("key", value);
                fail(String.format(INVALID_FEATURE_SPECIFICATION, VertexPropertyFeatures.class.getSimpleName(), featureName));
            } catch (UnsupportedOperationException e) {
                assertEquals(Property.Exceptions.dataTypeOfPropertyValueNotSupported(value).getMessage(), e.getMessage());
            }
        }

        private Edge createEdgeForPropertyFeatureTests() {
            final Vertex vertexA = g.addVertex();
            final Vertex vertexB = g.addVertex();
            return vertexA.addEdge(BlueprintsStandardSuite.GraphManager.get().convertLabel("knows"), vertexB);
        }
    }

    /**
     * Feature checks that simply evaluate conflicting feature definitions without evaluating the actual implementation
     * itself.
     */
    public static class LogicalFeatureSupportTest extends AbstractBlueprintsTest {

        private EdgeFeatures edgeFeatures;
        private EdgePropertyFeatures edgePropertyFeatures;
        private GraphFeatures graphFeatures;
        private GraphAnnotationFeatures graphAnnotationFeatures;
        private VertexFeatures vertexFeatures;
        private VertexAnnotationFeatures vertexAnnotationFeatures;
        private VertexPropertyFeatures vertexPropertyFeatures;

        @Before
        public void innerSetup() {
            final Graph.Features f = g.getFeatures();
            edgeFeatures = f.edge();
            edgePropertyFeatures = edgeFeatures.properties();
            graphFeatures = f.graph();
            graphAnnotationFeatures = graphFeatures.annotations();
            vertexFeatures = f.vertex();
            vertexAnnotationFeatures = vertexFeatures.annotations();
            vertexPropertyFeatures = vertexFeatures.properties();
        }

        @Test
        public void ifGraphHasAnnotationsEnabledThenItMustSupportADataType() {
            assertTrue(graphAnnotationFeatures.supportsAnnotations()
                    && (graphAnnotationFeatures.supportsBooleanValues() || graphAnnotationFeatures.supportsDoubleValues()
                    || graphAnnotationFeatures.supportsFloatValues() || graphAnnotationFeatures.supportsIntegerValues()
                    || graphAnnotationFeatures.supportsLongValues() || graphAnnotationFeatures.supportsMapValues()
                    || graphAnnotationFeatures.supportsMetaProperties() || graphAnnotationFeatures.supportsMixedListValues()
                    || graphAnnotationFeatures.supportsPrimitiveArrayValues() || graphAnnotationFeatures.supportsPrimitiveArrayValues()
                    || graphAnnotationFeatures.supportsSerializableValues() || graphAnnotationFeatures.supportsStringValues()
                    || graphAnnotationFeatures.supportsUniformListValues()));
        }

        @Test
        public void ifVertexHasAnnotationsEnabledThenItMustSupportADataType() {
            assertTrue(vertexAnnotationFeatures.supportsAnnotations()
                    && (vertexAnnotationFeatures.supportsBooleanValues() || vertexAnnotationFeatures.supportsDoubleValues()
                    || vertexAnnotationFeatures.supportsFloatValues() || vertexAnnotationFeatures.supportsIntegerValues()
                    || vertexAnnotationFeatures.supportsLongValues() || vertexAnnotationFeatures.supportsMapValues()
                    || vertexAnnotationFeatures.supportsMetaProperties() || vertexAnnotationFeatures.supportsMixedListValues()
                    || vertexAnnotationFeatures.supportsPrimitiveArrayValues() || vertexAnnotationFeatures.supportsPrimitiveArrayValues()
                    || vertexAnnotationFeatures.supportsSerializableValues() || vertexAnnotationFeatures.supportsStringValues()
                    || vertexAnnotationFeatures.supportsUniformListValues()));
        }

        @Test
        public void ifEdgeHasPropertyEnabledThenItMustSupportADataType() {
            assertTrue(edgePropertyFeatures.supportsProperties()
                    && (edgePropertyFeatures.supportsBooleanValues() || edgePropertyFeatures.supportsDoubleValues()
                    || edgePropertyFeatures.supportsFloatValues() || edgePropertyFeatures.supportsIntegerValues()
                    || edgePropertyFeatures.supportsLongValues() || edgePropertyFeatures.supportsMapValues()
                    || edgePropertyFeatures.supportsMetaProperties() || edgePropertyFeatures.supportsMixedListValues()
                    || edgePropertyFeatures.supportsPrimitiveArrayValues() || edgePropertyFeatures.supportsPrimitiveArrayValues()
                    || edgePropertyFeatures.supportsSerializableValues() || edgePropertyFeatures.supportsStringValues()
                    || edgePropertyFeatures.supportsUniformListValues()));
        }

        @Test
        public void ifVertexHasPropertyEnabledThenItMustSupportADataType() {
            assertTrue(vertexPropertyFeatures.supportsProperties()
            && (vertexPropertyFeatures.supportsBooleanValues() || vertexPropertyFeatures.supportsDoubleValues()
                    || vertexPropertyFeatures.supportsFloatValues() || vertexPropertyFeatures.supportsIntegerValues()
                    || vertexPropertyFeatures.supportsLongValues() || vertexPropertyFeatures.supportsMapValues()
                    || vertexPropertyFeatures.supportsMetaProperties() || vertexPropertyFeatures.supportsMixedListValues()
                    || vertexPropertyFeatures.supportsPrimitiveArrayValues() || vertexPropertyFeatures.supportsPrimitiveArrayValues()
                    || vertexPropertyFeatures.supportsSerializableValues() || vertexPropertyFeatures.supportsStringValues()
                    || vertexPropertyFeatures.supportsUniformListValues()));
        }


    }
}
