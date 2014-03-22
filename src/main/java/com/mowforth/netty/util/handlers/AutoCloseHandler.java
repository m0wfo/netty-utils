package com.mowforth.netty.util.handlers;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

/**
 * Ensures channels are asynchronously closed unless HTTP keep-alive
 * has been requested by the client.
 */
@ChannelHandler.Sharable
public class AutoCloseHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpMessage) {
            if (!HttpHeaders.isKeepAlive((HttpMessage)msg)) {
                ctx.write(msg, promise).addListener(ChannelFutureListener.CLOSE);
                return;
            }
        }
        ctx.write(msg, promise);
    }
}
