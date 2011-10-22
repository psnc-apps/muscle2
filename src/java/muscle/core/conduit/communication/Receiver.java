/*
 * 
 */
package muscle.core.conduit.communication;

/**
 *
 * @author jborgdo1
 */
public interface Receiver<E, F> extends CommunicatingPoint<E,F> {
	/** Receives a message. Will return null if no more messages can be received */
	public E receive();
}
