package com.mowforth.netty.util.handlers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import javax.inject.Inject;
import java.util.List;

/**
 * TODO
 */
public class JsonCodec<T> extends ByteToMessageCodec<T> {

    private final ObjectMapper mapper;

    @Inject
    public JsonCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, T msg, ByteBuf out) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
