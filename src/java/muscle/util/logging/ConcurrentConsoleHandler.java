/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Fixes logger contention by adding a queue in the middle and a high-priority
 * thread that writes the output.
 * @author Joris Borgdorff
 */
public class ConcurrentConsoleHandler extends ConsoleHandler implements LogWriter {
	private final LinkedBlockingQueue<String> records;
	private final FileLoggerThread loggerThread;
	private Formatter formatter;
	private Formatter trivialFormatter;
	
	public ConcurrentConsoleHandler() throws IOException {
		super();
		this.records = new LinkedBlockingQueue<String>(); 
		this.loggerThread = new FileLoggerThread(this, records);
		this.loggerThread.start();
	}
	
	@Override
	public void setFormatter(Formatter format) {
		this.formatter = format;
	}
	
	public Formatter getFormatter() {
		return this.formatter;
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
	
	public void write(String msg, int next) {
		System.err.print(msg);
		System.err.flush();
	}
}
