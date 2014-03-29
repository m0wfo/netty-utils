package com.mowforth.netty.util.handlers.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 * Extracts source port and IP information from various load-balancer proxy protocols.
 */
@Sharable
public class ProxyHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyHandler.class);
    public static final AttributeKey<SocketAddress> SOURCE_ADDRESS = new AttributeKey("source_address");

	private final ProxyParser parser;

	public ProxyHandler() {
		super(false);
		this.parser = new ProxyV1Parser();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		SocketAddress source = parser.parse(msg);
		ctx.channel().attr(SOURCE_ADDRESS).set(source);
		ctx.fireChannelRead(msg);
	}
}