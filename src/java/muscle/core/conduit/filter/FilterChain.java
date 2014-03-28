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
package muscle.core.conduit.filter;

import eu.mapperproject.jmml.util.FastArrayList;
import java.lang.reflect.Constructor;
import java.util.List;
import muscle.core.model.Observation;
import muscle.exception.MUSCLERuntimeException;

/**
 * Initialize a chain of filters with init(), and override the queue(Observation) to use the outgoing messages of the filter
 * @author Joris Borgdorff
 */
public abstract class FilterChain implements Filter {
	private final List<ThreadedFilter> threadedFilters;
	private Filter nextFilter;
	private boolean isDone;
	
	public FilterChain() {
		this.threadedFilters = new FastArrayList<ThreadedFilter>();
		this.nextFilter = null;
	}
	
	public boolean isBusy() {
		synchronized (this) {
			if (isDone) {
				return false;
			}
		}
		return this.nextFilter.isProcessing();
	}
	
	public boolean waitForFilters() throws InterruptedException {
		return nextFilter.waitUntilEmpty();
	}
	
	@Override
	public synchronized boolean waitUntilEmpty() throws InterruptedException {
		return !isDone;
	}
	
	@Override
	public synchronized boolean isProcessing() {
		return false;
	}
	
	public void init(List<String> args) {
		if (args.isEmpty()) {
			throw new IllegalArgumentException("Can not create empty FilterChain");
		}
		Filter qc = automaticPipeline(args, this);
		this.setNextFilter(qc);
	}
	
	@SuppressWarnings("unchecked")
	public void process(Observation obs) {
		this.nextFilter.queue(obs);
		this.nextFilter.apply();
	}
	
	@Override
	public void apply() {}
	
	private Filter automaticPipeline(List<String> filterNames, Filter tailFilter) {
		Filter filter = tailFilter;
		for (int i = filterNames.size() - 1; i >= 0; i--) {
			filter = filterForName(filterNames.get(i), filter);
		}

		return filter;
	}

	public void dispose() {
		synchronized (this) {
			if (this.isDone) {
				return;
			} else {
				this.isDone = true;
			}
		}
		for (ThreadedFilter f : this.threadedFilters) {
			f.dispose();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Filter filterForName(String fullName, Filter tailFilter) {
		// split any args from the preceding filter name
		String name;
		String remainder;
		int underscoreIndex = fullName.lastIndexOf('_');
		if (underscoreIndex == -1) {
			name = fullName;
			remainder = null;
		} else {
			name = fullName.substring(0, underscoreIndex);
			remainder = fullName.substring(underscoreIndex + 1);
		}

		Filter filter = null;

		// filters without args
		if (name.equals("null")) {
			filter = new NullFilter();
		} else if (name.equals("pipe")) {
			filter = new PipeFilter();
		} else if (name.equals("console")) {
			filter = new ConsoleWriterFilter();
		} else if (name.equals("linearinterpolation")) {
			filter = new LinearInterpolationFilterDouble();
		} else if (name.equals("serialize")) {
			filter = new SerializeFilter();
		} else if (name.equals("deserialize")) {
			filter = new DeserializeFilter();
		} else if (name.equals("compress")) {
			filter = new CompressFilter();
		} else if (name.equals("decompress")) {
			filter = new DecompressFilter();
		} else if (name.equals("thread")) {
			ThreadedFilter tfilter = new ThreadedFilter();
			tfilter.start();
			this.threadedFilters.add(tfilter);
			filter = tfilter;
		}// filters with mandatory args
		else if (name.equals("multiply")) {
			filter = new MultiplyFilterDouble(Double.valueOf(remainder));
		} else if (name.equals("drop")) {
			filter = new DropFilter(Integer.valueOf(remainder));
		} else if (name.equals("timeoffset")) {
			filter = new TimeOffsetFilter(Double.valueOf(remainder));
		} else if (name.equals("timefactor")) {
			filter = new TimeFactorFilter(Double.valueOf(remainder));
		} else if (name.equals("blockafter")) {
			filter = new BlockAfterTimeFilter(Double.valueOf(remainder));
		} else if (name.equals("lineartimeinterpolation")) {
			filter = new LinearTimeInterpolationFilterDouble(Integer.valueOf(remainder));
		} else if (name.equals("chunk")) {
			filter = new ChunkFilter(Integer.valueOf(remainder));
		} else if (name.equals("dechunk")) {
			filter = new DechunkFilter(Integer.valueOf(remainder));
		} // assume name refers to a class name
		else {
			Class<? extends Filter> filterClass = null;
			double rem = 0d;
			if (remainder != null) {
				rem = Double.valueOf(remainder);
			}

			try {
				filterClass = (Class<? extends Filter>) Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new MUSCLERuntimeException(e);
			}


			// try to find constructor with tailFilter
			Constructor<? extends Filter> filterConstructor = null;
			try {
				if (remainder == null) {
					filterConstructor = filterClass.getDeclaredConstructor((Class[]) null);
				} else {
					filterConstructor = filterClass.getDeclaredConstructor(new Class[]{Double.TYPE});
				}
			} catch (java.lang.NoSuchMethodException e) {
				throw new MUSCLERuntimeException(e);
			}

			try {
				if (remainder == null) {
					filter = filterConstructor.newInstance();
				} else {
					filter = filterConstructor.newInstance(rem);
				}
				if (filter instanceof ThreadedFilter) {
					ThreadedFilter tfilter = (ThreadedFilter)filter;
					tfilter.start();
					threadedFilters.add(tfilter);
				}
			} catch (java.lang.InstantiationException e) {
				throw new MUSCLERuntimeException(e);
			} catch (java.lang.IllegalAccessException e) {
				throw new MUSCLERuntimeException(e);
			} catch (java.lang.reflect.InvocationTargetException e) {
				throw new MUSCLERuntimeException(e);
			}

		}

		if (filter != null) {
			filter.setNextFilter(tailFilter);
			return filter;
		} else {
			return tailFilter;
		}
	}
	
	@Override
	public void setNextFilter(Filter filter) {
		this.nextFilter = filter;
	}
}