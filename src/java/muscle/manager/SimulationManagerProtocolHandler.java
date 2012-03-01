/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.manager;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.ident.InstanceID;
import muscle.core.ident.Location;
import muscle.net.XdrProtocolHandler;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.XdrTcpDecodingStream;
import org.acplt.oncrpc.XdrTcpEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class SimulationManagerProtocolHandler extends XdrProtocolHandler<SimulationManager> {
	private final static Logger logger = Logger.getLogger(SimulationManagerProtocolHandler.class.getName());

	public SimulationManagerProtocolHandler(Socket s, SimulationManager listener) {
		super(s, listener);
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
	
}
