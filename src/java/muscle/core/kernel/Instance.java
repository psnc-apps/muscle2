/*
 * 
 */

package muscle.core.kernel;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.*;

/**
 * A generic MML instance.
 * @author Joris Borgdorff
 */
public abstract class Instance extends RawInstance {
	private final static Logger logger = Logger.getLogger(Instance.class.getName());
	protected final static int NONE = 0;
	protected final static int SEND = 1;
	protected final static int RECV = 2;
	/**
	 * Determines which operations (SEND or RECV) may take place at a given time.
	 * This is enforced in the out() and in() functions.
	 */
	protected int operationsAllowed = SEND & RECV;
	
	/**
	 * Initialized all ports for you.
	 * If overridden, all ConduitCntrances and ConduitExits have to be added in this function.
	 */
	@Override
	protected void addPortals() {
		Map<String,ConduitDescription> ports = this.controller.getEntranceDescriptions();
		if (ports != null) {
			for (ConduitDescription entrance : ports.values()) {
				this.addEntrance(entrance.getEntrance().getPortName(), Serializable.class);
			}
		}
		logger.log(Level.FINE, "{0}: added all conduit entrances", getLocalName());
		ports = this.controller.getExitDescriptions();
		if (ports != null) {
			for (ConduitDescription exit : ports.values()) {
				this.addExit(exit.getExit().getPortName(), Serializable.class);
			}
		}
		logger.log(Level.FINE, "{0}: added all conduit exits", getLocalName());
	}

	/**
	 * Get the ConduitEntrance (or out-port) with a certain name.
	 * @param outName name of the port or conduit entrance
	 * @throws IllegalArgumentException if the port is not defined in the MUSCLE CxA file.
	 * @throws IllegalStateException if called while not in a sending operator
	 * @return a ConduitEntrance with the given name
	 */
	protected ConduitEntrance out(String outName) {
		if ((operationsAllowed & SEND) != SEND) {
			throw new IllegalStateException("May only send information while in a sending operator");
		}
		ConduitEntranceController ec = this.entrances.get(outName);
		if (ec == null) {
			throw new IllegalArgumentException("ConduitEntrance " + outName + " in Instance " + getLocalName() + " is called but is not defined in the MUSCLE CxA file.");
		} else {
			return ec.getEntrance();
		}
	}
	
	/**
	 * Get the ConduitExit (or in-port) with a certain name.
	 * @param inName name of the port or conduit exit
	 * @throws IllegalArgumentException if the port is not defined in the MUSCLE CxA file.
	 * @throws IllegalStateException if called while not in a receiving operator
	 * @return a ConduitExit with the given name
	 */
	protected ConduitExit in(String inName) {
		if ((operationsAllowed & RECV) != RECV) {
			throw new IllegalStateException("May not receive information while in a sending operator");
		}

		ConduitExitController ec = this.exits.get(inName);
		if (ec == null) {
			throw new IllegalArgumentException("ConduitExit " + inName + " in Instance " + getLocalName() + " is called but it is not defined in the MUSCLE CxA file.");
		} else {
			return ec.getExit();
		}
	}
}
