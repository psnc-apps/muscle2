/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.messaging.serialization;

import java.io.IOException;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrDeserializerWrapper implements DeserializerWrapper {
	private final XdrDecodingStream xdrIn;

	public XdrDeserializerWrapper(XdrDecodingStream xdrIn) {
		this.xdrIn = xdrIn;
	}
	
	@Override
	public void refresh() throws IOException {
		try {
			xdrIn.beginDecoding();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not refresh", ex);
		}
	}

	@Override
	public int readInt() throws IOException {
		try {
			return xdrIn.xdrDecodeInt();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not read int", ex);
		}
	}

	@Override
	public boolean readBoolean() throws IOException {
		try {
			return xdrIn.xdrDecodeBoolean();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not read boolean", ex);
		}
	}

	@Override
	public byte[] readByteArray() throws IOException {
		try {
			return xdrIn.xdrDecodeByteVector();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not read bytes", ex);
		}
	}
	
	@Override
	public String readString() throws IOException {
		try {
			return xdrIn.xdrDecodeString();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not read string", ex);
		}
	}
	
	@Override
	public double readDouble() throws IOException {
		try {
			return xdrIn.xdrDecodeDouble();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not read double", ex);
		}
	}

	public XdrDecodingStream getXdrDecodingStream() {
		return this.xdrIn;
	}

	@Override
	public void close() throws IOException {
		try {
			this.xdrIn.close();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Can not close input", ex);
		}
	}
}
