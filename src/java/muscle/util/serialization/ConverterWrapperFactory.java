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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import muscle.net.CrossSocketFactory;

import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author Joris Borgdorff
 */
public class ConverterWrapperFactory {
	private final static int DATA_BUFFER_SIZE = 1024*1024;
	private final static int CONTROL_BUFFER_SIZE = 1024;
	private final static boolean isXdr = System.getProperty("muscle.core.serialization.method") != null && System.getProperty("muscle.core.serialization.method").equals("XDR");
	
	public static SerializerWrapper getDataSerializer(Socket s) throws IOException {
		if (isXdr) {
			XdrTcpEncodingStream xdrOut = new XdrTcpEncodingStream(s, DATA_BUFFER_SIZE);
			return new XdrSerializerWrapper(xdrOut, DATA_BUFFER_SIZE);
		} else {
			MessagePack msgPack = new MessagePack();
			OutputStream socketStream = s.getOutputStream();
			OutputStream stream = new BufferedOutputStream(socketStream, DATA_BUFFER_SIZE);
			Packer packer = msgPack.createPacker(stream);
			return new PackerWrapper(packer, stream, socketStream);
		}
	}
	public static SerializerWrapper getControlSerializer(Socket s) throws IOException {
		if (isXdr) {
			XdrTcpEncodingStream xdrOut = new XdrTcpEncodingStream(s, CONTROL_BUFFER_SIZE);
			return new XdrSerializerWrapper(xdrOut, CONTROL_BUFFER_SIZE);
		} else {
			MessagePack msgPack = new MessagePack();
			OutputStream socketStream = s.getOutputStream();
			OutputStream stream = new BufferedOutputStream(socketStream, CONTROL_BUFFER_SIZE);
			Packer packer = msgPack.createPacker(stream);
			return new PackerWrapper(packer, stream, socketStream);
		}
	}
	public static DeserializerWrapper getDataDeserializer(Socket s) throws IOException {
		if (isXdr) {
			XdrTcpDecodingStream xdrIn = new XdrTcpDecodingStream(s, DATA_BUFFER_SIZE);
			return new XdrDeserializerWrapper(xdrIn);
		} else {
			MessagePack msgPack = new MessagePack();
			InputStream socketStream = System.getProperty(CrossSocketFactory.PROP_MTO_TRACE) == null ? s.getInputStream() : 
				new CrossSocketFactory.LoggableInputStream(s.getRemoteSocketAddress().toString(), s.getInputStream()); // * temporary, there is no way to do it in cleaner way */
			Unpacker unpacker = msgPack.createUnpacker(new BufferedInputStream(socketStream, DATA_BUFFER_SIZE));
			unpacker.setArraySizeLimit(Integer.MAX_VALUE);
			unpacker.setMapSizeLimit(Integer.MAX_VALUE);
			// 2 GB
			unpacker.setRawSizeLimit(Integer.MAX_VALUE);
			return new UnpackerWrapper(unpacker);
		}
	}
	public static DeserializerWrapper getControlDeserializer(Socket s) throws IOException {
		if (isXdr) {
			XdrTcpDecodingStream xdrIn = new XdrTcpDecodingStream(s,CONTROL_BUFFER_SIZE);
			return new XdrDeserializerWrapper(xdrIn);
		} else {
			MessagePack msgPack = new MessagePack();
			Unpacker unpacker = msgPack.createUnpacker(new BufferedInputStream(s.getInputStream(), CONTROL_BUFFER_SIZE));
			return new UnpackerWrapper(unpacker);
		}
	}
}
