package com.mowforth.netty.util.pipelines;

import com.mowforth.netty.util.handlers.HttpResponseDecorator;
import com.mowforth.netty.util.handlers.MonitoringHandler;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.health.HealthCheckRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import javax.inject.Inject;

/**
 * Assembles a pipeline for a monitoring service.
 */
public class MonitoringPipeline extends ChannelInitializer<Channel> {

    private final MonitoringHandler monitoringHandler;

    @Inject
    public MonitoringPipeline(MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry) {
        this.monitoringHandler = new MonitoringHandler(metricRegistry, healthCheckRegistry);
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1024 * 10));
        ch.pipeline().addLast(new HttpResponseDecorator());
        ch.pipeline().addLast(monitoringHandler);
    }
}
