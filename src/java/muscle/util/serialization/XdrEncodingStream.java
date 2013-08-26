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
    void xdrEncodeInt(int value) throws IOException;
    void xdrEncodeIntVector(int[] ints) throws IOException;
    void xdrEncodeDynamicOpaque(byte [] opaque) throws IOException;
    void xdrEncodeOpaque(byte [] opaque, int len) throws IOException;
    void xdrEncodeOpaque(byte [] opaque, int offset, int len) throws IOException;
    void xdrEncodeBoolean(boolean bool) throws IOException;
    void xdrEncodeBooleanVector(boolean[] bool) throws IOException;
    void xdrEncodeString(String str) throws IOException;
    void xdrEncodeStringVector(String[] str) throws IOException;
    void xdrEncodeLong(long value) throws IOException;
    void xdrEncodeLongVector(long[] longs) throws IOException;
    void xdrEncodeFloat(float value) throws IOException;
    void xdrEncodeDouble(double value) throws IOException;
    void xdrEncodeFloatVector(float[] value) throws IOException;
    void xdrEncodeFloatFixedVector(float[] value, int length) throws IOException;
    void xdrEncodeDoubleVector(double[] value) throws IOException;
    void xdrEncodeDoubleFixedVector(double[] value, int length) throws IOException;
    void xdrEncodeByteVector(byte[] value) throws IOException;
    void xdrEncodeByteFixedVector(byte[] value, int length) throws IOException;
    void xdrEncodeByte(byte value) throws IOException;
    void xdrEncodeShort(short value) throws IOException;
    void xdrEncodeShortVector(short[] value) throws IOException;
    void xdrEncodeShortFixedVector(short[] value, int length) throws IOException;

	void close() throws IOException;
}
