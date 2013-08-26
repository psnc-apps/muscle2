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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XdrIn implements XdrDecodingStream {
	private final static Logger logger = Logger.getLogger(XdrIn.class.getName());
	private final byte[] longbuffer = new byte[8];
	
	/**
	 * Byte buffer used by XDR record.
	 */
	protected final InputStream in;
	private int fragmentRemaining;
	private boolean lastFragment;

	/**
	 * Create a new Xdr object with a buffer of given size.
	 *
	 * @param size of the buffer in bytes
	 */
	public XdrIn(InputStream in) {
		this.in = in;
	}

	public void beginDecoding() throws IOException {
		/*
		 * Set position to the beginning of this XDR in back end buffer.
		 */
		fragmentRemaining = 0;
		lastFragment = false;
		fill();
		logger.log(Level.FINER, "Begin decoding {0} bytes", fragmentRemaining);
	}
	
	private int readInt() throws IOException {
		if (in.read(longbuffer, 0, 4) != 4)
			throw new IOException("Could not read 4 bytes in XDR");
		return (longbuffer[0] & 0xff) << 24 | (longbuffer[1] & 0xff) << 16 | (longbuffer[2] & 0xff) << 8 | (longbuffer[3] & 0xff);
	}
	
	private void skip(final long n) throws IOException {
		if (in.skip(n) != n) {
			throw new IOException("Stream for XDR does not do full seek");
		}
	}
	
	private void fill() throws IOException {
		if (fragmentRemaining == 0) {
			if ( lastFragment ) {
				// In case there is no more data in the current XDR record
				// (as we already saw the last fragment), throw an exception.
				throw new IOException("Buffer underflow");
			}
			
			fragmentRemaining = readInt();
			
			logger.log(Level.FINE, "Fragment remaining: {0}", fragmentRemaining);
			
			// XDR header is the last one if the sign is negative (& 0x80000000);
			if ((fragmentRemaining & 0x80000000) != 0) {
				// the rest is the length of the fragment
				fragmentRemaining &= 0x7FFFFFFF;
				lastFragment = true;
			} else {
				lastFragment = false;
			}
			
			logger.log(Level.FINE, "Fragment remaining: {0}", fragmentRemaining);
			
			// Sanity check on incomming fragment length: the length must
			// be at least four bytes long, otherwise this fragment does
			// not make sense. There are ONC/RPC implementations that send
			// empty trailing fragments, so we accept them here.
			// Also check for fragment lengths which are not a multiple of
			// four -- and thus are invalid.
			assert( (fragmentRemaining & 3) == 0 );
			assert( fragmentRemaining != 0 || lastFragment);
		}
	}
	
	public void endDecoding() throws IOException {
		// Clear all buffers that are still remaining
		while (true) {
			if (fragmentRemaining > 0) {
				skip(fragmentRemaining);
				fragmentRemaining = 0;
			}
			if (lastFragment) {
				break;
			} else {
				fill();
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

		while (len > 0) {
			fill();
			final int numRead = in.read(buf, offset, Math.min(fragmentRemaining, len));
			if (numRead == -1) throw new EOFException("XDR stream closed.");
			fragmentRemaining -= numRead;
			len -= numRead;
			offset += numRead;
		}

		if (padding > 0) {
			skip(padding);
			fragmentRemaining -= padding;
		}
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
		int len = xdrDecodeInt();
		logger.log(Level.FINEST, "Decoding string with len = {0}", len);

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
			if (in.read(longbuffer, 0, 8) != 8)
				throw new IOException("Could not read 8 bytes in XDR");
		
			fragmentRemaining -= 8;
			return (longbuffer[0] & 0xffL) << 56 | (longbuffer[1] & 0xffL) << 48 | (longbuffer[2] & 0xffL) << 40 | (longbuffer[3] & 0xffL) << 32 | (longbuffer[4] & 0xffL) << 24 | (longbuffer[5] & 0xffL) << 16 | (longbuffer[6] & 0xffL) << 8 | (longbuffer[7] & 0xffL);
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
		fill();
		fragmentRemaining -= 4;
		skip(3);
		final int b = in.read();
		if (b == -1) throw new EOFException("XDR stream ended");
		return (byte)b;
	}

	/**
	 * Decodes (aka "deserializes") a short (which is a 16 bit quantity) read
	 * from this XDR stream.
	 *
	 * @return Decoded short value.
	 */
	public short xdrDecodeShort() throws IOException {
		fill();
		fragmentRemaining -= 4;
		skip(2);
		int ret = in.read();
		if (ret == -1) throw new EOFException("XDR stream ended");
		int ret2 = in.read();
		if (ret2 == -1) throw new EOFException("XDR stream ended");
		return (short)((ret << 8) | ret2);
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
		fill();
		fragmentRemaining -= 4;
		return readInt();
	}
}