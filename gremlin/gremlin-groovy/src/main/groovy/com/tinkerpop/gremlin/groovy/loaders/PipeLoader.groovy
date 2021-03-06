package com.tinkerpop.gremlin.groovy.loaders

import com.tinkerpop.gremlin.Pipeline
import com.tinkerpop.gremlin.groovy.GremlinLoader

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class PipeLoader {

    public static void load() {

        Pipeline.metaClass.propertyMissing = { final String name ->
            if (GremlinLoader.isStep(name)) {
                return delegate."$name"();
            } else {
                return ((Pipeline) delegate).value(name);
            }
        }

        /*[Iterable, Iterator].each {
            it.metaClass.count = {
                return PipeHelper.counter(delegate.iterator());
            }
        }*/

        [Iterable, Iterator].each {
            it.metaClass.mean = {
                double counter = 0;
                double sum = 0;
                delegate.each { counter++; sum += it; }
                return sum / counter;
            }
        }

        Pipeline.metaClass.getAt = { final Integer index ->
            return ((Pipeline) delegate).range(index, index);
        }


        Pipeline.metaClass.getAt = { final Range range ->
            return ((Pipeline) delegate).range(range.getFrom() as Integer, range.getTo() as Integer);
        }
    }
}
