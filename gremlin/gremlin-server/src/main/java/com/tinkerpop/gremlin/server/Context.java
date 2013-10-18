package com.tinkerpop.gremlin.server;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class Context {
    private final RequestMessage requestMessage;
    private final ChannelHandlerContext channelHandlerContext;
    private final Settings settings;
    private final GremlinServer.Graphs graphs;

    public Context(final RequestMessage requestMessage, final ChannelHandlerContext ctx,
                   final Settings settings, final GremlinServer.Graphs graphs) {
        this.requestMessage = requestMessage;
        this.channelHandlerContext = ctx;
        this.settings = settings;
        this.graphs = graphs;
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public Settings getSettings() {
        return settings;
    }

    public GremlinServer.Graphs getGraphs() {
        return graphs;
    }
}