/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
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
	
	public ConcurrentConsoleHandler() throws IOException {
		super();
		this.records = new LinkedBlockingQueue<String>(); 
		this.formatter = new MuscleFormatter();
		this.loggerThread = new FileLoggerThread(this, records);
		this.loggerThread.start();
	}
	
	@Override
	public void setFormatter(Formatter format) {
		if (format != null) {
			this.formatter = format;
		}
	}
	
	@Override
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
