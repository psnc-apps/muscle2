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
	private final boolean writeXDRlen;

	public SocketChannelOutputStream(SocketChannel sc, int size, boolean direct, boolean writeXDRLength) {
		this.channel = sc;
		this.buffer = direct ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
		this.buffer.order(ByteOrder.BIG_ENDIAN);
		this.buffer.clear();
		
		this.writeXDRlen = writeXDRLength;
		if (this.writeXDRlen) {
			// make room for the header
			buffer.position(4);
		}
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
		if (writeXDRlen) {
			// Send a negative value: we're only sending single fragments.
			final int size = (buffer.position() - 4) | 0x80000000;
			buffer.putInt(0, size);
		}
		
		buffer.flip();
		// Assume a full write
		this.channel.write(buffer);
		buffer.clear();
		
		if (this.writeXDRlen) {
			// make room for the header
			buffer.position(4);
		}
	}
	
	@Override
	public void close() throws IOException {
		flush();
		this.channel.close();
	}
}
