package muscle.core.standalone;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.data.SerializableData;
import muscle.util.serialization.DeserializerWrapper;
import muscle.util.serialization.SerializerWrapper;
import muscle.util.serialization.Xdr;
import muscle.util.serialization.XdrDeserializerWrapper;
import muscle.util.serialization.XdrNIODeserializerWrapper;
import muscle.util.serialization.XdrNIOSerializerWrapper;
import muscle.util.serialization.XdrSerializerWrapper;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;


public class NativeGateway  extends Thread {
	protected final ServerSocket ss;
	protected final CallListener listener;
	protected static final Logger logger = Logger.getLogger(NativeGateway.class.getName());
	private final static boolean USE_ASYNC = true;
	
	public NativeGateway(CallListener listener) throws UnknownHostException, IOException {
		super("NativeGateway-" + listener.getKernelName());
		
		if (USE_ASYNC) {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ss = ssc.socket();
			ss.bind(new InetSocketAddress(InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1}), 0), 1);
		} else {
			ss = new ServerSocket(0, 1, InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1}));		
		}
		
		this.listener = listener;
		
		setDaemon(true);
	}
	
	public interface CallListener {
		/* OPCODE = 0 */
		public void isFinished();
		/* OPCODE = 1 */
		public String getKernelName();
		/* OPCODE = 2 */
		public String getProperty(String name);
		/* OPCODE = 3 */
		public boolean willStop();
		/* OPCODE = 4 */
		public void send(String entranceName, SerializableData data);
		/* OPCODE = 5 */
		public SerializableData receive(String exitName);
		/* OPCODE = 6 */
		public String getProperties();
		/* OPCODE = 7 */
		public String getTmpPath();
			
		}

	public int getPort() {
		return ss.getLocalPort();
		
		//return "tcp://" + ss.getInetAddress() + ":" + ss.getLocalPort();
	}
	public InetAddress getInetAddress() {
		return ss.getInetAddress();
	
		//return "tcp://" + ss.getInetAddress() + ":" + ss.getLocalPort();
	}
	
	@Override
	public void run() {
		Socket s = null;
		DeserializerWrapper in = null;
		SerializerWrapper out = null;
		try {
			if (USE_ASYNC) {
				SocketChannel sc = ss.getChannel().accept();
				sc.configureBlocking(false);
				s = sc.socket();

				int buffer_size = XdrNIOSerializerWrapper.DEFAULT_BUFFER_SIZE;
				in =  new XdrNIODeserializerWrapper(new Xdr(sc, buffer_size));
				out = new XdrNIOSerializerWrapper(new Xdr(sc, buffer_size), buffer_size);
			} else {
				s = ss.accept();

				int buffer_size = XdrSerializerWrapper.DEFAULT_BUFFER_SIZE;
				in =  new XdrDeserializerWrapper(new XdrTcpDecodingStream(s, buffer_size));
				out = new XdrSerializerWrapper(new XdrTcpEncodingStream(s, buffer_size), buffer_size);
			}
			
			logger.log(Level.FINE, "Accepted connection from: {0}:{1}", new Object[]{s.getRemoteSocketAddress(), s.getPort()});
			
			while (true) {
				logger.finest("Starting decoding...");
				in.refresh();

				int operationCode = in.readInt();
				logger.log(Level.FINEST, "Operation code = {0}", operationCode);
				
				switch (operationCode) {
					case 0:
					{
						logger.finest("finalize() request.");
						in.close();
						out.close();
						listener.isFinished();
						logger.finest("Native Process Gateway exiting...");
						return;
					}	
					case 1:
					{
						logger.finest("getKernelName() request.");
						out.writeString(listener.getKernelName());
						logger.log(Level.FINEST, "Kernel name sent : {0}", listener.getKernelName());
						break;
					}
					case 2:
					{
						logger.finest("getProperty() request.");
						String value = listener.getProperty(in.readString());
						out.writeString(value);
						logger.log(Level.FINEST, "Property value sent: {0}", value);
						break;
					}
					case 3:
					{
						logger.finest("willStop() request.");
						boolean stop = listener.willStop();
						out.writeBoolean(stop);
						logger.log(Level.FINEST, "Stop?: {0}", stop);
						break;
					}
					case 4:
					{
						logger.finest("sendDouble() request.");
						String entranceName = in.readString();
						SerializableData data = SerializableData.parseData(in);
						logger.log(Level.FINEST, "entranceName = {0}, data = {1}", new Object[]{entranceName, data});
						listener.send(entranceName, data);
						logger.finest("data sent");
						break;
					}
					case 5:
					{
						logger.finest("receiveDouble() request.");
						String exitName = in.readString();
						logger.log(Level.FINEST, "exitName = {0}", exitName);
						SerializableData data =  listener.receive(exitName);
						logger.log(Level.FINEST, "exitName = {0}, data = {1}", new Object[]{exitName, data});
						data.encodeData(out);
						logger.finest("data encoded");
						break;
					}
					case 6:
					{
						logger.finest("getProperties() request.");
						out.writeString(listener.getProperties());
						break;
					}
					case 7:
					{
						logger.finest("getTmpPath() request.");
						out.writeString(listener.getTmpPath());
						break;
					}
					default:
						throw new IOException("Unknown operation code " + operationCode);	
				}
				logger.finest("flushing response");
				out.flush();
				
				logger.finest("proceeding to next native call");
				in.cleanUp();
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Communication error: " + ex, ex);
		} catch (Throwable ex) {
			logger.log(Level.SEVERE, listener.getKernelName() + " could not finish communication with native code: " + ex, ex);
		} finally {
			listener.isFinished();
			if (s != null) {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ex1) {
						Logger.getLogger(NativeGateway.class.getName()).log(Level.SEVERE, listener.getKernelName() + "could not close communications with native code", ex1);
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (Exception ex1) {
						Logger.getLogger(NativeGateway.class.getName()).log(Level.SEVERE, listener.getKernelName() + "could not close communications with native code", ex1);
					}
				}
				try {
					s.close();
				} catch (IOException ex1) {
					Logger.getLogger(NativeGateway.class.getName()).log(Level.SEVERE, listener.getKernelName() + "could not close communications with native code", ex1);
				}
			}
		}
	}
}
