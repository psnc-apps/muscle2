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
import org.acplt.oncrpc.XdrDecodingStream;
import org.acplt.oncrpc.XdrEncodingStream;

/**
 *
 * @author Joris Borgdorff
 */
public class SimulationManagerProtocolHandler extends XdrProtocolHandler<Boolean,SimulationManager> {
	private final static Logger logger = Logger.getLogger(SimulationManagerProtocolHandler.class.getName());

	public SimulationManagerProtocolHandler(Socket s, SimulationManager listener) {
		super(s, listener);
	}

	@Override
	protected Boolean executeProtocol(XdrDecodingStream xdrIn, XdrEncodingStream xdrOut) throws OncRpcException, IOException {
		boolean success = false;
		xdrIn.beginDecoding();
		int opnum = xdrIn.xdrDecodeInt();
		SimulationManagerProtocol[] protoArr = SimulationManagerProtocol.values();
		if (opnum >= protoArr.length || opnum < 0) {
			xdrOut.xdrEncodeInt(SimulationManagerProtocol.UNSUPPORTED.ordinal());
			logger.log(Level.WARNING, "Unsupported operation number {0} requested", opnum);
			return Boolean.FALSE;
		}
		SimulationManagerProtocol proto = protoArr[opnum];
		String name = xdrIn.xdrDecodeString();
		InstanceID id = new InstanceID(name);
		switch (proto) {
			case LOCATE:
				xdrOut.xdrEncodeInt(opnum);
				// Flush, to indicate that we are waiting to resolve the location
				xdrOut.endEncoding();
				try {
					listener.resolve(id);
					success = true;
					xdrOut.xdrEncodeBoolean(true);
					encodeLocation(xdrOut, id.getLocation());
				} catch (InterruptedException ex) {
					xdrOut.xdrEncodeBoolean(false);
					logger.log(Level.SEVERE, "Could not resolve identifier", ex);
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
		xdrOut.endEncoding();
		return success;
	}
	
}
