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

public class CustomDeserializer {
	private final static Logger logger = Logger.getLogger(CustomDeserializer.class.getName());
	private final static int NEGATIVE_BIT = 0x80000000;
	private final byte[] buffer;
	private int idx, limit;
	private final InputStream in;

	/**
	 * Byte buffer used by XDR record.
	 */
	private int fragmentRemaining;
	private boolean lastFragment;

	/**
	 * Create a new Xdr object with a buffer of given size.
	 *
	 * @param in inputstream to read the bytes of the object from
	 * @param bufsize of the buffer in bytes
	 */
	public CustomDeserializer(InputStream in, int bufsize) {
		buffer = new byte[bufsize];
		idx = limit = 0;
		this.in = in;
		fragmentRemaining = 0;
		lastFragment = false;
	}

	public void beginDecoding() throws IOException {
		/*
		 * Set position to the beginning of this XDR in back end buffer.
		 */
		fill(4);
		logger.log(Level.FINEST, "Begin decoding {0} bytes", fragmentRemaining);
	}
	
	private void fill(int n) throws IOException {
		if (fragmentRemaining == 0) {
			// In case there is no more data in the current XDR record
			// (since we already saw the last fragment), throw an exception.
			if (lastFragment) throw new BufferUnderflowException();

			read(4);
			final int fragmentHead = (buffer[idx++] & 0xff) | ((buffer[idx++] & 0xff) << 8) | ((buffer[idx++] & 0xff) << 16) | ((buffer[idx++] & 0xff) << 24);

			// XDR header is the last one if tfhe sign is negative
			// and the other bits are the size of the fragment
			fragmentRemaining = fragmentHead & ~NEGATIVE_BIT;
			lastFragment = (fragmentRemaining != fragmentHead);

//			logger.log(Level.FINEST, "Fragment remaining: {0}", fragmentRemaining);

			// Sanity check on incomming fragment length:
			// There are ONC/RPC implementations that send
			// empty trailing fragments, so we accept them here.
			assert( fragmentRemaining != 0 || lastFragment);
		}
		if (fragmentRemaining < n)
			throw new BufferUnderflowException();
		
		read(n);
	}
	
	public void endDecoding() throws IOException {
		// Clear all buffers that are still remaining
		while (true) {
			if (fragmentRemaining > 0) {
				if (fragmentRemaining <= limit - idx) {
					idx += fragmentRemaining;
				} else {
					int skipped = limit - idx;
					idx = limit = 0;
					if (skipped + in.skip(fragmentRemaining - skipped) != fragmentRemaining) {
						throw new IOException("Stream for XDR does not do full seek");
					}
				}
				fragmentRemaining = 0;
			}
			
			if (lastFragment) {
				lastFragment = false;
				break;
			} else {
				fill(0);
			}
		}
	}

	/**
	 * Get next array of integers.
	 *
	 * @return the array on integers
	 * @throws java.io.IOException 
	 */
	public int[] decodeIntArray() throws IOException {
		final int len = decodeInt();
		int[] ints = new int[len];
		for (int i = 0; i < len; ++i) {
			ints[i] = decodeInt();
		}
		return ints;
	}

