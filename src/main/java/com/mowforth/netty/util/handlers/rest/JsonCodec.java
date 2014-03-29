package com.mowforth.netty.util.handlers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import javax.inject.Inject;
import java.util.List;

/**
 * TODO
 */
public class JsonCodec extends ByteToMessageCodec<Object> {

    private final ObjectMapper mapper;

    @Inject
    public JsonCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		ByteBufOutputStream stream = new ByteBufOutputStream(out);
		mapper.writeValue(stream, msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		ByteBufInputStream stream = new ByteBufInputStream(in);
		Object decoded = mapper.readValue(stream, Object.class);
		out.add(decoded);
    }
}
