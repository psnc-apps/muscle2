/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
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
	private final LogRecord dummyRecord;

	FileLoggerThread(LogWriter logger, LinkedBlockingQueue<String> records) {
		super("FileLoggerThread");
		this.setDaemon(true);
		this.setPriority(MAX_PRIORITY);
		this.records = records;
		this.logger = logger;
		this.dummyRecord = new LogRecord(Level.OFF,"");
	}

	@Override
	protected void execute() throws Exception {
		// Repeats until dispose is called
		// wait for new messages with the blocking queue
		String msg = records.take();
		// Do not log poison packet
		if (msg != EMPTY) {
			dummyRecord.setMessage(msg);
			logger.write(dummyRecord);
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
		LogRecord extraDummy = new LogRecord(Level.OFF, "");
		while ((msg = records.poll()) != null) {
			extraDummy.setMessage(msg);
			logger.write(extraDummy);
		}
		records.offer(EMPTY);
	}
}
