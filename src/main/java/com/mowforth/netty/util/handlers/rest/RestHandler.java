package com.mowforth.netty.util.handlers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.List;

/**
 * CHANGEME
 */
@ChannelHandler.Sharable
public class RestHandler extends MessageToMessageCodec<HttpRequest, HttpResponse> {

    private final int version;
    private final ObjectMapper mapper;

    public RestHandler(int version) {
        this.version = version;
        this.mapper = new ObjectMapper();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpResponse msg, List<Object> out) throws Exception {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) throws Exception {

    }
}
