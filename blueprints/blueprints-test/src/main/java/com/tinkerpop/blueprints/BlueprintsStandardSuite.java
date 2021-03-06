package com.tinkerpop.blueprints;

import com.tinkerpop.blueprints.strategy.IdGraphStrategyTest;
import com.tinkerpop.blueprints.strategy.PartitionGraphStrategyTest;
import com.tinkerpop.blueprints.strategy.ReadOnlyGraphStrategyTest;
import com.tinkerpop.blueprints.strategy.SequenceGraphStrategyTest;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * The BlueprintsStandardSuite is a custom JUnit test runner that executes the Blueprints Test Suite over a Graph
 * implementation.  This specialized test suite and runner is for use by Blueprints implementers to test their
 * Graph implementations.  The BlueprintsStandardSuite ensures consistency and validity of the implementations that they
 * test.
 * <p/>
 * To use the BlueprintSuite define a class in a test module.  Simple naming would expect the name of the
 * implementation followed by "BlueprintsStandardSuite".  This class should be annotated as follows (note that the "Suite"
 * implements BlueprintsStandardSuite.GraphProvider as a convenience only...it could be implemented in a separate class file):
 * <code>
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 * @RunWith(BlueprintsSuite.class)
 * @BlueprintsSuite.GraphProviderClass(MsAccessBlueprintsTest.class)
 * public class MsAccessBlueprintsTest implements AbstractBlueprintsSuite.GraphProvider {
 * }
 * </code>
 * Implementing AbstractBlueprintsSuite.GraphProvider provides a way for the BlueprintsStandardSuite to instantiate Graph instances
 * from the implementation being tested to inject into tests in the suite.  The BlueprintsStandardSuite will utilized
 * Features defined in the suite to determine which tests will be executed.
 */
public class BlueprintsStandardSuite extends AbstractBlueprintsSuite {

    /**
     * This list of tests in the suite that will be executed.  Blueprints developers should add to this list
     * as needed to enforce tests upon implementations.
     */
    private static final Class<?>[] testsToExecute = new Class<?>[]{
            AnnotationTest.class,
            EdgeTest.class,
            ExceptionConsistencyTest.class,
            FeatureSupportTest.class,
            GraphTest.class,
            GraphComputerTest.class,
            GraphConstructionTest.class,
            IdGraphStrategyTest.class,
            IoTest.class,
            PartitionGraphStrategyTest.class,
            PropertyTest.class,
            ReadOnlyGraphStrategyTest.class,
            SequenceGraphStrategyTest.class,
            VertexTest.class};


    public BlueprintsStandardSuite(final Class<?> klass, final RunnerBuilder builder) throws InitializationError {
        super(klass, builder, testsToExecute);
    }
}
