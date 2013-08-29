/*
 * 
 */

package muscle.util.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 *
 * @author Joris Borgdorff
 */
public class SocketChannelInputStream extends InputStream {
	private final ByteBuffer buffer;
	private final SocketChannel channel;

	public SocketChannelInputStream(SocketChannel sc, int size) {
		this.channel = sc;
		this.buffer = ByteBuffer.allocateDirect(size);
		this.buffer.limit(0);
	}
	
	private boolean fill() throws IOException {
		if (buffer.hasRemaining()) {
			return true;
		} else {
			buffer.clear();
			int bytesRead = this.channel.read(buffer);
			if (bytesRead == -1) {
				this.channel.close();
				return false;
			} else {
				buffer.flip();
				return true;
			}
		}
	}
	
	@Override
	public int read() throws IOException {
		if (!fill()) return -1;
		return (buffer.get() & 0xff);
	}
	
	@Override
	public int read(byte[] data, int offset, int len) throws IOException {
		if (!fill()) return -1;
		final int numBytes = Math.min(len, buffer.remaining());
		buffer.get(data, offset, numBytes);
		return numBytes;
	}
	
	@Override
	public int available() throws IOException {
		if (!fill()) return 0;
		return buffer.remaining();
	}
	
	@Override
	public long skip(long n) throws IOException {
		long remain = n;
		while (remain > 0) {
			if (!fill()) return n - remain;
			
			final int remainInBuffer = (int)Math.min(remain, buffer.remaining());
			buffer.position(buffer.position() + remainInBuffer);
			remain -= remainInBuffer;
		}
		return n;
	}
	
	@Override
	public void close() throws IOException {
		this.channel.close();
	}
}
