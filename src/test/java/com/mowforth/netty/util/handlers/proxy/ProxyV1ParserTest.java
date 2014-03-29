package com.mowforth.netty.util.handlers.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.*;

public class ProxyV1ParserTest {

	private ProxyV1Parser parser;
	private ByteBuf msg;

	@Before
	public void setup() {
		parser = new ProxyV1Parser();
	}

	@After
	public void tearDown() {
		if (msg != null) {
			ReferenceCountUtil.release(msg);
		}
	}

	@Test
	public void testValidIPv4Header() throws ProxyParseException {
		String header = "PROXY TCP4 89.100.52.99 127.0.0.1 23109 443\r\n";
		msg = Unpooled.wrappedBuffer(header.getBytes());
		InetSocketAddress address = parser.parse(msg);
		assertEquals("89.100.52.99", address.getAddress().getHostAddress());
		assertEquals(23109, address.getPort());
	}

	@Test
	public void testIPv4FromSpec() throws ProxyParseException {
		String header = "PROXY TCP4 255.255.255.255 255.255.255.255 65535 65535\r\n";
		msg = Unpooled.wrappedBuffer(header.getBytes());
		InetSocketAddress address = parser.parse(msg);
		assertEquals("255.255.255.255", address.getAddress().getHostAddress());
		assertEquals(65535, address.getPort());
	}

	@Test
	public void testFullIPv6SourceAddress() throws ProxyParseException {
		String header = "PROXY TCP6 2607:f0d0:1002:0051:0000:0000:0000:0004 2607:f0d0:1002:51::4 22 65535\r\n";
		msg = Unpooled.wrappedBuffer(header.getBytes());
		InetSocketAddress address = parser.parse(msg);
		assertEquals("2607:f0d0:1002:51:0:0:0:4", address.getAddress().getHostAddress());
		assertEquals(22, address.getPort());
	}
}
