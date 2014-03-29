package com.mowforth.netty.util.handlers.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Parser for V1 of the HAProxy PROXY protocol.
 *
 * See <a href="http://haproxy.1wt.eu/download/1.5/doc/proxy-protocol.txt">here</a> for more details.
 */
public class ProxyV1Parser implements ProxyParser {

	private static final short ASCII_NUMERIC_OFFSET = 48;

	// 'PROXY '
	private static final byte[] PREFIX = new byte[] {0x50, 0x52, 0x4F, 0x58, 0x59, 0x20};
	// 'TCP'
	private static final byte[] TCP = new byte[] {0x54, 0x43, 0x50};

	private static final byte V4 = 0x34;
	private static final byte V6 = 0x36;

	@Override
	public InetSocketAddress parse(ByteBuf msg) throws ProxyParseException {
		int readerIndex;

		// See if we have a valid PROXY prefix
		readerIndex = msg.forEachByte(0, PREFIX.length, new ByteBufProcessor() {
			int i = 0;
			@Override
			public boolean process(byte value) throws Exception {
				boolean matches = value == PREFIX[i];
				i++;
				return matches && i != PREFIX.length;
			}
		});

		if (readerIndex != PREFIX.length - 1) {
			// Couldn't find prefix
			throw new ProxyParseException();
		} else {
			// Check we're using TCP, not UNKNOWN
			readerIndex = msg.forEachByte(readerIndex + 1, TCP.length, new ByteBufProcessor() {
				int i = 0;
				@Override
				public boolean process(byte value) throws Exception {
					boolean matches = value == TCP[i];
					i++;
					return matches && i != TCP.length;
				}
			});

			if (readerIndex != (PREFIX.length + TCP.length) - 1) {
				// Unknown protocol
				throw new ProxyParseException();
			} else {
				msg.readerIndex(readerIndex + 1);
				byte version = msg.readByte();
				if (version != V4 && version != V6) {
					// Not IPv4 or IPv6, throw exception
					throw new ProxyParseException();
				}

				// Skip a space
				byte sp = msg.readByte();

				readerIndex = msg.forEachByte(msg.readerIndex(), 64, ByteBufProcessor.FIND_LINEAR_WHITESPACE);

				if (readerIndex == -1) {
					throw new ProxyParseException();
				}

				int addressLength = readerIndex - msg.readerIndex();

				// Read the source address
				byte[] sourceIpBytes = new byte[addressLength];
				msg.readBytes(sourceIpBytes);
				InetAddress addr = null;
				try {
					String lit = new String (sourceIpBytes);
					addr = InetAddress.getByName(lit);
				} catch (Exception e) {
					throw new ProxyParseException();
				}

				// Skip space
				msg.readByte();

				// Find space after dest ip
				int portBoundary = msg.forEachByte(msg.readerIndex(), msg.readableBytes(),
						ByteBufProcessor.FIND_LINEAR_WHITESPACE);

				msg.readerIndex(portBoundary + 1);

				int destPortBoundary = msg.forEachByte(msg.readerIndex(), msg.readableBytes(),
						ByteBufProcessor.FIND_LINEAR_WHITESPACE);

				byte[] sourcePortBytes = new byte[destPortBoundary - msg.readerIndex()];
				msg.readBytes(sourcePortBytes);

				int sourcePort = 0;
				for (int i = 0; i < sourcePortBytes.length; i++) {
					int b = sourcePortBytes[i] - ASCII_NUMERIC_OFFSET;
					sourcePort *= 10;
					sourcePort += b;
				}

				// Done parsing- wind the readerIndex past the protocol boundary
				int lineBoundary = msg.forEachByte(ByteBufProcessor.FIND_LF);
				msg.readerIndex(lineBoundary + 1);

				return new InetSocketAddress(addr, sourcePort);
			}
		}
	}
}
