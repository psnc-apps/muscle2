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

public class CustomSerializer {
	/**
	 * Byte buffer used by XDR record.
	 */
	private final static int NEGATIVE_BIT = 0x80000000;
	private final static int BUFFER_THRESHOLD = 1024;
	private final byte[] buffer;
	private int idx;
	private final OutputStream out;
	
	/**
	 * Create a new Xdr object with a buffer of given size.
	 *
	 * @param out stream to write the XDR data to
	 * @param bufsize of the buffer in bytes
	 */
	public CustomSerializer(OutputStream out, int bufsize) {
		buffer = (bufsize < BUFFER_THRESHOLD) ? new byte[BUFFER_THRESHOLD] : new byte[bufsize];
		idx = 4;
		this.out = out;
	}

	public void flush() throws IOException {
		final int size = idx - 4;
		idx = 0;
		// Last fragment
		encode(size | NEGATIVE_BIT);
		out.write(buffer, 0, size + 4);
		out.flush();
	}
	
	private void writeBuffer(int minimal) throws IOException {
		if (idx + minimal >= buffer.length) {
			final int size = idx - 4;
			idx = 0;
			// Last fragment
			encode(size);
			out.write(buffer, 0, size + 4);
		}
	}

	/**
	 * Encodes (aka "serializes") a "XDR int" value and writes it down a
	 * XDR stream. A XDR int is 32 bits wide -- the same width Java's "int"
	 * data type has. This method is one of the basic methods all other
	 * methods can rely on.
	 */
	public void encode(final int value) throws IOException {
		writeBuffer(4);
		buffer[idx++] = (byte)(value & 0xff);
		buffer[idx++] = (byte)((value >>  8) & 0xff);
		buffer[idx++] = (byte)((value >> 16) & 0xff);
		buffer[idx++] = (byte)((value >> 24) & 0xff);
	}

	/**
	 * Encodes (aka "serializes") a vector of ints and writes it down
	 * this stream.
	 *
	 * @param values int vector to be encoded.
	 *
	 */
	public void encode(int[] values) throws IOException {
		encode(values.length);
		for (int value: values) {
			encode(value);
		}
	}

	/**
	 * Encodes (aka "serializes") a vector of longs and writes it down
	 * this XDR stream.
	 *
	 * @param values long vector to be encoded.
	 *
	 */
	public void encode(long[] values) throws IOException {
		encode(values.length);
		for (long value : values) {
			encode(value);
		}
	}

	/**
	 * Encodes (aka "serializes") a float (which is a 32 bits wide floating
	 * point quantity) and write it down this XDR stream.
	 *
	 * @param value Float value to encode.
	 */
	public void encode(float value) throws IOException {
		encode(Float.floatToIntBits(value));
	}

	/**
	 * Encodes (aka "serializes") a double (which is a 64 bits wide floating
	 * point quantity) and write it down this XDR stream.
	 *
	 * @param value Double value to encode.
	 */
	public void encode(double value) throws IOException {
		encode(Double.doubleToLongBits(value));
	}

	/**
	 * Encodes (aka "serializes") a vector of floats and writes it down this XDR
	 * stream.
	 *
	 * @param value float vector to be encoded.
	 */
	public void encode(float[] value) throws IOException {
		encode(value.length);
		for (float f : value) {
			encode(f);
		}
	}

	/**
	 * Encodes (aka "serializes") a vector of doubles and writes it down this
	 * XDR stream.
	 *
	 * @param value double vector to be encoded.
	 */
	public void encode(double[] value) throws IOException {
		encode(value.length);
		for (double d : value) {
			encode(d);
		}
	}

	/**
	 * Encodes (aka "serializes") a string and writes it down this XDR stream.
	 *
	 */
	public void encode(String string) throws IOException {
		encode(string == null ? new byte[] {} : string.getBytes());
	}

	/**
	 * Encodes (aka "serializes") part of a byte array. The length of the byte array is written
	 * to the stream, so the receiver does not need to know
	 * the exact length in advance.
	 */
	public void encode(byte[] bytes, int offset, int length) throws IOException {
		encode(length);
		// Copy small packages, but send larger ones directly
		if (length < BUFFER_THRESHOLD && length + idx < buffer.length) {
			System.arraycopy(bytes, offset, buffer, idx, length);
			idx += length;
		} else {
			final int oldidx = idx;
			final int size = idx - 4 + length;
			idx = 0;
			encode(size); // idx is set back to 4
			out.write(buffer, 0, oldidx);
			out.write(bytes, offset, length);
		}
	}

	/**
	 * Encodes (aka "serializes") a byte array. The length of the byte array is written
	 * to the stream, so the receiver does not need to know
	 * the exact length in advance.
	 */
	public void encode(byte[] bytes) throws IOException {
		encode(bytes, 0, bytes.length);
	}

	public void encode(boolean bool) throws IOException {
		encode((byte)(bool ? 1 : 0));
	}

	/**
	 * Encodes (aka "serializes") a long (which is called a "hyper" in XDR
	 * babble and is 64&nbsp;bits wide) and write it down this XDR stream.
	 */
	public void encode(long value) throws IOException {
		writeBuffer(8);
		buffer[idx++] = (byte)( value        & 0xffL);
		buffer[idx++] = (byte)((value >>  8) & 0xffL);
		buffer[idx++] = (byte)((value >> 16) & 0xffL);
		buffer[idx++] = (byte)((value >> 24) & 0xffL);
		buffer[idx++] = (byte)((value >> 32) & 0xffL);
		buffer[idx++] = (byte)((value >> 40) & 0xffL);
		buffer[idx++] = (byte)((value >> 48) & 0xffL);
		buffer[idx++] = (byte)((value >> 56) & 0xffL);
	}
	
	/**
	 * Encodes (aka "serializes") a byte and write it down this XDR stream.
	 *
	 * @param value Byte value to encode.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void encode(byte value) throws IOException {
		writeBuffer(1);
		buffer[idx++] = value;
	}

	/**
	 * Encodes (aka "serializes") a short (which is a 16 bits wide quantity) and
	 * write it down this XDR stream.
	 *
	 * @param value Short value to encode.
	 */
	public void encode(short value) throws IOException {
		writeBuffer(2);
		buffer[idx++] = (byte)(value & 0xff);
		buffer[idx++] = (byte)((value >>  8) & 0xff);
	}

	/**
	 * Encodes (aka "serializes") a vector of short integers and writes it down
	 * this XDR stream.
	 *
	 * @param value short vector to be encoded.
	 */
	public void encode(short[] value) throws IOException {
		encode(value.length);
		for (short s : value) {
			encode(s);
		}
	}
	
	public void encode(boolean[] bool) throws IOException {
		encode(bool.length);
		for (boolean b : bool) {
			encode(b);
		}
	}

	public void encode(String[] str) throws IOException {
		encode(str.length);
		for (String value : str) {
			encode(value);
		}
	}
}
