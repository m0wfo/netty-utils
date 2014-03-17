package com.mowforth.netty.util.handlers;

import com.google.common.net.MediaType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCounted;

import java.util.Date;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class HttpResponseDecorator extends MessageToMessageCodec<HttpRequest,HttpResponse> {

    private boolean isKeepAlive;

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpResponse response, List<Object> out) throws Exception {
        if (response instanceof ReferenceCounted) {
            ((ReferenceCounted) response).retain();
        }

        // Add content length if necessary
        if (!response.headers().contains(CONTENT_LENGTH)) {
            if (response instanceof DefaultFullHttpResponse) {
                DefaultFullHttpResponse full = (DefaultFullHttpResponse) response;
                HttpHeaders.setContentLength(response, full.content().readableBytes());
            }
        }

        // Set content type
        if (!response.headers().contains(CONTENT_TYPE)) {
            response.headers().set(CONTENT_TYPE, MediaType.PLAIN_TEXT_UTF_8.toString());
        }

        // Set keep-alive status
        if (isKeepAlive) {
            response.headers().set(CONNECTION, "Keep-Alive");
        }

        // Set date
        if (!response.headers().contains(DATE)) {
            HttpHeaders.setDate(response, new Date());
        }

        out.add(response);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpRequest request, List<Object> out) throws Exception {
        if (request instanceof ReferenceCounted) {
            ((ReferenceCounted) request).retain();
        }
        isKeepAlive = HttpHeaders.isKeepAlive(request);
        out.add(request);
    }
}