package muscle.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import muscle.id.Location;
import muscle.net.ProtocolHandler;
import muscle.util.data.SerializableDatatype;
import muscle.util.serialization.CustomSerializer;
import muscle.util.serialization.CustomSerializerWrapper;
import muscle.util.serialization.SerializerWrapper;

/**
 *
 * @author joris
 */
public class UDPActivityLogger extends ActivityWriter {
	private final SerializerWrapper out;
	private final ByteArrayOutputStream buffer;
	private final Location loc;
	private final int locHash;
	private final DatagramSocket sock;
	private final SocketAddress addr;
	
	public UDPActivityLogger(Location loc, SocketAddress addr) throws IOException {
		this.loc = loc;
		this.locHash = loc.hashCode();
		buffer = new ByteArrayOutputStream(100);
		out = new CustomSerializerWrapper(new CustomSerializer(buffer, 1024), 1024);
		sock = new DatagramSocket();
		this.addr = addr;
	}

	private void send() throws IOException {
		out.flush();
		byte[] data = buffer.toByteArray();
		buffer.reset();
		DatagramPacket packet = new DatagramPacket(data, data.length, addr);
		sock.send(packet);
	}
	
	@Override
	protected synchronized void write(ActivityProtocol action, String id, int sec, int nano) throws IOException {
		ActivityProtocol.handler.write(out, action);
		out.writeInt(locHash);
		out.writeString(id);
		out.writeInt(sec);
		out.writeInt(nano);
		send();
	}

	@Override
	public synchronized void dispose(int sec, int nano) throws IOException {
		ActivityProtocol.handler.write(out, ActivityProtocol.FINALIZE);
		out.writeInt(locHash);
		out.writeInt(sec);
		out.writeInt(nano);
		send();
		sock.close();
		out.close();
	}

	@Override
	protected synchronized void init(long sec, int milli) throws IOException {
		ActivityProtocol.handler.write(out, ActivityProtocol.INIT);
		out.writeInt(locHash);
		ProtocolHandler.encodeLocation(out, loc);
		out.writeValue(Long.valueOf(sec), SerializableDatatype.LONG);
		out.writeInt(milli);
		send();
	}
}
