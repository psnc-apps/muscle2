/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.messaging.serialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
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
			return new XdrSerializerWrapper(xdrOut);
		} else {
			MessagePack msgPack = new MessagePack();
			OutputStream stream = new BufferedOutputStream(s.getOutputStream(), DATA_BUFFER_SIZE);
			Packer packer = msgPack.createPacker(stream);
			return new PackerWrapper(packer, stream);
		}
	}
	public static SerializerWrapper getControlSerializer(Socket s) throws IOException {
		if (isXdr) {
			XdrTcpEncodingStream xdrOut = new XdrTcpEncodingStream(s, CONTROL_BUFFER_SIZE);
			return new XdrSerializerWrapper(xdrOut);
		} else {
			MessagePack msgPack = new MessagePack();
			OutputStream stream = new BufferedOutputStream(s.getOutputStream(), CONTROL_BUFFER_SIZE);
			Packer packer = msgPack.createPacker(stream);
			return new PackerWrapper(packer, stream);
		}
	}
	public static DeserializerWrapper getDataDeserializer(Socket s) throws IOException {
		if (isXdr) {
			XdrTcpDecodingStream xdrIn = new XdrTcpDecodingStream(s, DATA_BUFFER_SIZE);
			return new XdrDeserializerWrapper(xdrIn);
		} else {
			MessagePack msgPack = new MessagePack();
			Unpacker unpacker = msgPack.createUnpacker(new BufferedInputStream(s.getInputStream(), DATA_BUFFER_SIZE));
			unpacker.setArraySizeLimit(DATA_BUFFER_SIZE);
			unpacker.setMapSizeLimit(DATA_BUFFER_SIZE);
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
