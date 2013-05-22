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
import java.nio.ByteBuffer;

/**
 * Defines interface for encoding XDR stream. An encoding
 * XDR stream receives data in the form of Java data types and writes it to
 * a data sink (for instance, network or memory buffer) in the
 * platform-independent XDR format.
 */
public interface XdrEncodingStream {

    void beginEncoding();
    void endEncoding() throws IOException;
    void xdrEncodeInt(int value);
    void xdrEncodeIntVector(int[] ints);
    void xdrEncodeDynamicOpaque(byte [] opaque);
    void xdrEncodeOpaque(byte [] opaque, int len);
    void xdrEncodeOpaque(byte [] opaque, int offset, int len);
    void xdrEncodeBoolean(boolean bool);
    void xdrEncodeBooleanVector(boolean[] bool);
    void xdrEncodeString(String str);
    void xdrEncodeStringVector(String[] str);
    void xdrEncodeLong(long value);
    void xdrEncodeLongVector(long[] longs);
    void xdrEncodeByteBuffer(ByteBuffer buf);
    void xdrEncodeFloat(float value);
    void xdrEncodeDouble(double value);
    void xdrEncodeFloatVector(float[] value);
    void xdrEncodeFloatFixedVector(float[] value, int length);
    void xdrEncodeDoubleVector(double[] value);
    void xdrEncodeDoubleFixedVector(double[] value, int length);
    void xdrEncodeByteVector(byte[] value);
    void xdrEncodeByteFixedVector(byte[] value, int length);
    void xdrEncodeByte(byte value);
    void xdrEncodeShort(short value);
    void xdrEncodeShortVector(short[] value);
    void xdrEncodeShortFixedVector(short[] value, int length);

	void close() throws IOException;

    /*
     * Fake interface for compatibility with Remote Tea RPC library
     *
     */
}
