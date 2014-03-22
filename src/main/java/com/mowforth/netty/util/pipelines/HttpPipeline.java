package com.mowforth.netty.util.pipelines;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mowforth.netty.util.handlers.AutoCloseHandler;
import com.mowforth.netty.util.handlers.HttpCatchAllHandler;
import com.mowforth.netty.util.handlers.HttpResponseDecorator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * TODO document
 */
public class HttpPipeline extends ChannelInitializer<Channel> implements Pipeline {

    private final Injector injector;
    private Class<? extends ChannelHandler>[] appHandlers;

    @Inject
    public HttpPipeline(Injector injector) {
        this.injector = injector;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 10));
        ch.pipeline().addLast(new AutoCloseHandler());
        ch.pipeline().addLast(new HttpResponseDecorator());
        for (Class<? extends ChannelHandler> handler : appHandlers) {
            ch.pipeline().addLast(injector.getInstance(handler));
        }
        ch.pipeline().addLast(new HttpCatchAllHandler());
    }

    @Override
    public void setApplicationHandlers(Class<? extends ChannelHandler>... handlers) {
        this.appHandlers = handlers;
    }
}
