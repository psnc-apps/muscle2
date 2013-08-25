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
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XdrIn implements XdrDecodingStream {

//	private final static int SIZE_OF_LONG = Long.SIZE / 8;
//	private final static int SIZE_OF_INT = Integer.SIZE / 8;

	private final static Logger logger = Logger.getLogger(XdrIn.class.getName());
	private final byte[] longbuffer = new byte[8];
	
	/**
	 * Byte buffer used by XDR record.
	 */
	protected final InputStream in;
	private int fragmentLength, fragmentPos;
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
		fragmentLength = 0;
		fragmentPos = 0;
		fill();
		logger.log(Level.FINER, "Begin decoding {0} bytes", fragmentLength);
	}
	
	private int readInt() throws IOException {
		if (in.read(longbuffer, 0, 4) != 4)
			throw new IOException("Could not read 4 bytes in XDR");
		
		return (longbuffer[0] & 0xff) << 24 | (longbuffer[1] & 0xff) << 16 | (longbuffer[2] & 0xff) << 8 | (longbuffer[3] & 0xff);
	}
		
	private void fill() throws IOException {
		if (fragmentLength == fragmentPos) {
			if ( lastFragment ) {
				// In case there is no more data in the current XDR record
				// (as we already saw the last fragment), throw an exception.
				throw new IOException("Buffer underflow");
			}
			// Reset buffer to 0
			fragmentLength = readInt();
			fragmentPos = 0;
			
			// XDR header is the last one if the sign is negative (& 0x80000000);
			if ((fragmentLength & 0x80000000) != 0) {
				// the rest is the length of the fragment
				fragmentLength &= 0x7FFFFFFF;
				lastFragment = true;
			} else {
				lastFragment = false;
			}
			
			// Sanity check on incomming fragment length: the length must
			// be at least four bytes long, otherwise this fragment does
			// not make sense. There are ONC/RPC implementations that send
			// empty trailing fragments, so we accept them here.
			// Also check for fragment lengths which are not a multiple of
			// four -- and thus are invalid.
			assert( (fragmentLength & 3) == 0 );
			assert( fragmentLength != 0 || lastFragment);
		}
	}
	
	public void endDecoding() throws IOException {
		while (true) {
			while (fragmentLength > fragmentPos) {
				fragmentPos += in.skip(fragmentLength - fragmentPos);
			}
			if (lastFragment) {
				break;
			} else {
				fill();
			}
		}
		
		lastFragment = false;
	}

	/**
	 * Get next array of integers.
	 *
	 * @return the array on integers
	 */
	public int[] xdrDecodeIntVector() throws IOException {
		int len = xdrDecodeInt();
//		logger.log(Level.FINEST, "Decoding int array with len = {0}", len);
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
		int len = xdrDecodeInt();		
		
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
		int length = xdrDecodeInt();
		
		double[] value = new double[length];
		for (int i = 0; i < length; ++i) {
			value[i] = xdrDecodeDouble();
		}
		return value;
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
		int length = xdrDecodeInt();

		float[] value = new float[length];
		for (int i = 0; i < length; ++i) {
			value[i] = xdrDecodeFloat();
		}
		return value;
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
		int padding = (4 - (len & 3)) & 3;

		while (len > 0) {
			fill();
			final int numRead = in.read(buf, offset, Math.min(fragmentLength - fragmentPos, len));
			fragmentPos += numRead;
			len -= numRead;
			offset += numRead;
		}

		fragmentPos += in.skip(padding);
	}

	public void xdrDecodeOpaque(byte[] buf,  int len) throws IOException {
		xdrDecodeOpaque(buf, 0, len);
	}

	public byte[] xdrDecodeOpaque(int len) throws IOException {
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
		int length = xdrDecodeInt();
		byte [] opaque = new byte[length];
		if ( length != 0 ) {
			xdrDecodeOpaque(opaque, 0, length);
		}
		return opaque;
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
		fill();
		fragmentPos += 4;
		in.skip(3);
		return (in.read() != 0);
	}

	/**
	 * Decodes (aka "deserializes") a long (which is called a "hyper" in XDR
	 * babble and is 64&nbsp;bits wide) read from a XDR stream.
	 *
	 * @return Decoded long value.
	 */
	public long xdrDecodeLong() throws IOException {
		if (fragmentLength - fragmentPos >= 8) {
			if (in.read(longbuffer, 0, 8) != 8)
				throw new IOException("Could not read 8 bytes in XDR");
		
			fragmentPos += 8;
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
		int length = xdrDecodeInt();
		
		byte[] bytes = new byte[length];

		for (int i = 0; i < length; ++i) {
			bytes[i] = xdrDecodeByte();
		}
		return bytes;
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
	public byte[] xdrDecodeByteFixedVector(int length) throws IOException {
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
		fragmentPos += 4;
		in.skip(3);
		return (byte)in.read();
	}

	/**
	 * Decodes (aka "deserializes") a short (which is a 16 bit quantity) read
	 * from this XDR stream.
	 *
	 * @return Decoded short value.
	 */
	public short xdrDecodeShort() throws IOException {
		fill();
		fragmentPos += 4;
		in.skip(2);
		return (short)((in.read() << 8) | in.read());
	}

	/**
	 * Decodes (aka "deserializes") a vector of short integers read from a XDR
	 * stream.
	 *
	 * @return Decoded vector of short integers..
	 */
	public short[] xdrDecodeShortVector() throws IOException {
		int length = xdrDecodeInt();
		short[] value = new short[length];
		for (int i = 0; i < length; ++i) {
			value[i] = xdrDecodeShort();
		}
		return value;
	}

	/**
	 * Decodes (aka "deserializes") a vector of short integers read from a XDR
	 * stream.
	 *
	 * @param length of vector to read.
	 *
	 * @return Decoded vector of short integers.
	 */
	public short[] xdrDecodeShortFixedVector(int length) throws IOException {
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
		for (int i = 0; i < len; i++) {
			arr[i] = xdrDecodeBoolean();
		}
		return arr;
	}

	@Override
	public String[] xdrDecodeStringVector() throws IOException {
		int len = xdrDecodeInt();
		logger.log(Level.FINEST, "Decoding String array with len = {0}", len);

		String[] arr = new String[len];
		for (int i = 0; i < len; i++) {
			arr[i] = xdrDecodeString();
		}
		return arr;
	}

	@Override
	public int xdrDecodeInt() throws IOException {
		fill();
		fragmentPos += 4;
		return readInt();
	}
}
