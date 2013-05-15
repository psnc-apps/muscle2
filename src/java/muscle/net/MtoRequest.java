/*
 * 
 */

package muscle.net;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Mariusz Mamonski
 */
public class MtoRequest {
	static final byte TYPE_REGISTER = 1;
	static final byte TYPE_CONNECT = 2;
	static final byte TYPE_CONNECT_RESPONSE = 3;
	static final byte ENDPOINT_IPV4 = 1;
	static final byte ENDPOINT_IPV6 = 2;
	private final static byte[] empty = new byte[12];
		
	final byte type;
	final InetAddress srcA;
	final InetAddress dstA;
	public int srcP = 0;
	public int dstP = 0;
	public int sessionId = 0;
	
	
	public MtoRequest(byte type, InetAddress srcA, InetAddress dstA)
	{
		if (dstA == null || srcA == null)
			throw new IllegalArgumentException("Source and destination may not be null");
		this.type = type;
		this.srcA = srcA;
		this.dstA = dstA;
	}
	public MtoRequest(byte type, InetSocketAddress src, InetSocketAddress dst)
	{
		if (src == null)
			throw new IllegalArgumentException("Source may not be null");
		
		this.type = type;
		this.srcA = src.getAddress();
		this.srcP = src.getPort();
		if (dst == null) {
			this.dstA = this.srcA;
			this.dstP = this.srcP;
		} else {
			this.dstA = dst.getAddress();
			this.dstP = dst.getPort();
		}
	}
	
	public String toString() {
		return "MtoRequest<" + type + ">[" + srcA.toString() + ":" + srcP + " - " + dstA.toString() + ":" + dstP + "]";
	}

	public ByteBuffer write() {
		ByteBuffer buffer = ByteBuffer.allocate(byteSize());
		buffer.order(ByteOrder.BIG_ENDIAN); // Network byte order
		buffer.put(type);
		if (srcA instanceof Inet4Address) {
			buffer.put(ENDPOINT_IPV4);
			buffer.put(srcA.getAddress());
			buffer.put(empty);
		} else {
			buffer.put(ENDPOINT_IPV6);
			buffer.put(srcA.getAddress());
		}
		buffer.putShort((short)(srcP & 0xffff));
		if (dstA instanceof Inet4Address) {
			buffer.put(ENDPOINT_IPV4);
			buffer.put(dstA.getAddress());
			buffer.put(empty);
		} else {
			buffer.put(ENDPOINT_IPV6);
			buffer.put(dstA.getAddress());
		}
		buffer.putShort((short)(dstP & 0xffff));
		buffer.putInt(sessionId);
		assert (buffer.remaining() == 0);
		return buffer;
	}

	public static int byteSize() {
		return 43;
	}

	public static MtoRequest read(ByteBuffer buffer) throws UnknownHostException {
		byte type;
		byte[] addr;
		InetAddress src, dst;
		int srcPort, dstPort;
		
		buffer.order(ByteOrder.BIG_ENDIAN); // Network byte order
		type = buffer.get();
		if (buffer.get() == ENDPOINT_IPV4) {
			addr = new byte[4];
			buffer.get(addr);
			buffer.position(buffer.position() + 12);
		} else {
			addr = new byte[16];
			buffer.get(addr);
		}
		src = InetAddress.getByAddress(addr);
		srcPort = (buffer.getShort() & 0xffff);
		if (buffer.get() == ENDPOINT_IPV4) {
			addr = new byte[4];
			buffer.get(addr);
			buffer.position(buffer.position() + 12);
		} else {
			addr = new byte[16];
			buffer.get(addr);
		}
		dst = InetAddress.getByAddress(addr);
		dstPort = (buffer.getShort() & 0xffff);
		
		MtoRequest r = new MtoRequest(type, src, dst);
		r.srcP = srcPort;
		r.dstP = dstPort;
		r.sessionId = buffer.getInt();
		return r;
	}
}
