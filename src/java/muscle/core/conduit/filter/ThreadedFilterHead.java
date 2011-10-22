/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.filter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.SafeTriggeredThread;

/**
 *
 * @author Joris Borgdorff
 */
public class ThreadedFilterHead<F> extends SafeTriggeredThread implements QueueProducer<F> {
	private QueueConsumer<F> consumer;
	private final Queue<F> outgoingQueue;
	
	public ThreadedFilterHead(QueueConsumer<F> consumer) {
		this.outgoingQueue = new LinkedBlockingQueue<F>();
		this.setQueueConsumer(consumer);
	}
	
	public void put(F data) {
		outgoingQueue.add(data);
		apply();
	}
	
	private void apply() {
		this.trigger();
	}
	
	protected void execute() {
		this.consumer.apply();
	}

	public final void setQueueConsumer(QueueConsumer<F> qc) {
		if (this.consumer != null) {
			this.consumer.setIncomingQueue(null);
		}
		this.consumer = qc;
		qc.setIncomingQueue(outgoingQueue);
		qc.apply();
	}

	protected void handleInterruption(InterruptedException ex) {
		Logger.getLogger(ThreadedFilterHead.class.getName()).log(Level.SEVERE, "Could not apply filter", ex);
	}
}
