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
import muscle.core.messaging.serialization.DeserializerWrapper;
import muscle.core.messaging.serialization.SerializerWrapper;
import muscle.net.ProtocolHandler;

/**
 *
 * @author Joris Borgdorff
 */
public class SimulationManagerProtocolHandler extends ProtocolHandler<Boolean,SimulationManager> {
	private final static Logger logger = Logger.getLogger(SimulationManagerProtocolHandler.class.getName());

	public SimulationManagerProtocolHandler(Socket s, SimulationManager listener) {
		super(s, listener);
	}

	@Override
	protected Boolean executeProtocol(DeserializerWrapper in, SerializerWrapper out) throws IOException {
		boolean success = false;
		in.refresh();
		int opnum = in.readInt();
		SimulationManagerProtocol[] protoArr = SimulationManagerProtocol.values();
		if (opnum >= protoArr.length || opnum < 0) {
			out.writeInt(SimulationManagerProtocol.UNSUPPORTED.ordinal());
			logger.log(Level.WARNING, "Unsupported operation number {0} requested", opnum);
			return Boolean.FALSE;
		}
		SimulationManagerProtocol proto = protoArr[opnum];
		String name = in.readString();
		InstanceID id = new InstanceID(name);
		switch (proto) {
			case LOCATE:
				out.writeInt(opnum);
				// Flush, to indicate that we are waiting to resolve the location
				out.flush();
				try {
					listener.resolve(id);
					success = true;
					out.writeBoolean(true);
					encodeLocation(out, id.getLocation());
				} catch (InterruptedException ex) {
					out.writeBoolean(false);
					logger.log(Level.SEVERE, "Could not resolve identifier", ex);
				}
				break;
			case REGISTER:
				out.writeInt(opnum);
				Location loc = decodeLocation(in);
				id.resolve(loc);
				success = listener.register(id);
				out.writeBoolean(success);
				break;
			case DEREGISTER:
				out.writeInt(opnum);
				success = listener.deregister(id);
				out.writeBoolean(success);
				break;
			default:
				logger.log(Level.WARNING, "Unsupported operation {0} requested", proto);
				out.writeInt(SimulationManagerProtocol.UNSUPPORTED.ordinal());
				break;
		}
		out.flush();
		return success;
	}
	
}
