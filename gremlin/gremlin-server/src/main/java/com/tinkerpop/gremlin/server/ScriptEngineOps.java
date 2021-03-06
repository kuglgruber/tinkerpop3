package com.tinkerpop.gremlin.server;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Features that {@code ScriptEngine} objects expose regardless of whether they are in a session or shared.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public interface ScriptEngineOps {
    /**
     * Evaluate a script with {@code Bindings} for a particular language.
     */
    public Object eval(final String script, final Bindings bindings, final String language)
            throws ScriptException, InterruptedException, ExecutionException, TimeoutException;

    /**
     * Perform append to the existing import list for all {@code ScriptEngine} instances that implement the
     * {@link com.tinkerpop.gremlin.groovy.jsr223.DependencyManager} interface.
     */
    public void addImports(final Set<String> imports);

    /**
     * Pull in dependencies given some Maven coordinates.  Cycle through each {@code ScriptEngine} object and determine
     * if it implements {@link com.tinkerpop.gremlin.groovy.jsr223.DependencyManager}.  For those that do call the
     * {@link com.tinkerpop.gremlin.groovy.jsr223.DependencyManager#use} method to fire it up.
     */
    public void use(final String group, final String artifact, final String version);

    /**
     * Get a list of all dependencies loaded to the {@code ScriptEngine} where the key is the {@code ScriptEngine}
     * name and the value is the dependency list for that {@code ScriptEngine}.
     */
    public Map<String,List<Map>> dependencies();

    /**
     * Gets a list of all imports in the {@code ScriptEngine} where the key is the {@code ScriptEngine} name and the
     * value is the import list for that {@code ScriptEngine}.
     */
    public Map<String, List<Map>> imports();

    /**
     * Resets the {@code ScriptEngine} which recreates the classloader and destroys all caches of compiled scripts.
     * Doing a reset will imply a potential slowdown to server operations as future executions will have to recompile
     * scripts and potentially reload classes.
     */
    public void reset();
}