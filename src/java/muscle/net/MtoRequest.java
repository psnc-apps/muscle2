/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
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
