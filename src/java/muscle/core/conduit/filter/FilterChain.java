/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.core.conduit.filter;

import java.lang.reflect.Constructor;
import java.util.List;
import muscle.core.wrapper.Observation;
import muscle.exception.MUSCLERuntimeException;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class FilterChain extends AbstractFilter<Observation,Observation> {
	public void init(List<String> args) {
		System.out.println("Filterchain args: " + args);
		if (!args.isEmpty()) {
			QueueConsumer<Observation> qc = automaticPipeline(args, this);
			this.setQueueConsumer(qc);
		}
	}
	
	public boolean isActive() {
		return this.consumer != null;
	}

	public void process(Observation obs) {
		if (this.isActive()) {
			put(obs);
			this.consumer.apply();
		} else {
			this.apply(obs);
		}
	}
	
	public void apply() {
		while (!incomingQueue.isEmpty()) {
			Observation message = incomingQueue.remove();
			this.apply(message);
		}
	}
	
	private QueueConsumer<Observation> automaticPipeline(List<String> filterNames, QueueConsumer<Observation> tailFilter) {
		QueueConsumer<Observation> filter = tailFilter;
		for (int i = filterNames.size() - 1; i >= 0; i--) {
			filter = filterForName(filterNames.get(i), filter);
		}

		return filter;
	}

	private QueueConsumer<Observation> filterForName(String fullName, QueueConsumer<Observation> tailFilter) {
		// split any args from the preceding filter name
		String[] tmp = fullName.split("_", 2); // 2 means only split once
		String name = tmp[0];
		String remainder = null;
		if (tmp.length > 1) {
			remainder = tmp[1];
		}

		ObservationFilter filter = null;

		// filters without args
		if (name.equals("null")) {
			filter = new NullFilter();
		} else if (name.equals("pipe")) {
			filter = new PipeFilter();
		} else if (name.equals("console")) {
			filter = new ConsoleWriterFilter();
		} else if (name.equals("linearinterpolation")) {
			filter = new LinearInterpolationFilterDouble();
		} // filters with mandatory args
		else if (name.equals("multiply")) {
			filter = new MultiplyFilterDouble(Double.valueOf(remainder));
		} else if (name.equals("drop")) {
			filter = new DropFilter(Integer.valueOf(remainder));
		} else if (name.equals("timeoffset")) {
			filter = new TimeOffsetFilter(Integer.valueOf(remainder));
		} else if (name.equals("timefactor")) {
			filter = new TimeFactorFilter(Integer.valueOf(remainder));
		} else if (name.equals("blockafter")) {
			filter = new BlockAfterTimeFilter(Integer.valueOf(remainder));
		} else if (name.equals("lineartimeinterpolation")) {
			filter = new LinearTimeInterpolationFilterDouble(Integer.valueOf(remainder));
		} // assume name refers to a class name
		else {
			Class<? extends ObservationFilter> filterClass = null;
			double rem = 0d;
			if (remainder != null) {
				rem = Double.valueOf(remainder);
			}

			try {
				filterClass = (Class<? extends ObservationFilter>) Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new MUSCLERuntimeException(e);
			}


			// try to find constructor with tailFilter
			Constructor<? extends ObservationFilter> filterConstructor = null;
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
			} catch (java.lang.InstantiationException e) {
				throw new MUSCLERuntimeException(e);
			} catch (java.lang.IllegalAccessException e) {
				throw new MUSCLERuntimeException(e);
			} catch (java.lang.reflect.InvocationTargetException e) {
				throw new MUSCLERuntimeException(e);
			}

		}

		if (filter != null) {
			filter.setQueueConsumer(tailFilter);
			return filter;
		} else {
			return tailFilter;
		}
	}
}