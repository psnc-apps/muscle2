/*
 * 
 */
package muscle.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import muscle.core.conduit.communication.Receiver;
import muscle.core.ident.PortalID;
import muscle.core.kernel.InstanceController;
import muscle.core.messaging.Message;

/**
 *
 * @author Joris Borgdorff
 */
public class ConduitExitController<T> extends Portal<T> {
	private Receiver<T, ?> receiver;
	private ConduitExit<T> conduitExit;
	private final BlockingQueue<T> queue;

	public ConduitExitController(PortalID newPortalID, InstanceController newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);
		this.queue = new LinkedBlockingQueue<T>();
	}
	
	public void setReceiver(Receiver<T, ?> recv) {
		this.receiver = recv;
	}
	
	BlockingQueue<T> getQueue() {
		return this.queue;
	}

	public void setExit(ConduitExit<T> exit) {
		this.conduitExit = exit;
	}

	public ConduitExit<T> getExit() {
		return this.conduitExit;
	}

	@Override
	protected void execute() {
		Message<T> msg = this.receiver.receive();
		if (msg != null) {
			this.queue.add(msg.getData());
		}
	}
}
