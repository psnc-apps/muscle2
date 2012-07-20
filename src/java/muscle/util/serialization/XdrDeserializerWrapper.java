/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.IOException;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrDecodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class XdrDeserializerWrapper implements DeserializerWrapper {
	private final XdrDecodingStream xdrIn;
	private boolean isClean = true;

	public XdrDeserializerWrapper(XdrDecodingStream xdrIn) {
		this.xdrIn = xdrIn;
	}
	
	@Override
	public void refresh() throws IOException {
		this.cleanUp();
		try {
			xdrIn.beginDecoding();
			this.isClean = false;
		} catch (OncRpcException ex) {
			throw new IOException("Sending side hung up", ex);
		}
	}

	@Override
	public void cleanUp() throws IOException {
		if (!isClean) {
			try {
				xdrIn.endDecoding();
				this.isClean = true;
			} catch (OncRpcException ex) {
				throw new IllegalStateException("Can not clean up", ex);
			}
		}
	}

	@Override
	public int readInt() throws IOException {
		try {
			return xdrIn.xdrDecodeInt();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not an int", ex);
		}
	}

	@Override
	public boolean readBoolean() throws IOException {
		try {
			return xdrIn.xdrDecodeBoolean();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not a boolean", ex);
		}
	}

	@Override
	public byte[] readByteArray() throws IOException {
		try {
			return xdrIn.xdrDecodeByteVector();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not a byte array", ex);
		}
	}
	
	@Override
	public String readString() throws IOException {
		try {
			return xdrIn.xdrDecodeString();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not a string", ex);
		}
	}
	
	@Override
	public double readDouble() throws IOException {
		try {
			return xdrIn.xdrDecodeDouble();
		} catch (OncRpcException ex) {
			throw new IllegalStateException("Message is depleted or next value is not an int", ex);
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
