/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.IOException;
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author Joris Borgdorff
 */
public class UnpackerWrapper implements DeserializerWrapper {
	private final Unpacker unpacker;

	public UnpackerWrapper(Unpacker unpack) {
		this.unpacker = unpack;
	}
	
	@Override
	public void refresh() throws IOException {
		// nop
	}
	
	@Override
	public void cleanUp() throws IOException {
		// nop
	}

	@Override
	public int readInt() throws IOException {
		return this.unpacker.readInt();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return this.unpacker.readBoolean();
	}

	@Override
	public byte[] readByteArray() throws IOException {
		return this.unpacker.readByteArray();
	}

	@Override
	public String readString() throws IOException {
		return this.unpacker.readString();
	}

	@Override
	public double readDouble() throws IOException {
		return this.unpacker.readDouble();
	}

	@Override
	public void close() throws IOException {
		this.unpacker.close();
	}
	
	public Unpacker getUnpacker() {
		return this.unpacker;
	}
}
