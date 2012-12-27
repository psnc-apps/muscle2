/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Fixes logger contention by adding a queue in the middle and a high-priority
 * thread that writes the output.
 * @author Joris Borgdorff
 */
public class ConcurrentFileHandler extends FileHandler implements LogWriter {
	private final LinkedBlockingQueue<String> records;
	private final FileLoggerThread loggerThread;
	private Formatter formatter;
	
	public ConcurrentFileHandler() throws IOException {
		super();
		this.records = new LinkedBlockingQueue<String>(); 
		this.loggerThread = new FileLoggerThread(this, records);
		this.loggerThread.start();
	}
	
	@Override
	public void setFormatter(Formatter format) {
		this.formatter = format;
		super.setFormatter(new TrivialFormatter());
	}
	
	@Override
	public void publish(LogRecord record) {
		if (this.isLoggable(record)) {
			// Already preprocess the message
			this.records.offer(this.formatter.format(record));
		}
	}
	
	@Override
	public void close() {
		try {
			this.loggerThread.dispose();
		} finally {
			super.close();
		}
	}
	
	public void write(LogRecord record) {
		super.publish(record);
	}
}
