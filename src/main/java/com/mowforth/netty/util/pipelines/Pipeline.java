package com.mowforth.netty.util.pipelines;

import io.netty.channel.ChannelHandler;

/**
 * CHANGEME
 */
public interface Pipeline extends ChannelHandler {

    public void setApplicationHandlers(Class<? extends ChannelHandler>... handlers);
}
