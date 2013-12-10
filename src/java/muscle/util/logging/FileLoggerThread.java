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
	@SuppressWarnings("StringEquality")
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

	@Override
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
