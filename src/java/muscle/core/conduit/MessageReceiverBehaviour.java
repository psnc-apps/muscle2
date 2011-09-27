/*
 * 
 */
package muscle.core.conduit;

import jade.core.behaviours.CyclicBehaviour;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.filter.FilterHead;
import muscle.core.conduit.filter.QueueConsumer;
import muscle.core.messaging.BufferingRemoteDataSinkTail;
import muscle.core.messaging.RemoteDataSinkTail;
import muscle.core.messaging.SinkObserver;
import muscle.core.messaging.jade.DataMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class MessageReceiverBehaviour extends CyclicBehaviour implements RemoteDataSinkTail<DataMessage<?>> {
	private FilterHead<DataMessage> headFilter;
	private RemoteDataSinkTail<DataMessage<?>> receiver;
	BasicConduit outer;

	public MessageReceiverBehaviour(QueueConsumer<DataMessage> newFilter, BasicConduit outer) {
		this(new FilterHead<DataMessage>(newFilter), outer);
		this.outer = outer;
	}

	public MessageReceiverBehaviour(FilterHead<DataMessage> newFilterHead, BasicConduit outer) {
		this.outer = outer;
		headFilter = newFilterHead;
		receiver = new BufferingRemoteDataSinkTail<DataMessage<?>>(outer.exitName);
		receiver.addObserver(outer);
		outer.addSource(receiver);
	}

	// receive from entrance
	@Override
	public void action() {
		DataMessage dmsg = null;
		try {
			dmsg = poll(60, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Logger.getLogger(MessageReceiverBehaviour.class.getName()).log(Level.SEVERE, "Message receiving interrupted", ex);
		}
		if (dmsg != null) {
			// feed the first headFilter
			headFilter.put(dmsg);
		}
	}

	@Override
	public void put(DataMessage<?> d) {
		receiver.put(d);
	}

	@Override
	public DataMessage<?> take() {
		// we use a custom poll instead of take.
		// super.take would call notifySinkWillYield AND also poll,
		// leading in notifySinkWillYield being called twice
		throw new java.lang.UnsupportedOperationException("can not take from " + getClass());
	}

	@Override
	public DataMessage<?> poll() {
		DataMessage<?> val = receiver.poll();
		if (val != null) {
			outer.notifySinkWillYield(val);
		}
		return val;
	}

	@Override
	public String id() {
		return receiver.id();
	}

	@Override
	public void addObserver(SinkObserver<DataMessage<?>> o) {
		receiver.addObserver(o);
	}

	@Override
	public DataMessage<?> poll(long time, TimeUnit unit) throws InterruptedException {
		DataMessage<?> val = receiver.poll(time, unit);
		if (val != null) {
			outer.notifySinkWillYield(val);
		}
		return val;
	}
}
