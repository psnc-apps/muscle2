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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XdrIn implements XdrDecodingStream {
	private final static Logger logger = Logger.getLogger(XdrIn.class.getName());
	private final static int NEGATIVE_BIT = 0x80000000;
	private final XdrBuffer buffer;
	
	/**
	 * Byte buffer used by XDR record.
	 */
	private int fragmentRemaining;
	private boolean lastFragment;

	/**
	 * Create a new Xdr object with a buffer of given size.
	 *
	 * @param size of the buffer in bytes
	 */
	public XdrIn(InputStream in, int bufsize) {
		this.buffer = new XdrBuffer(in, bufsize);
	}

	public void beginDecoding() throws IOException {
		/*
		 * Set position to the beginning of this XDR in back end buffer.
		 */
		fragmentRemaining = 0;
		lastFragment = false;
		fill(4);
		logger.log(Level.FINEST, "Begin decoding {0} bytes", fragmentRemaining);
	}
	
	private void fill(int n) throws IOException {
		if (fragmentRemaining == 0) {
			// In case there is no more data in the current XDR record
			// (since we already saw the last fragment), throw an exception.
			if (lastFragment) throw new BufferUnderflowException();
			
			buffer.read(4);
			final int fragmentHead = buffer.getInt();
			
			// XDR header is the last one if the sign is negative
			// and the other bits are the size of the fragment
			fragmentRemaining = fragmentHead & ~NEGATIVE_BIT;
			lastFragment = (fragmentRemaining != fragmentHead);
						
			logger.log(Level.FINEST, "Fragment remaining: {0}", fragmentRemaining);
			
			// Sanity check on incomming fragment length: the length must
			// be at least four bytes long, otherwise this fragment does
			// not make sense. There are ONC/RPC implementations that send
			// empty trailing fragments, so we accept them here.
			// Also check for fragment lengths which are not a multiple of
			// four -- and thus are invalid.
			assert( (fragmentRemaining & 3) == 0 );
			assert( fragmentRemaining != 0 || lastFragment);
		}
		buffer.read(n);
	}
	
	public void endDecoding() throws IOException {
		// Clear all buffers that are still remaining
		while (true) {
			if (fragmentRemaining > 0) {
				if (buffer.skip(fragmentRemaining) != fragmentRemaining) {
					throw new IOException("Stream for XDR does not do full seek");
				}
				fragmentRemaining = 0;
			}
			if (lastFragment) {
				break;
			} else {
				fill(4);
			}
		}
	}

	/**
	 * Get next array of integers.
	 *
	 * @return the array on integers
	 */
	public int[] xdrDecodeIntVector() throws IOException {
		final int len = xdrDecodeInt();
		int[] ints = new int[len];
		for (int i = 0; i < len; ++i) {
			ints[i] = xdrDecodeInt();
		}
		return ints;
	}

	/**
	 * Get next array of long.
	 *
	 * @return the array on integers
	 */
	public long[] xdrDecodeLongVector() throws IOException {
		final int len = xdrDecodeInt();
		long[] longs = new long[len];
		for (int i = 0; i < len; i++) {
			longs[i] = xdrDecodeLong();
		}
		return longs;
	}

	/**
	 * Decodes (aka "deserializes") a float (which is a 32 bits wide floating
	 * point entity) read from a XDR stream.
	 *
	 * @return Decoded float value.rs.
	 */
	public float xdrDecodeFloat() throws IOException {
		return Float.intBitsToFloat(xdrDecodeInt());
	}

	/**
	 * Decodes (aka "deserializes") a double (which is a 64 bits wide floating
	 * point entity) read from a XDR stream.
	 *
	 * @return Decoded double value.rs.
	 */
	public double xdrDecodeDouble() throws IOException {
		return Double.longBitsToDouble(xdrDecodeLong());
	}

	/**
	 * Decodes (aka "deserializes") a vector of doubles read from a XDR stream.
	 *
	 * @return Decoded double vector.
	 */
	public double[] xdrDecodeDoubleVector() throws IOException {
		final int length = xdrDecodeInt();
		return xdrDecodeDoubleFixedVector(length);
	}

	/**
	 * Decodes (aka "deserializes") a vector of doubles read from a XDR stream.
	 *
	 * @param length of vector to read.
	 *
	 * @return Decoded double vector..
	 */
	public double[] xdrDecodeDoubleFixedVector(int length) throws IOException {
		double[] value = new double[length];
		for (int i = 0; i < length; ++i) {
			value[i] = xdrDecodeDouble();
		}
		return value;
	}

	/**
	 * Decodes (aka "deserializes") a vector of floats read from a XDR stream.
	 *
	 * @return Decoded float vector.
	 */
	public float[] xdrDecodeFloatVector() throws IOException {
		final int length = xdrDecodeInt();
		return xdrDecodeFloatFixedVector(length);
	}

	/**
	 * Decodes (aka "deserializes") a vector of floats read from a XDR stream.
	 *
	 * @param length of vector to read.
	 *
	 * @return Decoded float vector.
	 */
	public float[] xdrDecodeFloatFixedVector(int length) throws IOException {
		float[] value = new float[length];
		for (int i = 0; i < length; ++i) {
			value[i] = xdrDecodeFloat();
		}
		return value;
	}

	/**
	 * Get next opaque data.  The decoded data
	 * is always padded to be a multiple of four.
	 *
	 * @param buf buffer where date have to be stored
	 * @param offset in the buffer.
	 * @param len number of bytes to read.
	 */
	public void xdrDecodeOpaque(byte[] buf, int offset, int len) throws IOException {		
		final int padding = (4 - (len & 3)) & 3;

		int bytesDecoded = 0;
		while (bytesDecoded < len) {
			fill(0);
			final int cp = buffer.getPart(buf, offset + bytesDecoded, Math.min(fragmentRemaining, len - bytesDecoded));
			fragmentRemaining -= cp;
			bytesDecoded += cp;
		}

		buffer.skip(padding);
		fragmentRemaining -= padding;
	}

	public void xdrDecodeOpaque(byte[] buf, int len) throws IOException {
		xdrDecodeOpaque(buf, 0, len);
	}

	public byte[] xdrDecodeOpaque(final int len) throws IOException {
		byte[] opaque = new byte[len];
		xdrDecodeOpaque(opaque, 0, len);
		return opaque;
	}

	/**
	 * Decodes (aka "deserializes") a XDR opaque value, which is represented
	 * by a vector of byte values. The length of the opaque value to decode
	 * is pulled off of the XDR stream, so the caller does not need to know
	 * the exact length in advance. The decoded data is always padded to be
	 * a multiple of four (because that's what the sender does).
	 */
	public byte [] xdrDecodeDynamicOpaque() throws IOException {
		final int length = xdrDecodeInt();
		return xdrDecodeOpaque(length);
	}

	/**
	 * Get next String.
	 *
	 * @return decoded string
	 */
	public String xdrDecodeString() throws IOException {
		final int len = xdrDecodeInt();
		
		if (len > 0) {
			byte[] bytes = new byte[len];
			xdrDecodeOpaque(bytes, 0, len);
			return new String(bytes);
		} else {
			return "";
		}
	}

	public boolean xdrDecodeBoolean() throws IOException {
		return xdrDecodeByte() != (byte)0;
	}

	/**
	 * Decodes (aka "deserializes") a long (which is called a "hyper" in XDR
	 * babble and is 64&nbsp;bits wide) read from a XDR stream.
	 *
	 * @return Decoded long value.
	 */
	public long xdrDecodeLong() throws IOException {
		if (fragmentRemaining >= 8) {
			// Only try to read in one assignment if the long is in a single fragment
			buffer.read(8);
			fragmentRemaining -= 8;
			return buffer.getLong();
		} else {
			// The & is necessery to make sure the int is read as unsigned
			return ((xdrDecodeInt() & 0xffffffffL) << 32) | (xdrDecodeInt() & 0xffffffffL);
		}
	}

	/**
	 * Decodes (aka "deserializes") a vector of bytes, which is nothing more
	 * than a series of octets (or 8 bits wide bytes), each packed into its very
	 * own 4 bytes (XDR int). Byte vectors are decoded together with a
	 * preceeding length value. This way the receiver doesn't need to know the
	 * length of the vector in advance.
	 *
	 * @return The byte vector containing the decoded data.
	 */
	public byte[] xdrDecodeByteVector() throws IOException {
		final int length = xdrDecodeInt();
		return xdrDecodeByteFixedVector(length);
	}

	/**
	 * Decodes (aka "deserializes") a vector of bytes, which is nothing more
	 * than a series of octets (or 8 bits wide bytes), each packed into its very
	 * own 4 bytes (XDR int).
	 *
	 * @param length of vector to read.
	 *
	 * @return The byte vector containing the decoded data.
	 */
	public byte[] xdrDecodeByteFixedVector(final int length) throws IOException {
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; ++i) {
			bytes[i] = xdrDecodeByte();
		}
		return bytes;
	}

	/**
	 * Decodes (aka "deserializes") a byte read from this XDR stream.
	 *
	 * @return Decoded byte value.
	 */
	public byte xdrDecodeByte() throws IOException {
		fill(4);
		fragmentRemaining -= 4;
		return buffer.getByte();
	}

	/**
	 * Decodes (aka "deserializes") a short (which is a 16 bit quantity) read
	 * from this XDR stream.
	 *
	 * @return Decoded short value.
	 */
	public short xdrDecodeShort() throws IOException {
		fill(4);
		fragmentRemaining -= 4;
		return buffer.getShort();
	}

	/**
	 * Decodes (aka "deserializes") a vector of short integers read from a XDR
	 * stream.
	 *
	 * @return Decoded vector of short integers..
	 */
	public short[] xdrDecodeShortVector() throws IOException {
		final int length = xdrDecodeInt();
		return xdrDecodeShortFixedVector(length);
	}

	/**
	 * Decodes (aka "deserializes") a vector of short integers read from a XDR
	 * stream.
	 *
	 * @param length of vector to read.
	 *
	 * @return Decoded vector of short integers.
	 */
	public short[] xdrDecodeShortFixedVector(final int length) throws IOException {
		short[] value = new short[length];
		for (int i = 0; i < length; ++i) {
			value[i] = xdrDecodeShort();
		}
		return value;
	}

	public void close() {
		// nop
	}

	@Override
	public boolean[] xdrDecodeBooleanVector() throws IOException {
		int len = xdrDecodeInt();
		boolean[] arr = new boolean[len];
		for (int i = 0; i < len; ++i) {
			arr[i] = xdrDecodeBoolean();
		}
		return arr;
	}

	@Override
	public String[] xdrDecodeStringVector() throws IOException {
		int len = xdrDecodeInt();
		String[] arr = new String[len];
		for (int i = 0; i < len; ++i) {
			arr[i] = xdrDecodeString();
		}
		return arr;
	}

	@Override
	public int xdrDecodeInt() throws IOException {
		fill(4);
		fragmentRemaining -= 4;
		return buffer.getInt();
	}
	
	private static class XdrBuffer {
		private final byte[] buffer;
		private int idx, limit;
		private final InputStream in;

		public XdrBuffer(InputStream in, int bufsize) {
			buffer = new byte[bufsize];
			idx = limit = 0;
			this.in = in;
		}

		public void read(int minimal) throws IOException {
			if (limit - idx < minimal) {
				if (idx > 0) {
					if (idx < limit) {
						System.arraycopy(buffer, idx, buffer, 0, limit - idx);
					}
					limit -= idx;
					idx = 0;
				}
				do {
					// Move the remaining buffer to the start
					final int bytesRead = in.read(buffer, limit, buffer.length - limit);
					if (bytesRead == -1) throw new EOFException("Can not fill buffer");
					limit += bytesRead;
				} while (limit < minimal);
			}
		}

		public long skip(long n) throws IOException {
			if (n <= size()) {
				idx += n;
				return n;
			} else {
				int skipped = size();
				idx = limit = 0;
				return skipped + in.skip(n - skipped);
			}
		}

		public long getLong() {
			return ((buffer[idx++] & 0xffL) << 56) | ((buffer[idx++] & 0xffL) << 48) | ((buffer[idx++] & 0xffL) << 40) | ((buffer[idx++] & 0xffL) << 32) | ((buffer[idx++] & 0xffL) << 24) | ((buffer[idx++] & 0xffL) << 16) | ((buffer[idx++] & 0xffL) << 8) | (buffer[idx++] & 0xffL);
		}

		public int getInt() {
			return ((buffer[idx++] & 0xff) << 24) | ((buffer[idx++] & 0xff) << 16) | ((buffer[idx++] & 0xff) << 8) | (buffer[idx++] & 0xff);
		}

		public short getShort() {
			idx += 4;
			return (short)(((buffer[idx - 2] & 0xff) << 8) | (buffer[idx - 1] & 0xff));
		}

		public byte getByte() {
			idx += 4;
			return buffer[idx - 1];
		}

		public int getPart(byte[] b, int offset, int length) throws IOException {
			if (idx == limit) {
				return in.read(b, offset, length);
			} else {
				final int cp = Math.min(length, limit - idx);
				System.arraycopy(buffer, idx, b, offset, cp);
				idx += cp;
				return cp;
			}
		}

		public int size() {
			return limit - idx;
		}
	}
}
