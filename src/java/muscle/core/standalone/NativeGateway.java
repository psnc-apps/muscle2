package muscle.core.standalone;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;


public class NativeGateway  extends Thread {
	protected ServerSocket ss;
	protected CallListener listener;
	protected static final Logger logger = Logger.getLogger(NativeGateway.class.getName());
	
	public NativeGateway(CallListener listener) throws UnknownHostException, IOException {
		ss = new ServerSocket(0, 1, InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1}));
		
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
		public void sendDouble(String entranceName, double data[]);
		/* OPCODE = 5 */
		public double[] receiveDouble(String exitName);
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
		try {
			Socket s = ss.accept();
			
			logger.log(Level.FINE, "Accepted connection from: {0}:{1}", new Object[]{s.getRemoteSocketAddress(), s.getPort()});
			
			XdrTcpDecodingStream xdrIn =  new XdrTcpDecodingStream(s, 64 * 1024);
			XdrTcpEncodingStream xdrOut = new XdrTcpEncodingStream(s, 64 * 1024);
			
			while (true) {
				logger.finest("Starting decoding...");
				xdrIn.beginDecoding();

				int operationCode = xdrIn.xdrDecodeInt();
				logger.log(Level.FINEST, "Operation code = {0}", operationCode);
				
				switch (operationCode) {
					case 0:
					{
						logger.finest("finalize() request.");
						xdrIn.close();
						xdrOut.close();
						listener.isFinished();
						logger.finest("Native Process Gateway exiting...");
						return;
					}	
					case 1:
					{
						logger.finest("getKernelName() request.");
						xdrOut.xdrEncodeString(listener.getKernelName());
						logger.log(Level.FINEST, "Kernel name sent : {0}", listener.getKernelName());
						break;
					}
					case 2:
					{
						logger.finest("getProperty() request.");
						String value = listener.getProperty(xdrIn.xdrDecodeString());
						xdrOut.xdrEncodeString(value);
						logger.log(Level.FINEST, "Property value sent: {0}", value);
						break;
					}
					case 3:
					{
						logger.finest("willStop() request.");
						boolean stop = listener.willStop();
						xdrOut.xdrEncodeBoolean(stop);
						logger.log(Level.FINEST, "Stop?: {0}", stop);
						break;
					}
					case 4:
					{
						logger.finest("sendDouble() request.");
						String entranceName = xdrIn.xdrDecodeString();
						double[] doubleA = xdrIn.xdrDecodeDoubleVector();
						logger.log(Level.FINEST, "entranceName = {0}, array lenght = {1}", new Object[]{entranceName, doubleA.length});
						listener.sendDouble(entranceName, doubleA);
						logger.finest("data sent");
						break;
					}
					case 5:
					{
						logger.finest("receiveDouble() request.");
						String exitName = xdrIn.xdrDecodeString();
						logger.log(Level.FINEST, "exitName = {0}", exitName);
						double[] doubleA =  listener.receiveDouble(exitName);
						logger.log(Level.FINEST, "exitName = {0}, array lenght = {1}", new Object[]{exitName, doubleA.length});
						xdrOut.xdrEncodeDoubleVector(doubleA);
						logger.finest("data encoded");
						break;
					}
					case 6:
					{
						logger.finest("getProperties() request.");
						xdrOut.xdrEncodeString(listener.getProperties());
						break;
					}
					case 7:
					{
						logger.finest("getTmpPath() request.");
						xdrOut.xdrEncodeString(listener.getTmpPath());
						break;
					}
					default:
						throw new IOException("Unknown operation code " + operationCode);	
				}
				logger.finest("flushing response");
				xdrOut.endEncoding();
				
				logger.finest("operation decoded.");
				xdrIn.endDecoding();
				
			}
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Communication error", ex);
		} catch (OncRpcException ex) {
			logger.log(Level.SEVERE, "XDR Enc/Dec exception", ex);
		} catch (Throwable ex) {
			logger.log(Level.SEVERE, "Could not finish communication with native code.", ex);
		}
		
	}

}
