/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.messaging.serialization;

import java.io.IOException;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrSerializerWrapper implements SerializerWrapper {
	private final XdrEncodingStream xdrOut;

	public XdrSerializerWrapper(XdrEncodingStream xdrOut) {
		this.xdrOut = xdrOut;
	}

	@Override
	public void writeInt(int num) throws IOException {
		try {
			this.xdrOut.xdrEncodeInt(num);
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not write int", ex);
		}
	}

	@Override
	public void writeBoolean(boolean bool) throws IOException {
		try {
			this.xdrOut.xdrEncodeBoolean(bool);
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not write boolean", ex);
		}
	}

	@Override
	public void writeByteArray(byte[] bytes) throws IOException {
		try {
			this.xdrOut.xdrEncodeByteVector(bytes);
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not write bytes", ex);
		}
	}

	@Override
	public void flush() throws IOException {
		try {
			this.xdrOut.endEncoding();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not flush response", ex);
		}
	}
	
	@Override
	public void writeString(String str) throws IOException {
		try {
			this.xdrOut.xdrEncodeString(str);
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not write string", ex);
		}
	}

	@Override
	public void writeDouble(double d) throws IOException {
		try {
			this.xdrOut.xdrEncodeDouble(d);
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not write string", ex);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			this.xdrOut.close();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not close output", ex);
		}
	}
	
	public XdrEncodingStream getXdrEncodingStream() {
		return this.xdrOut;
	}
}
