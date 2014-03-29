package com.mowforth.netty.util.pipelines;

import com.google.inject.Provider;
import io.netty.channel.ChannelHandler;

/**
 * CHANGEME
 */
public interface Pipeline extends ChannelHandler {

    public void setApplicationHandlers(Provider<? extends ChannelHandler>... handlers);
}
