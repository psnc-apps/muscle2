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

public class XdrOut implements XdrEncodingStream {
	private final byte[] longbytes = new byte[8];
	
	/**
	 * Byte buffer used by XDR record.
	 */
	protected final OutputStream out;
	
	/**
	 * Create a new Xdr object with a buffer of given size.
	 *
	 * @param size of the buffer in bytes
	 */
	public XdrOut(OutputStream out) {
		this.out = out;
	}

	public void beginEncoding() {
		// nop
	}

	public void endEncoding() throws IOException {
		out.flush();
	}

	/**
	 * Encodes (aka "serializes") a "XDR int" value and writes it down a
	 * XDR stream. A XDR int is 32 bits wide -- the same width Java's "int"
	 * data type has. This method is one of the basic methods all other
	 * methods can rely on.
	 */
	public void xdrEncodeInt(final int value) throws IOException {
		longbytes[0] = (byte)((value >> 24) & 0xff);
		longbytes[1] = (byte)((value >> 16) & 0xff);
		longbytes[2] = (byte)((value >>  8) & 0xff);
		longbytes[3] = (byte)(value & 0xff);
		out.write(longbytes, 0, 4);
	}

	/**
	 * Encodes (aka "serializes") a vector of ints and writes it down
	 * this XDR stream.
	 *
	 * @param values int vector to be encoded.
	 *
	 */
	public void xdrEncodeIntVector(int[] values) throws IOException {
		xdrEncodeInt(values.length);
		for (int value: values) {
			xdrEncodeInt( value );
		}
	}

	/**
	 * Encodes (aka "serializes") a vector of longs and writes it down
	 * this XDR stream.
	 *
	 * @param values long vector to be encoded.
	 *
	 */
	public void xdrEncodeLongVector(long[] values) throws IOException {
		xdrEncodeInt(values.length);
		for (long value : values) {
			xdrEncodeLong(value);
		}
	}

	/**
	 * Encodes (aka "serializes") a float (which is a 32 bits wide floating
	 * point quantity) and write it down this XDR stream.
	 *
	 * @param value Float value to encode.
	 */
	public void xdrEncodeFloat(float value) throws IOException {
		xdrEncodeInt(Float.floatToIntBits(value));
	}

	/**
	 * Encodes (aka "serializes") a double (which is a 64 bits wide floating
	 * point quantity) and write it down this XDR stream.
	 *
	 * @param value Double value to encode.
	 */
	public void xdrEncodeDouble(double value) throws IOException {
		xdrEncodeLong(Double.doubleToLongBits(value));
	}

	/**
	 * Encodes (aka "serializes") a vector of floats and writes it down this XDR
	 * stream.
	 *
	 * @param value float vector to be encoded.
	 */
	public void xdrEncodeFloatVector(float[] value) throws IOException {
		xdrEncodeInt(value.length);
		xdrEncodeFloatFixedVector(value, value.length);
	}

	/**
	 * Encodes (aka "serializes") a vector of floats and writes it down this XDR
	 * stream.
	 *
	 * @param value float vector to be encoded.
	 * @param length of vector to write. This parameter is used as a sanity
	 * check.
	 */
	public void xdrEncodeFloatFixedVector(float[] value, int length) throws IOException {
		assert (value.length == length);
		
		for (float f : value) {
			xdrEncodeFloat(f);
		}
	}

	/**
	 * Encodes (aka "serializes") a vector of doubles and writes it down this
	 * XDR stream.
	 *
	 * @param value double vector to be encoded.
	 */
	public void xdrEncodeDoubleVector(double[] value) throws IOException {
		xdrEncodeInt(value.length);
		xdrEncodeDoubleFixedVector(value, value.length);
	}

	/**
	 * Encodes (aka "serializes") a vector of doubles and writes it down this
	 * XDR stream.
	 *
	 * @param value double vector to be encoded.
	 * @param length of vector to write. This parameter is used as a sanity
	 * check.
	 */
	public void xdrEncodeDoubleFixedVector(double[] value, int length) throws IOException {
		assert (value.length == length);
		for (double d : value) {
			xdrEncodeDouble(d);
		}
	}

	/**
	 * Encodes (aka "serializes") a string and writes it down this XDR stream.
	 *
	 */
	public void xdrEncodeString(String string) throws IOException {
		xdrEncodeDynamicOpaque(string == null ? new byte[] {} : string.getBytes());
	}

	private static final byte [] paddingZeros = { 0, 0, 0 };

	/**
	 * Encodes (aka "serializes") a XDR opaque value, which is represented
	 * by a vector of byte values. Only the opaque value is encoded, but
	 * no length indication is preceeding the opaque value, so the receiver
	 * has to know how long the opaque value will be. The encoded data is
	 * always padded to be a multiple of four. If the length of the given byte
	 * vector is not a multiple of four, zero bytes will be used for padding.
	 */
	public void xdrEncodeOpaque(byte[] bytes, int offset, int len) throws IOException {
		out.write(bytes, offset, len);
		int padding = (4 - (len & 3)) & 3;
		out.write(paddingZeros, 0, padding);
	}

	public void xdrEncodeOpaque(byte[] bytes, int len) throws IOException {
		xdrEncodeOpaque(bytes, 0, len);
	}

