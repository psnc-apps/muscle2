/*
 * 
 */

package muscle.manager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.InstanceID;
import muscle.core.ident.Location;
import muscle.core.ident.TcpLocation;
import muscle.net.LocalSocketFactory;
import muscle.net.XdrConnectionHandler;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;

/**
 * Executes the server side of the SimulationManager protocol.
 * 
 * The protocol is executed as follows
 * @author Joris Borgdorff
 */
public class ManagerConnectionHandler extends XdrConnectionHandler<SimulationManager> {
	public ManagerConnectionHandler(SimulationManager listener) throws UnknownHostException, IOException {
		super(new LocalSocketFactory().createServerSocket(0, 10, InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1})), listener);
	} 

	@Override
	protected void executeProtocol(XdrTcpDecodingStream xdrIn, XdrTcpEncodingStream xdrOut) throws OncRpcException, IOException {
		boolean success;
		
		int opnum = xdrIn.xdrDecodeInt();
		SimulationManagerProtocol[] protoArr = SimulationManagerProtocol.values();
		if (opnum >= protoArr.length || opnum < 0) {
			xdrOut.xdrEncodeInt(SimulationManagerProtocol.UNSUPPORTED.ordinal());
			logger.log(Level.WARNING, "Unsupported operation number {0} requested", opnum);
			return;
		}
		SimulationManagerProtocol proto = protoArr[opnum];
		
		String name = xdrIn.xdrDecodeString();
		InstanceID id = new InstanceID(name);
		
		switch (proto) {			
			case LOCATE:
				xdrOut.xdrEncodeInt(opnum);
				// Flush, to indicate that we are waiting to resolve the location
				xdrOut.endEncoding(true);
				try {
					listener.resolve(id);
					xdrOut.xdrEncodeBoolean(true);
					encodeLocation(xdrOut, id.getLocation());
				} catch (InterruptedException ex) {
					xdrOut.xdrEncodeBoolean(false);
					Logger.getLogger(ManagerConnectionHandler.class.getName()).log(Level.SEVERE, "Could not resolve", ex);
				}
				break;
			case REGISTER:
				xdrOut.xdrEncodeInt(opnum);
				Location loc = decodeLocation(xdrIn);
				id.resolve(loc);
				success = listener.register(id);
				xdrOut.xdrEncodeBoolean(success);
				break;
			case DEREGISTER:
				xdrOut.xdrEncodeInt(opnum);
				success = listener.deregister(id);
				xdrOut.xdrEncodeBoolean(success);
				break;
			default:
				logger.log(Level.WARNING, "Unsupported operation {0} requested", proto);
				xdrOut.xdrEncodeInt(SimulationManagerProtocol.UNSUPPORTED.ordinal());
				break;
		}
	}
		
	private void encodeLocation(XdrTcpEncodingStream xdrOut, Location loc) throws OncRpcException, IOException {
		if (!(loc instanceof TcpLocation)) {
			throw new IllegalArgumentException("Location belonging to identity is not a TcpLocation; can only encode TcpLocation");
		}
		TcpLocation tcpLoc = (TcpLocation)loc;
		
		byte[] addr = tcpLoc.getAddress().getAddress();		
		xdrOut.xdrEncodeByteVector(addr);
		xdrOut.xdrEncodeInt(tcpLoc.getPort());
	}
	
	private Location decodeLocation(XdrTcpDecodingStream xdrIn) throws OncRpcException, IOException {
		byte[] addr = xdrIn.xdrDecodeByteVector();
		InetAddress inetAddr = InetAddress.getByAddress(addr);
		int port = xdrIn.xdrDecodeInt();
		return new TcpLocation(inetAddr, port);
	}
}
