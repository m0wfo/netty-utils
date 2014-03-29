package com.mowforth.netty.util.handlers;

import com.google.common.base.Throwables;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A final catch-all handler for any (presumably invalid) events that made it to
 * the end of the channel pipeline.
 *
 * <p>Its role is to report messages that weren't intercepted by any other
 * handlers, and to gracefully handle (and report) unchecked exceptions that
 * reached the end of the pipeline.</p>
 */
@ChannelHandler.Sharable
public class HttpCatchAllHandler extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(HttpCatchAllHandler.class);

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ReferenceCounted) {
            ((ReferenceCounted) msg).release();
        }
        sendError(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.error("Caught exception: {}", Throwables.getStackTraceAsString(cause));
        sendError(ctx);
    }

    private void sendError(ChannelHandlerContext ctx) {
        if (ctx.channel().isOpen()) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }
}
