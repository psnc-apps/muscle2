/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.communication.message;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.model.Observation;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeObservationMessage<E extends Serializable> extends JadeMessage<Observation<E>> implements Message<E> {
	private final static Logger logger = Logger.getLogger(JadeObservationMessage.class.getName());
	public final static String OBSERVATION_KEY = JadeObservationMessage.class.getName() + "#id";
	
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
