package muscle.core.standalone;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;


public class NativeGateway  extends Thread {
	protected ServerSocket ss;
	protected CallListener listener;
	protected static Logger log = Logger.getLogger("muscle.native.protocol");
	
	public NativeGateway(CallListener listener) throws UnknownHostException, IOException {
		ss = new ServerSocket(0, 1, InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1}));
		
		this.listener = listener;
		
		setDaemon(true);
	}
	
	public interface CallListener {
		
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
	
	public String getContactInformation() {
		return String.valueOf(ss.getLocalPort());
		
		//return "tcp://" + ss.getInetAddress() + ":" + ss.getLocalPort();
	}
	
	@Override
	public void run() {
		try {
			Socket s = ss.accept();
			
			log.fine("Accepted connection from: " +  s.getRemoteSocketAddress() + ":" + s.getPort());
			
			XdrTcpDecodingStream xdrIn =  new XdrTcpDecodingStream(s, 64 * 1024);
			XdrTcpEncodingStream xdrOut = new XdrTcpEncodingStream(s, 64 * 1024);
			
			while (true) {
				log.finest("Starting decoding...");
				xdrIn.beginDecoding();

				int operationCode = xdrIn.xdrDecodeInt();
				log.finest("Operation code = " + operationCode);
				
				switch (operationCode) {
					case 0:
					{
						log.finest("finalize() request.");
						xdrIn.close();
						xdrOut.close();
						log.finest("Native Process Gateway exiting...");
						return;
					}	
					case 1:
					{
						log.finest("getKernelName() request.");
						xdrOut.xdrEncodeString(listener.getKernelName());
						log.finest("Kernel name sent : " + listener.getKernelName());
						break;
					}
					case 2:
					{
						log.finest("getProperty() request.");
						String value = listener.getProperty(xdrIn.xdrDecodeString());
						xdrOut.xdrEncodeString(value);
						log.finest("Property value sent: " + value);
						break;
					}
					case 3:
					{
						log.finest("willStop() request.");
						boolean stop = listener.willStop();
						xdrOut.xdrEncodeBoolean(stop);
						log.finest("Stop?: " + stop);
						break;
					}
					case 4:
					{
						log.finest("sendDouble() request.");
						String entranceName = xdrIn.xdrDecodeString();
						double[] doubleA = xdrIn.xdrDecodeDoubleVector();
						log.finest("entranceName = " + entranceName + ", array lenght = " + doubleA.length);
						listener.sendDouble(entranceName, doubleA);
						log.finest("data sent");
						break;
					}
					case 5:
					{
						log.finest("receiveDouble() request.");
						String exitName = xdrIn.xdrDecodeString();
						log.finest("exitName = " + exitName);
						double[] doubleA =  listener.receiveDouble(exitName);
						log.finest("exitName = " + exitName + ", array lenght = " + doubleA.length);
						xdrOut.xdrEncodeDoubleVector(doubleA);
						log.finest("data encoded");
						break;
					}
					case 6:
					{
						log.finest("getProperties() request.");
						xdrOut.xdrEncodeString(listener.getProperties());
						break;
					}
					case 7:
					{
						log.finest("getTmpPath() request.");
						xdrOut.xdrEncodeString(listener.getTmpPath());
						break;
					}
					default:
						throw new IOException("Unknown operation code " + operationCode);	
				}
				log.finest("flushing response");
				xdrOut.endEncoding();
				
				log.finest("operation decoded.");
				xdrIn.endDecoding();
				
			}
		} catch (IOException ex) {
			System.err.println("Communication error:");
			ex.printStackTrace();
		} catch (OncRpcException ex) {
			System.err.println("XDR Enc/Dec exception:");
			ex.printStackTrace();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		
	}

}
