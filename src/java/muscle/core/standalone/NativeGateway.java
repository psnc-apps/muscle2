package muscle.core.standalone;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;


public class NativeGateway  extends Thread {
	protected ServerSocket ss;
	protected CallListener listener;
	
	public NativeGateway(CallListener listener) throws UnknownHostException, IOException {
		ss = new ServerSocket(0, 1, InetAddress.getLocalHost());
		
		this.listener = listener;
		
		setDaemon(true);
	}
	
	
	public interface CallListener {
		/* OPCODE = 0 */
		public int addEntrance(String name, int rate, int type);
		/* OPCODE = 1 */
		public int addExit(String name, int rate, int type);
		/* OPCODE = 2 */
		public void send(int entrancelId, double data[]);
		/* OPCODE = 3 */
		public double[] receive(int exitId);
		/* OPCODE = 4 */
		public String getKernelName();
		/* OPCODE = 5 */
		public String getProperty(String name);
	}
	
	public String getContactInformation() {
		return "tcp://" + ss.getInetAddress() + ":" + ss.getLocalPort();
	}
	
	@Override
	public void run() {
		try {
			Socket s = ss.accept();
			
			XdrTcpDecodingStream xdrIn =  new XdrTcpDecodingStream(s, 64 * 1024);
			XdrTcpEncodingStream xdrOut = new XdrTcpEncodingStream(s, 64 * 1024); 
			
			while (true) {			
				int operationCode = xdrIn.xdrDecodeInt();
				
				switch (operationCode) {
					case 0:
						int entranceID = listener.addEntrance(xdrIn.xdrDecodeString(), xdrIn.xdrDecodeInt(), xdrIn.xdrDecodeInt());
						xdrOut.xdrEncodeInt(entranceID);
						break;
					case 1:
						int exitID = listener.addExit(xdrIn.xdrDecodeString(), xdrIn.xdrDecodeInt(), xdrIn.xdrDecodeInt());
						xdrOut.xdrEncodeInt(exitID);
						break;
						
					case 2:
						listener.send(xdrIn.xdrDecodeInt(), xdrIn.xdrDecodeDoubleVector());
						break;
					case 3:
						double array[] =  listener.receive(xdrIn.xdrDecodeInt());
						xdrOut.xdrEncodeDoubleVector(array);
						break;
					case 4:
						xdrOut.xdrEncodeString(listener.getKernelName());
						break;
					case 5:
						String value = listener.getProperty(xdrIn.xdrDecodeString());
						xdrOut.xdrEncodeString(value);
						break;
					default:
						throw new IOException("Unknown operation code " + operationCode);	
				}
				
			}
		} catch (IOException ex) {
			System.err.println("Communication error:");
			ex.printStackTrace();
		} catch (OncRpcException ex) {
			System.err.println("XDR Enc/Dec exception:");
			ex.printStackTrace();
		}
		
	}

}
