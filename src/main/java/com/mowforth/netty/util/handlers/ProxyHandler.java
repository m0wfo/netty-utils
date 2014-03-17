package com.mowforth.netty.util.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class ProxyHandler extends ByteToMessageDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyHandler.class);

    public static final AttributeKey<InetAddress> PROXY_ADDRESS = AttributeKey.valueOf("proxy_address");

    public enum ProxyType {
        STUD,
        PROXY,
        PROXY_V2
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (buf.getByte(0) == 2) {
            buf.readByte();
            byte[] ip = new byte[4];
            try {
                buf.readBytes(ip);
                InetAddress address = InetAddress.getByAddress(ip);
                ctx.attr(PROXY_ADDRESS).set(address);
            } catch (UnknownHostException | IndexOutOfBoundsException e) {
                LOG.error("Couldn't read remote address from PROXY header- is this instance running behind stud?");
                ctx.channel().close();
            }
        } else {
            LOG.error("Unrecognized PROXY header!");
            ctx.channel().close();
        }
    }
}