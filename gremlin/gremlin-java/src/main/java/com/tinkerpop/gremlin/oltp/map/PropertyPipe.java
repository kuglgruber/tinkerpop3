package com.tinkerpop.gremlin.oltp.map;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Property;
import com.tinkerpop.gremlin.MapPipe;
import com.tinkerpop.gremlin.Pipeline;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PropertyPipe<E2> extends MapPipe<Element, Property<E2>> {

    public PropertyPipe(final Pipeline pipeline, final String key) {
        super(pipeline, holder -> holder.get().getProperty(key));
    }
}
