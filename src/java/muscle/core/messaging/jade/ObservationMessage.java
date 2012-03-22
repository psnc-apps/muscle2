/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.messaging.jade;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.messaging.Message;
import muscle.core.messaging.Observation;
import muscle.core.messaging.signal.Signal;

/**
 *
 * @author Joris Borgdorff
 */
public class ObservationMessage<E extends Serializable> extends DataMessage<Observation<E>> implements Message<E> {
	private final static Logger logger = Logger.getLogger(ObservationMessage.class.getName());
	public final static String OBSERVATION_KEY = ObservationMessage.class.getName() + "#id";
	
	@Override
	protected void setIdentifierString(String name, String type, String portname) {
		this.setIdentifierString(OBSERVATION_KEY, name, type, portname);
	}
	
	public E getRawData() {
		return getData().getData();
	}

	public Observation<E> getObservation() {
		return getData();
	}
	
	public void setSignal(Signal signal) {
		addUserDefinedParameter("signal", signal.getClass().getName());
	}
	
	public void setSignal(String s) {
		if (s != null) {
			addUserDefinedParameter("signal", s);
		}
	}
	
	public boolean isSignal() {
		return getUserDefinedParameter("signal") != null;
	}
	
	public Signal getSignal() {
		String sig = getUserDefinedParameter("signal");
		try {
			return (Signal)Class.forName(sig).newInstance();
		} catch (ClassNotFoundException ex) {
			logger.log(Level.SEVERE, "Can not reconstruct signal " + sig + "; not in classpath.", ex);
		} catch (InstantiationException ex) {
			logger.log(Level.SEVERE, "Can not reconstruct signal " + sig + "; can not instantiate.", ex);
		} catch (IllegalAccessException ex) {
			logger.log(Level.SEVERE, "Can not reconstruct signal " + sig + "; not allowed to instantiate.", ex);
		}
		return null;
	}
}
