/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import muscle.util.concurrency.SafeThread;

/**
 *
 * @author Joris Borgdorff
 */
public class FileLoggerThread extends SafeThread {
	private final LinkedBlockingQueue<String> records;
	private final LogWriter logger;
	private final static String EMPTY = "EMPTY";

	FileLoggerThread(LogWriter logger, LinkedBlockingQueue<String> records) {
		super("FileLoggerThread");
		this.setDaemon(true);
		this.setPriority(MAX_PRIORITY);
		this.records = records;
		this.logger = logger;
	}

	@Override
	protected void execute() throws Exception {
		// Repeats until dispose is called
		// wait for new messages with the blocking queue
		String msg = records.take();
		// Do not log poison packet
		if (msg != EMPTY) {
			logger.write(msg, records.size());
		}
	}

	@Override
	protected void handleInterruption(InterruptedException ex) {
		// Do nothing
	}

	@Override
	protected void handleException(Throwable ex) {
		Logger.getLogger(ConcurrentFileHandler.class.getName()).severe("Logging error occurred. No further file logging.");
	}

	public void dispose() {
		synchronized (this) {
			if (this.isDisposed()) {
				return;
			}
			super.dispose();
		}

		// Empty the queue
		String msg;
		while ((msg = records.poll()) != null) {
			logger.write(msg, records.size());
		}
		records.offer(EMPTY);
	}
}
