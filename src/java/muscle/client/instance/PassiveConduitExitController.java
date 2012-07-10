/*
 * 
 */
package muscle.client.instance;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import muscle.client.communication.Receiver;
import muscle.client.communication.message.BasicMessage;
import muscle.client.communication.message.DetachConduitSignal;
import muscle.client.communication.message.Message;
import muscle.core.ConduitExit;
import muscle.core.DataTemplate;
import muscle.core.kernel.InstanceController;
import muscle.core.model.Observation;
import muscle.id.InstanceID;
import muscle.id.PortalID;
import muscle.util.data.SerializableData;
import muscle.util.data.SingleProducerConsumerBlockingQueue;
import muscle.util.serialization.DataConverter;

/**
 *
 * @author Joris Borgdorff
 */
public class PassiveConduitExitController<T extends Serializable> extends PassivePortal<T> implements ConduitExitControllerImpl<T>, Receiver<T,BasicMessage<SerializableData>,InstanceID,PortalID<InstanceID>> {
	private ConduitExit<T> conduitExit;
	private final BlockingQueue<Observation<T>> queue;
	private volatile boolean isDone;
	protected DataConverter<Message<T>,BasicMessage<SerializableData>> converter;

	public PassiveConduitExitController(PortalID newPortalID, InstanceController newOwnerAgent, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newDataTemplate);
		this.queue = new SingleProducerConsumerBlockingQueue<Observation<T>>();
		this.conduitExit = null;
		this.isDone = false;
	}
	
	public void setExit(ConduitExit<T> exit) {
		this.conduitExit = exit;
	}

	public ConduitExit<T> getExit() {
		return this.conduitExit;
	}
	
	@Override
	public void dispose() {
		this.isDone = true;
	}

	public String toString() {
		return "in-port:" + this.getIdentifier();
	}

	@Override
	public BlockingQueue<Observation<T>> getMessageQueue() {
		return this.queue;
	}

	@Override
	public void messageReceived(Observation<T> obs) {
		this.setNextTimestamp(obs.getNextTimestamp());
	}

	@Override
	public void put(BasicMessage<SerializableData> dmsg) {
		Message<T> msg = converter.deserialize(dmsg);
		if (dmsg != null) {
			if (msg.isSignal()) {
				if (msg.getSignal() instanceof DetachConduitSignal) {
					queue.add(null);
					this.dispose();
				}
			} else {
				this.queue.add(msg.getObservation());
				increment();
			}
		}
	}

	@Override
	public void setDataConverter(DataConverter<Message<T>, BasicMessage<SerializableData>> serializer) {
		this.converter = serializer;
	}

	@Override
	public boolean isDisposed() {
		return this.isDone;
	}
	
		@Override
	public void setComplementaryPort(PortalID<InstanceID> id) {
		// Do nothing.
	}
	
	@Override
	public Message<T> receive() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void start() {
		// Do nothing
	}
}
