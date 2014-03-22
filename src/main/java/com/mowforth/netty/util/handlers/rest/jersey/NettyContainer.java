package com.mowforth.netty.util.handlers.rest.jersey;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import javax.ws.rs.core.SecurityContext;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CHANGEME
 */
public class NettyContainer  {

    private final ApplicationHandler application;

    public NettyContainer(ApplicationHandler application) {
        this.application = application;
    }

    private static class Writer extends SimpleChannelInboundHandler<HttpMessage> implements ContainerResponseWriter {

        private final ChannelHandlerContext ctx;
        private DefaultFullHttpResponse response;

        Writer(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext) throws ContainerException {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(responseContext.getStatus()));

            for (Map.Entry<String, List<Object>> header : responseContext.getHeaders().entrySet()) {

            }

            return new ByteBufOutputStream(response.content());
        }

        @Override
        public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void commit() {
            ctx.writeAndFlush(response);
        }

        @Override
        public void failure(Throwable error) {
            ctx.fireExceptionCaught(error);
        }

        @Override
        public boolean enableResponseBuffering() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
            Writer w = new Writer(ctx);

            PropertiesDelegate properties = new MapPropertiesDelegate();
            SecurityContext securityContext = new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return null;
                }

                @Override
                public boolean isUserInRole(String s) {
                    return false;
                }

                @Override
                public boolean isSecure() {
                    return false;
                }

                @Override
                public String getAuthenticationScheme() {
                    return null;
                }
            };
        }
    }
}
