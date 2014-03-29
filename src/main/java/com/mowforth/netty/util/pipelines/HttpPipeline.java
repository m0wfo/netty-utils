package com.mowforth.netty.util.pipelines;

import com.google.inject.Provider;
import com.mowforth.netty.util.handlers.AutoCloseHandler;
import com.mowforth.netty.util.handlers.HttpCatchAllHandler;
import com.mowforth.netty.util.handlers.HttpResponseDecorator;
import com.mowforth.netty.util.handlers.proxy.ProxyHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO document
 */
public class HttpPipeline extends ChannelInitializer<Channel> implements Pipeline {

	private static final Logger LOG = LoggerFactory.getLogger(HttpPipeline.class);

    private Provider<? extends ChannelHandler>[] appHandlers;

    @Override
    protected void initChannel(Channel ch) throws Exception {
		ch.pipeline().addLast(new ProxyHandler());
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 10));
        ch.pipeline().addLast(new AutoCloseHandler());
        ch.pipeline().addLast(new HttpResponseDecorator());
		ch.pipeline().addLast("catchall", new HttpCatchAllHandler());

        for (Provider<? extends ChannelHandler> provider : appHandlers) {
            try {
				ChannelHandler handler = provider.get();
				ch.pipeline().addBefore("catchall", handler.getClass().getName(), handler);
			} catch (Exception e) {
				LOG.error(e.getMessage());
				// Push the error back into the pipeline so we can handle gracefully
				// with the HttpCatchAllHandler
				ch.pipeline().fireExceptionCaught(e);
			}
        }
    }

    @Override
    public void setApplicationHandlers(Provider<? extends ChannelHandler>... handlers) {
        this.appHandlers = handlers;
    }
}
