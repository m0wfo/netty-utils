package com.mowforth.netty.util.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.health.HealthCheck;
import com.yammer.metrics.health.HealthCheckRegistry;
import com.yammer.metrics.json.HealthCheckModule;
import com.yammer.metrics.json.MetricsModule;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Handler for exposing metric & healthcheck data.
 *
 * <p>This provides equivalent functionality of an embedded
 * Jetty instance to run the {@literal MetricsServlet}
 * and {@literal HealthCheckServlet} servlets provided by the
 * Yammer metrics library.</p>
 *
 * <p>The only configurable parameter is the port to listen on.
 * The endpoint can only be accessed locally, i.e. it's bound
 * to {@code 127.0.0.1}.</p>
 */
@ChannelHandler.Sharable
public class MonitoringHandler extends SimpleChannelInboundHandler<DefaultFullHttpRequest> {

    private static final String METRICS = "metrics";
    private static final String PING = "ping";
    private static final String HEALTH = "healthcheck";
    private static final String TEMPLATE =
            "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "  <title>Operational Menu</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "  <h1>Operational Menu</h1>\n" +
                    "  <ul>\n" +
                    "    <li><a href=\"/" + METRICS + "?pretty=true\">Metrics</a></li>\n" +
                    "    <li><a href=\"/" + PING + "\">Ping</a></li>\n" +
                    "    <li><a href=\"/" + HEALTH + "\">Healthcheck</a></li>\n" +
                    "  </ul>\n" +
                    "</body>\n" +
                    "</html>";

    private final MetricRegistry metricRegistry;
    private final HealthCheckRegistry healthCheckRegistry;
    private final ObjectMapper mapper;

    @Inject
    public MonitoringHandler(MetricRegistry metricRegistry,
                             HealthCheckRegistry healthCheckRegistry) {
        this.mapper = new ObjectMapper()
                .registerModule(new MetricsModule(TimeUnit.SECONDS, TimeUnit.SECONDS, false))
                .registerModule(new HealthCheckModule());
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = healthCheckRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultFullHttpRequest msg) throws Exception {
        if (msg.getMethod() == HttpMethod.GET) {
            route(ctx, msg);
        } else {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_IMPLEMENTED));
        }
    }

    private void route(ChannelHandlerContext ctx, DefaultFullHttpRequest msg) throws Exception {
        DefaultFullHttpResponse response;
        String uri = msg.getUri();
        if (uri.equals("/")) {
            response = opMenu();
        } else if (uri.startsWith("/" + PING)) {
            response = handlePing();
        } else if (uri.startsWith("/" + HEALTH)) {
            response = handleHealthcheck();
        } else if (uri.startsWith("/" + METRICS)) {
            QueryStringDecoder decoder = new QueryStringDecoder(uri);
            List<String> data = decoder.parameters().get("pretty");
            boolean pretty = true;
            if (data == null || data.size() < 1 || data.get(0).isEmpty()) {
                pretty = false;
            }
            response = handleMetrics(pretty);
        } else {
            // 404
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        }
        ctx.writeAndFlush(response);
    }

    private DefaultFullHttpResponse opMenu() {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(TEMPLATE.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, MediaType.HTML_UTF_8);
        return response;
    }

    private DefaultFullHttpResponse handlePing() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer("pong".getBytes()));
    }

    private DefaultFullHttpResponse handleHealthcheck() throws Exception {
        Map<String, HealthCheck.Result> results = healthCheckRegistry.runHealthChecks();
        DefaultFullHttpResponse response;
        if (results.isEmpty()) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.NOT_IMPLEMENTED);
        } else {
            String formatted = mapper.writeValueAsString(results);
            HttpResponseStatus status = isAllHealthy(results) ?
                    HttpResponseStatus.OK : HttpResponseStatus.INTERNAL_SERVER_ERROR;
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                    Unpooled.copiedBuffer(formatted.getBytes()));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        }
        return response;
    }

    private DefaultFullHttpResponse handleMetrics(boolean pretty) throws Exception {
        String formatted;
        if (pretty) {
            formatted = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metricRegistry);
        } else {
            formatted = mapper.writeValueAsString(metricRegistry);
        }
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK, Unpooled.copiedBuffer(formatted.getBytes()));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        return response;
    }

    private static boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        for (HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }
}