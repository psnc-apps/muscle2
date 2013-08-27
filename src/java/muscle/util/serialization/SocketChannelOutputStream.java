/*
 * 
 */

package muscle.util.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

/**
 *
 * @author Joris Borgdorff
 */
public class SocketChannelOutputStream extends OutputStream {
	private final ByteBuffer buffer;
	private final SocketChannel channel;
	
	public SocketChannelOutputStream(SocketChannel sc, int size) {
		this.channel = sc;
		this.buffer = ByteBuffer.allocateDirect(size);
		this.buffer.order(ByteOrder.BIG_ENDIAN);
		this.buffer.clear();
	}

	@Override
	public void write(byte[] b, int offset, int len) {
		buffer.put(b, offset, len);
	}
	
	@Override
	public void write(byte[] b) {
		buffer.put(b, 0, b.length);
	}
	
	@Override
	public void write(int b) {
		buffer.put((byte)(b & 0xff));
	}
	
	@Override
	public void flush() throws IOException {
		buffer.flip();
		// Assume a full write
		this.channel.write(buffer);
		buffer.clear();
	}
	
	@Override
	public void close() throws IOException {
		flush();
		this.channel.close();
	}
}
