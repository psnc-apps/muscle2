/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Fixes logger contention by adding a queue in the middle and a high-priority
 * thread that writes the output.
 * @author Joris Borgdorff
 */
public class ConcurrentFileHandler extends FileHandler implements LogWriter {
	private final LinkedBlockingQueue<String> records;
	private final FileLoggerThread loggerThread;
	private Formatter formatter;
	private Writer writer;
	private OutputStream output;
	private int bufferWritten;
	private final static int MAX_UNWRITTEN = 32;
	
	public ConcurrentFileHandler() throws IOException {
		super();
		this.records = new LinkedBlockingQueue<String>(); 
		this.loggerThread = new FileLoggerThread(this, records);
		this.loggerThread.start();
		this.bufferWritten = 1;
	}
	
	@Override
	public void setFormatter(Formatter format) {
		this.formatter = format;
		super.setFormatter(new TrivialFormatter());
	}
	
	@Override
	public void setOutputStream(OutputStream output) throws SecurityException {
		super.setOutputStream(output);
		this.output = output;
		try {
			this.setEncoding(this.getEncoding());
		} catch (UnsupportedEncodingException ex) {
			// This shouldn't happen.  The setEncoding method
			// should have validated that the encoding is OK.
			throw new Error("Unexpected exception " + ex);
		}
	}
	
	@Override
	public void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
		super.setEncoding(encoding);
		if (this.output != null) {
			if (encoding == null) {
				this.writer = new BufferedWriter(new OutputStreamWriter(output));
			} else {
				this.writer = new BufferedWriter(new OutputStreamWriter(output, encoding));
			}
        }
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
			if (this.writer != null) {
				try {
					this.writer.close();
				} catch (IOException ex) {
					Logger.getLogger(ConcurrentFileHandler.class.getName()).log(Level.SEVERE, "Could not close log writer", ex);
				}
			}
		}
	}
	
	public void write(String msg, int next) {
		try {
			writer.write(msg);
			if (next == 0 || bufferWritten >= MAX_UNWRITTEN) {
				writer.flush();
				bufferWritten = 1;
			} else {
				bufferWritten++;
			}
		} catch (IOException ex) {
			Logger.getLogger(ConcurrentFileHandler.class.getName()).log(Level.SEVERE, "Could not write log message", ex);
		}
	}
}
