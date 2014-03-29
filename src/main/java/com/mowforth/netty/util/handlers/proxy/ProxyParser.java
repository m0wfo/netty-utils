package com.mowforth.netty.util.handlers.proxy;

import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;

/**
 * Extracts source IP / port data from proxy headers.
 */
public interface ProxyParser {

	public InetSocketAddress parse(ByteBuf msg) throws ProxyParseException;
}
