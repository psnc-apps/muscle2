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

package muscle.util.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Joris Borgdorff
 */
public class NamedExecutor {
	private final ExecutorService executor;
	
	public NamedExecutor() {
		executor = Executors.newCachedThreadPool();
	}
	
	public NamedExecutor(int threads) {
		this.executor = new ThreadPoolExecutor(1, threads, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	public <T> Future<T> submit(NamedCallable<T> job) {
		return executor.submit(new NamingCallable<T>(job));
	}
	
	private static class NamingCallable<T> implements Callable<T> {
		private final NamedCallable<T> job;
		NamingCallable(NamedCallable<T> job) {
			this.job = job;
		}
		@Override
		public T call() throws Exception {
			String newName = job.getName();
			
			if (newName == null) {
				return job.call();
			}
			
			Thread currentThread = Thread.currentThread();
			String oldName = currentThread.getName();
			currentThread.setName(newName);
			
			try {
				return job.call();
			} finally {
				currentThread.setName(oldName);
			}
		}
	}
	
	public void shutdown() {
		executor.shutdown();
	}
	
	public boolean isShutdown() {
		return executor.isShutdown();
	}
}