	/**
	 * Get next array of long.
	 *
	 * @return the array on integers
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public long[] decodeLongArray() throws IOException {
		final int len = decodeInt();
		long[] longs = new long[len];
		for (int i = 0; i < len; i++) {
			longs[i] = decodeLong();
		}
		return longs;
	}

	/**
	 * Decodes (aka "deserializes") a float (which is a 32 bits wide floating
	 * point entity) read from a XDR stream.
	 *
	 * @return Decoded float values.
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public float decodeFloat() throws IOException {
		return Float.intBitsToFloat(decodeInt());
	}

	/**
	 * Decodes (aka "deserializes") a double (which is a 64 bits wide floating
	 * point entity) read from a XDR stream.
	 *
	 * @return Decoded double values.
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public double decodeDouble() throws IOException {
		return Double.longBitsToDouble(decodeLong());
	}

	/**
	 * Decodes (aka "deserializes") a vector of doubles read from a XDR stream.
	 *
	 * @return Decoded double vector.
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public double[] decodeDoubleArray() throws IOException {
		final int length = decodeInt();
		double[] value = new double[length];
		for (int i = 0; i < length; ++i) {
			value[i] = decodeDouble();
		}
		return value;
	}

	/**
	 * Decodes (aka "deserializes") a vector of floats read from a XDR stream.
	 *
	 * @return Decoded float vector.
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public float[] decodeFloatArray() throws IOException {
		final int length = decodeInt();
		float[] value = new float[length];
		for (int i = 0; i < length; ++i) {
			value[i] = decodeFloat();
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
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public void decodeByteArray(byte[] buf, int offset, int len) throws IOException {
		int bytesDecoded = 0;
		while (bytesDecoded < len) {
			fill(0);
			int cp;
			int bytesInFragment = Math.min(fragmentRemaining, len - bytesDecoded);
			if (idx < limit) {
				cp = Math.min(bytesInFragment, limit - idx);
				System.arraycopy(buffer, idx, buf, offset + bytesDecoded, cp);
				idx += cp;
			} else if (bytesInFragment > 1024) { // read directly from socket
				cp = in.read(buf, offset + bytesDecoded, bytesInFragment);
			} else { // read through buffer
				read(1);
				cp = 0;
			}
			fragmentRemaining -= cp;
			bytesDecoded += cp;
		}
	}

	/**
	 * Decodes (aka "deserializes") a XDR opaque value, which is represented
	 * by a vector of byte values. The length of the opaque value to decode
	 * is pulled off of the XDR stream, so the caller does not need to know
	 * the exact length in advance. The decoded data is always padded to be
	 * a multiple of four (because that's what the sender does).
	 * @return decoded bytes, not necessarily a multiple of four
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public byte [] decodeByteArray() throws IOException {
		final int length = decodeInt();
//		logger.log(Level.FINE, "Decoding byte array of length {0}", length);
		byte[] value = new byte[length];
		decodeByteArray(value, 0, length);
		return value;
	}

	/**
	 * Get next String.
	 *
	 * @return decoded string
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public String decodeString() throws IOException {
		final int len = decodeInt();
		
		if (len > 0) {
			byte[] bytes = new byte[len];
			decodeByteArray(bytes, 0, len);
			return new String(bytes);
		} else {
			return "";
		}
	}
	
	public String[] decodeStringArray() throws IOException {
		final int len = decodeInt();
		String[] strs = new String[len];
		for (int i = 0; i < len; i++) {
			strs[i] = decodeString();
		}
		return strs; 
	}

	public boolean decodeBoolean() throws IOException {
		return decodeByte() != (byte)0;
	}

	/**
	 * Decodes (aka "deserializes") a long (which is called a "hyper" in XDR
	 * babble and is 64&nbsp;bits wide) read from a XDR stream.
	 *
	 * @return Decoded long value.
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public long decodeLong() throws IOException {
		fill(8);
		fragmentRemaining -= 8;
		return (buffer[idx++] & 0xffL) | ((buffer[idx++] & 0xffL) << 8) | ((buffer[idx++] & 0xffL) << 16) | ((buffer[idx++] & 0xffL) << 24) | ((buffer[idx++] & 0xffL) << 32) | ((buffer[idx++] & 0xffL) << 40) | ((buffer[idx++] & 0xffL) << 48) | ((buffer[idx++] & 0xffL) << 56);
	}

	/**
	 * Decodes (aka "deserializes") a byte read from this XDR stream.
	 *
	 * @return Decoded byte value.
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public byte decodeByte() throws IOException {
		fill(1);
		fragmentRemaining -= 1;
		return buffer[idx++];
	}

	/**
	 * Decodes (aka "deserializes") a short (which is a 16 bit quantity) read
	 * from this XDR stream.
	 *
	 * @return Decoded short value.
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public short decodeShort() throws IOException {
		fill(2);
		fragmentRemaining -= 2;
		return (short)((buffer[idx++] & 0xff) | ((buffer[idx++] & 0xff) << 8));
	}

	/**
	 * Decodes (aka "deserializes") a vector of short integers read from a XDR
	 * stream.
	 *
	 * @return Decoded vector of short integers..
	 * @throws java.io.IOException if the underlying stream has an IOException
	 */
	public short[] decodeShortArray() throws IOException {
		final int length = decodeInt();
		short[] value = new short[length];
		for (int i = 0; i < length; ++i) {
			value[i] = decodeShort();
		}
		return value;
	}

	public boolean[] decodeBooleanArray() throws IOException {
		int len = decodeInt();
		boolean[] arr = new boolean[len];
		for (int i = 0; i < len; ++i) {
			arr[i] = decodeBoolean();
		}
		return arr;
	}

	public String[] xdrDecodeStringArray() throws IOException {
		int len = decodeInt();
		String[] arr = new String[len];
		for (int i = 0; i < len; ++i) {
			arr[i] = decodeString();
		}
		return arr;
	}

	public int decodeInt() throws IOException {
		fill(4);
		fragmentRemaining -= 4;
//		logger.log(Level.FINE, "buffer pointer: {0}; buffer size: {1}; fragment remaining: {2}", new Object[]{idx, limit, fragmentRemaining});
		return (buffer[idx++] & 0xff) | ((buffer[idx++] & 0xff) << 8) | ((buffer[idx++] & 0xff) << 16) | ((buffer[idx++] & 0xff) << 24);
	}
	
	private void read(int minimal) throws IOException {
		if (limit - idx < minimal) {
			if (idx > 0) {
				if (idx < limit) {
					// Move the remaining buffer to the start
					System.arraycopy(buffer, idx, buffer, 0, limit - idx);
				}
				limit -= idx;
				idx = 0;
			}
			do {
				final int bytesRead = in.read(buffer, limit, buffer.length - limit);
				if (bytesRead == -1) throw new EOFException("Cannot fill buffer");
				limit += bytesRead;
//				logger.log(Level.FINE, "Buffer size {0}; buffer read: {1}; buffer pointer: {2}", new Object[]{buffer.length, limit, idx});
			} while (limit < minimal);
		}
	}
}
