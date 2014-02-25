/*
 * 
 */

package muscle.monitor;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import muscle.id.Location;
import muscle.net.ProtocolHandler;
import muscle.net.SocketFactory;
import muscle.util.concurrency.SafeThread;
import muscle.util.logging.ActivityProtocol;
import muscle.util.serialization.CustomDeserializer;
import muscle.util.serialization.CustomDeserializerWrapper;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.DirectlyFedInputStream;

/**
 *
 * @author Joris Borgdorff
 */
public class UDPActivityListener extends SafeThread {
	private final DatagramSocket sock;
	private final ActivityController controller;
	private final byte[] buffer;
	private final DirectlyFedInputStream inStream;
	private final DeserializerWrapper in;
	
	public UDPActivityListener(ActivityController viewer) throws SocketException, UnknownHostException {
		super("UDPActivityListener");
		this.controller = viewer;
		this.sock = new DatagramSocket(0, SocketFactory.getMuscleHost());
		System.out.println("Listening on UDP address " + sock.getLocalSocketAddress());
		this.buffer = new byte[1024];
		this.inStream = new DirectlyFedInputStream(buffer);
		this.in = new CustomDeserializerWrapper(new CustomDeserializer(inStream, 1024));
	}

	@Override
	protected void execute() throws Exception {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		sock.receive(packet);
		inStream.reset(packet.getLength());
//		int val = (buffer[0] & 0xff) | ((buffer[1] & 0xff) << 8) | ((buffer[1] & 0xff) << 16) | ((buffer[1] & 0xff) << 24);
//		System.out.println(Integer.toHexString(val) + "; " + val);
//		val = (buffer[4] & 0xff) | ((buffer[5] & 0xff) << 8) | ((buffer[6] & 0xff) << 16) | ((buffer[7] & 0xff) << 24);
//		System.out.println(Integer.toHexString(val) + "; " + val);
		
		in.refresh();
		
		ActivityProtocol activity = ActivityProtocol.handler.read(in);
		
		int hash = in.readInt();
		switch (activity) {
			case INIT:
				Location loc = ProtocolHandler.decodeLocation(in);
				System.out.println(loc);
				controller.addContainer(hash, loc);
				break;
			case FINALIZE:
				controller.removeContainer(hash);
				break;
			default:
				String id = in.readString();
				controller.action(hash, id, activity);
				break;
		}
		in.cleanUp();
	}

	@Override
	protected void handleInterruption(InterruptedException ex) {
		throw new RuntimeException(ex);
	}

	@Override
	protected void handleException(Throwable ex) {
		throw new RuntimeException(ex);
	}
}
