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
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package muscle.util.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class Base64OutputStream extends OutputStream {
	private final static byte ILL = Byte.MIN_VALUE;
	private final static byte[] base64chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();
	private final static byte[] base64lookup = new byte[256]; // initialized 0
	static {
		Arrays.fill(base64lookup, ILL);
		for (int i = 0; i < base64chars.length; i++) {
			base64lookup[base64chars[i]] = (byte)i;
		}
	}
	
	private final byte[] buffer;
	private int idx;
	private int remain;
	private int remainIdx;
	
	private final OutputStream s;
	
	/**
	 * Create a new Base64 serializer.
	 *
	 * @param out stream to write the XDR data to
	 */
	public Base64OutputStream(OutputStream out) {
		this.s = out;
		this.idx = 0;
		this.remain = 0;
		this.remainIdx = 0;
		this.buffer = new byte[78];
		assert(buffer.length % 78 == 0);
	}

	private void startSequence() throws IOException {
		// we add newlines after every 76 output characters, according to
		// the MIME specs
		if (idx % 78 == 76) {
			buffer[idx++] = '\r';
			buffer[idx++] = '\n';
			if (idx + 4 > buffer.length) {
				s.write(buffer, 0, idx);
				idx = 0;
			}
		}
	}
		
	@Override
	public void flush() throws IOException {
		if (remainIdx > 0) {
			startSequence();
			buffer[idx++] = base64chars[(this.remain >>> 18) & 0x3f];
			buffer[idx++] = base64chars[(this.remain >> 12) & 0x3f];
			if (remainIdx == 2) {
				buffer[idx++] = base64chars[(this.remain >> 6) & 0x3f];
			} else {
				buffer[idx++] = '=';
			}
			buffer[idx++] = '=';
		}
		s.write(buffer, 0, idx);
		s.write('\n');
		s.flush();
		idx = 0;
		remain = 0;
		remainIdx = 0;
	}
	
	@Override
	public void write(byte[] s, int offset, int length) throws IOException {
		// the result/encoded string, the padding string, and the pad count
		if (length == 0) return;

		int c = 0;
		// first append to remaining results
		for (; remainIdx > 0 && c < length; c++) {
			write(s[offset + c] & 0xff);
		}
		
		// increment over the length of the string, three characters at a time
		// don't add partial results
		for (; c + 2 < length; c += 3) {
			// these three 8-bit (ASCII) characters become one 24-bit number
			int n = ((s[offset + c] & 0xff) << 16)
					| ((s[offset + c + 1] & 0xff) << 8)
					| (s[offset + c + 2] & 0xff);

			// this 24-bit number gets separated into four 6-bit numbers
			// those four 6-bit numbers are used as indices into the base64
			// character list
			encodeElem(n);
		}
		
		// Add final partial results
		for (; c < length; c++) {
			write(s[offset + c] & 0xff);
		}
	}
	
	@Override
	public void write(int b) throws IOException {
		this.remain |= b << (8 * (2 - remainIdx));
		this.remainIdx = (remainIdx + 1) % 3;
		if (remainIdx == 0) {
			encodeElem(remain);
			this.remain = 0;
		}
	}
	
	public void encodeDouble(double d) throws IOException {
		encodeLong(Double.doubleToLongBits(d));
	}
	
	public void encodeFloat(float f) throws IOException {
		encodeInt(Float.floatToIntBits(f));
	}
	
	public void encodeInt(int i) throws IOException {
		encodeElem((i >>> (remainIdx + 1)*8) | this.remain);
		if (remainIdx == 2) {
			encodeElem(i);
			this.remain = 0;
		} else {
			this.remain = i << (8 * (2 - remainIdx));
		}
		this.remainIdx = (this.remainIdx + 1) % 3;
	}
	public void encodeLong(long l) throws IOException {
		encodeElem(((int)(l >>> (remainIdx + 5)*8)) | this.remain);
		encodeElem((int)((l >>> (remainIdx + 2)*8)));
		int i = (int)l;
		if (remainIdx > 0) {
			encodeElem(i >>> ((this.remainIdx - 1)*8));
		}
		this.remain = i << ((3 - this.remainIdx) % 3 + 1);
		this.remainIdx = (this.remainIdx + 2) % 3;
	}
	
	private void encodeElem(int n) throws IOException {
		startSequence();
		buffer[idx++] = base64chars[(n >>> 18) & 0x3f];
		buffer[idx++] = base64chars[(n >>> 12) & 0x3f];
		buffer[idx++] = base64chars[(n >>> 6) & 0x3f];
		buffer[idx++] = base64chars[n & 0x3f];
	}
	
	@Override
	public void close() throws IOException {
		flush();
		s.close();
	}
}
