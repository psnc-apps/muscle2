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
import muscle.core.messaging.jade.ObservationMessage;

/**
 *
 * @author Joris Borgdorff
 */
public class MessageReceiverBehaviour extends CyclicBehaviour implements RemoteDataSinkTail<ObservationMessage<?>> {
	private FilterHead<ObservationMessage> headFilter;
	private RemoteDataSinkTail<ObservationMessage<?>> receiver;
	BasicConduit outer;

	public MessageReceiverBehaviour(QueueConsumer<ObservationMessage> newFilter, BasicConduit outer) {
		this(new FilterHead<ObservationMessage>(newFilter), outer);
		this.outer = outer;
	}

	public MessageReceiverBehaviour(FilterHead<ObservationMessage> newFilterHead, BasicConduit outer) {
		this.outer = outer;
		headFilter = newFilterHead;
		receiver = new BufferingRemoteDataSinkTail<ObservationMessage<?>>(outer.exitName);
		receiver.addObserver(outer);
		outer.addSource(receiver);
	}

	// receive from entrance
	@Override
	public void action() {
		ObservationMessage dmsg = null;
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
	public void put(ObservationMessage<?> d) {
		receiver.put(d);
	}

	@Override
	public ObservationMessage<?> take() {
		// we use a custom poll instead of take.
		// super.take would call notifySinkWillYield AND also poll,
		// leading in notifySinkWillYield being called twice
		throw new java.lang.UnsupportedOperationException("can not take from " + getClass());
	}

	@Override
	public ObservationMessage<?> poll() {
		ObservationMessage<?> val = receiver.poll();
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
	public void addObserver(SinkObserver<ObservationMessage<?>> o) {
		receiver.addObserver(o);
	}

	@Override
	public ObservationMessage<?> poll(long time, TimeUnit unit) throws InterruptedException {
		ObservationMessage<?> val = receiver.poll(time, unit);
		if (val != null) {
			outer.notifySinkWillYield(val);
		}
		return val;
	}
}