	/**
	 * Encodes (aka "serializes") a XDR opaque value, which is represented
	 * by a vector of byte values. The length of the opaque value is written
	 * to the XDR stream, so the receiver does not need to know
	 * the exact length in advance. The encoded data is always padded to be
	 * a multiple of four to maintain XDR alignment.
	 *
	 */
	public void xdrEncodeDynamicOpaque(byte [] opaque) throws IOException {
		xdrEncodeInt(opaque.length);
		xdrEncodeOpaque(opaque, 0, opaque.length);
	}

	public void xdrEncodeBoolean(boolean bool) throws IOException {
		xdrEncodeByte((byte)(bool ? 1 : 0));
	}

	/**
	 * Encodes (aka "serializes") a long (which is called a "hyper" in XDR
	 * babble and is 64&nbsp;bits wide) and write it down this XDR stream.
	 */
	public void xdrEncodeLong(long value) throws IOException {
		longbytes[0] = (byte)((value >> 56) & 0xffL);
		longbytes[1] = (byte)((value >> 48) & 0xffL);
		longbytes[2] = (byte)((value >> 40) & 0xffL);
		longbytes[3] = (byte)((value >> 32) & 0xffL);
		longbytes[4] = (byte)((value >> 24) & 0xffL);
		longbytes[5] = (byte)((value >> 16) & 0xffL);
		longbytes[6] = (byte)((value >>  8) & 0xffL);
		longbytes[7] = (byte)( value        & 0xffL);
		out.write(longbytes, 0, 8);
	}

	/**
	 * Encodes (aka "serializes") a vector of bytes, which is nothing more than
	 * a series of octets (or 8 bits wide bytes), each packed into its very own
	 * 4 bytes (XDR int). Byte vectors are encoded together with a preceeding
	 * length value. This way the receiver doesn't need to know the length of
	 * the vector in advance.
	 *
	 * @param value Byte vector to encode.
	 */
	public void xdrEncodeByteVector(byte[] value) throws IOException {
		xdrEncodeInt(value.length);
		xdrEncodeByteFixedVector(value, value.length);
	}

	/**
	 * Encodes (aka "serializes") a vector of bytes, which is nothing more than
	 * a series of octets (or 8 bits wide bytes), each packed into its very own
	 * 4 bytes (XDR int).
	 *
	 * @param value Byte vector to encode.
	 * @param length of vector to write. This parameter is used as a sanity
	 * check.
	 */
	public void xdrEncodeByteFixedVector(byte[] value, int length) throws IOException {
		assert(value.length == length);

		longbytes[0] = longbytes[1] = longbytes[2] = 0;
		for (byte b : value) {
			longbytes[3] =  b;
			out.write(longbytes, 0, 4);
		}
	}

	/**
	 * Encodes (aka "serializes") a byte and write it down this XDR stream.
	 *
	 * @param value Byte value to encode.
	 *
	 * @throws OncRpcException if an ONC/RPC error occurs.
	 * @throws IOException if an I/O error occurs.
	 */
	public void xdrEncodeByte(byte value) throws IOException {
		longbytes[0] = longbytes[1] = longbytes[2] = 0;
		longbytes[3] =  value;
		out.write(longbytes, 0, 4);
	}

	/**
	 * Encodes (aka "serializes") a short (which is a 16 bits wide quantity) and
	 * write it down this XDR stream.
	 *
	 * @param value Short value to encode.
	 */
	public void xdrEncodeShort(short value) throws IOException {
		longbytes[0] = longbytes[1] = 0;
		longbytes[2] = (byte)((value >> 8) & 0xff);
		longbytes[3] = (byte)( value         & 0xff);
		out.write(longbytes, 0, 4);
	}

	/**
	 * Encodes (aka "serializes") a vector of short integers and writes it down
	 * this XDR stream.
	 *
	 * @param value short vector to be encoded.
	 */
	public void xdrEncodeShortVector(short[] value) throws IOException {
		xdrEncodeInt(value.length);
		xdrEncodeShortFixedVector(value, value.length);
	}

	/**
	 * Encodes (aka "serializes") a vector of short integers and writes it down
	 * this XDR stream.
	 *
	 * @param value short vector to be encoded.
	 * @param length of vector to write. This parameter is used as a sanity
	 * check.
	 */
	public void xdrEncodeShortFixedVector(short[] value, int length) throws IOException {
		assert (value.length == length);
		
		longbytes[0] = longbytes[1] = 0;
		for (short s : value) {
			longbytes[2] = (byte)((s >> 8) & 0xff);
			longbytes[3] = (byte)( s         & 0xff);
			out.write(longbytes, 0, 4);
		}
	}
	
	
	@Override
	public void xdrEncodeBooleanVector(boolean[] bool) throws IOException {
		xdrEncodeInt(bool.length);

		longbytes[0] = longbytes[1] = longbytes[2] = 0;
		for (boolean b : bool) {
			longbytes[3] =  (byte)(b ? 1 : 0);
			out.write(longbytes, 0, 4);
		}
	}

	@Override
	public void xdrEncodeStringVector(String[] str) throws IOException {
		xdrEncodeInt(str.length);
		for (String value: str) {
			xdrEncodeString( value );
		}
	}

	public void close() {
		// nop
	}
}
