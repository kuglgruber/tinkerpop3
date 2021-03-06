package com.tinkerpop.gremlin.server;

import com.codahale.metrics.Counter;
import com.tinkerpop.gremlin.server.op.OpLoader;
import com.tinkerpop.gremlin.server.util.MetricManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Adapted from https://github.com/netty/netty/tree/netty-4.0.10.Final/example/src/main/java/io/netty/example/http/websocketx/server
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
class GremlinServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(GremlinServerHandler.class);
    static final Counter requestCounter = MetricManager.INSTANCE.getCounter(name(GremlinServer.class, "requests"));
    private static final String websocketPath = "/gremlin";

    private WebSocketServerHandshaker handshaker;
    private StaticFileHandler staticFileHandler;
    private final Settings settings;
    private final Graphs graphs;

    private static GremlinExecutor gremlinExecutor = new GremlinExecutor();

    public GremlinServerHandler(final Settings settings, final Graphs graphs) {
        this.settings = settings;
        this.graphs = graphs;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, final FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        final String uri = req.getUri();

        if (uri.startsWith(websocketPath)) {
            // Web socket handshake
            final WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, false);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
            }
        } else {
            // Static file request
            if (staticFileHandler == null) {
                staticFileHandler = new StaticFileHandler();
            }

            staticFileHandler.handleHttpStaticFileRequest(ctx, req);
        }
    }

    private void handleWebSocketFrame(final ChannelHandlerContext ctx, final WebSocketFrame frame) {
        requestCounter.inc();

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame)
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        else if (frame instanceof PingWebSocketFrame)
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
        else if (frame instanceof PongWebSocketFrame) { } // nothing to do
        else if (frame instanceof TextWebSocketFrame) {
            final String request = ((TextWebSocketFrame) frame).text();

            // message consists of two parts.  the first part has the mime type of the incoming message and the
            // second part is the message itself.  these two parts are separated by a "|-". if there aren't two parts
            // assume application/json and that the entire message is that format (i.e. there is no "mimetype|-")
            final String[] parts = segmentMessage(request);
            final RequestMessage requestMessage = MessageSerializer.select(parts[0], MessageSerializer.DEFAULT_REQUEST_SERIALIZER)
                    .deserializeRequest(parts[1]).orElse(RequestMessage.INVALID);

            if (!gremlinExecutor.isInitialized())
                gremlinExecutor.init(settings);

            final Optional<OpProcessor> processor = OpLoader.getProcessor(requestMessage.processor);
            if (processor.isPresent()) {
                final Context gremlinServerContext = new Context(requestMessage, ctx, settings, graphs, gremlinExecutor);
                processor.get().select(gremlinServerContext).accept(gremlinServerContext);
            }
            else
                logger.warn("Invalid OpProcessor requested [{}]", requestMessage.processor);
        }
        else
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
    }

    private static String[] segmentMessage(final String msg) {
        final int splitter = msg.indexOf("|-");
        if (splitter == -1)
            return new String[] {"application/json", msg};

        return new String[] {msg.substring(0, splitter), msg.substring(splitter + 2)};
    }

    private static void sendHttpResponse(final ChannelHandlerContext ctx,
                                         final FullHttpRequest req, final FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            final ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        final ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + websocketPath;
    }
}
