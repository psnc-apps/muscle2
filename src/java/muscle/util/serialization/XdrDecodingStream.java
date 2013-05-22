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
 * Defines interface for decoding XDR stream. A decoding
 * XDR stream returns data in the form of Java data types which it reads
 * from a data source (for instance, network or memory buffer) in the
 * platform-independent XDR format.
 */
public interface XdrDecodingStream {


    void beginDecoding() throws IOException;
    void endDecoding() throws IOException;
    int xdrDecodeInt() throws IOException;
    int[] xdrDecodeIntVector() throws IOException;
    byte[] xdrDecodeDynamicOpaque() throws IOException;
    byte[] xdrDecodeOpaque(int size) throws IOException;
    void xdrDecodeOpaque(byte[] data, int offset, int len) throws IOException;
    boolean xdrDecodeBoolean() throws IOException;
    boolean[] xdrDecodeBooleanVector() throws IOException;
    String xdrDecodeString() throws IOException;
	String[] xdrDecodeStringVector() throws IOException;
    long xdrDecodeLong() throws IOException;
    long[] xdrDecodeLongVector() throws IOException;
    ByteBuffer xdrDecodeByteBuffer() throws IOException;
    float xdrDecodeFloat() throws IOException;
    double xdrDecodeDouble() throws IOException;
    double[] xdrDecodeDoubleVector() throws IOException;
    double[] xdrDecodeDoubleFixedVector(int length) throws IOException;
    float[] xdrDecodeFloatVector() throws IOException;
    float[] xdrDecodeFloatFixedVector(int length) throws IOException;
    byte[] xdrDecodeByteVector() throws IOException;
    byte[] xdrDecodeByteFixedVector(int length) throws IOException;
    byte xdrDecodeByte() throws IOException;
    short xdrDecodeShort() throws IOException;
    short[] xdrDecodeShortVector() throws IOException;
    short[] xdrDecodeShortFixedVector(int length) throws IOException;
	void close() throws IOException;
    /*
     * Fake interface for compatibility with Remote Tea RPC library
     *
     */
}
