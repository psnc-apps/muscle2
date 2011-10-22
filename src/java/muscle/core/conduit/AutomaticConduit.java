/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

MUSCLE is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MUSCLE is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
 */
package muscle.core.conduit;

import java.util.logging.Level;
import muscle.core.wrapper.Observation;
import muscle.exception.MUSCLERuntimeException;
import utilities.MiscTool;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;
import muscle.core.conduit.filter.*;
import muscle.core.ident.JadePortalID;
import muscle.core.messaging.jade.ObservationMessage;

/**
will automatically create a filter chain from a list of filter names<br>
e.g. in the connection scheme write<br>
exit@receiver muscle.core.conduit.AutomaticConduit#A(muscle.core.conduit.filter.MultiplyFilterDouble_42) entrance@sender<br>
to load the MultiplyFilterDouble_42 and pass a 42 to its constructor
@author Jan Hegewald
 */
public class AutomaticConduit extends BasicConduit {
	private final static transient Logger logger = Logger.getLogger(AutomaticConduit.class.getName());

	@Override
	protected void constructMessagePassingMechanism() {
		// init filter chain
		Filter<ObservationMessage,Observation>  filters = initFilterChain(new DataSenderFilterTail(this));

		MessageReceiverBehaviour receiver = new MessageReceiverBehaviour(filters, this);
		addBehaviour(receiver);
	}

	protected Filter<ObservationMessage,Observation> initFilterChain(final FilterTail<ObservationMessage> filterTail) {
		List<String> filterArgs = getOptionalArgs();
		logger.log(Level.FINE, "filter args: <{0}>", MiscTool.joinItems(filterArgs, ", "));

		// assume our optional args are filter names
		// cast our optional args to filter names

		final ObservationMessage dataMessage = new ObservationMessage();
		dataMessage.setRecipient(new JadePortalID(exitName, exitAgent));

		// At the end, convert wrappers back to messages
		final Filter<Observation, ObservationMessage> wrapper2dmsg = new AbstractFilter<Observation, ObservationMessage>(filterTail) {
			@Override
			protected void apply(Observation in) {
				dataMessage.store(in, null);
				put(dataMessage);
			}
		};

		QueueConsumer<Observation> filters = automaticPipeline(filterArgs, wrapper2dmsg);

		// At the beginning, convert messages to wrappers
		final Filter<ObservationMessage,Observation> dmsg2wrapper = new AbstractFilter<ObservationMessage,Observation>(filters) {
			@Override
			public void apply(ObservationMessage in) {
				Observation wrapper = (Observation)in.getData();
				put(wrapper);
			}
		};

		return dmsg2wrapper;
	}

	private QueueConsumer<Observation> automaticPipeline(List<String> filterNames,  QueueConsumer<Observation> tailFilter) {
		QueueConsumer<Observation> filter = tailFilter;
		for (int i = filterNames.size() - 1; i > -1; i--) {
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
		} 
		
		// filters with mandatory args
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
		}
		
		// assume name refers to a class name
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
					filterConstructor = filterClass.getDeclaredConstructor((Class[])null);
				}
				else {
					filterConstructor = filterClass.getDeclaredConstructor(new Class[]{Double.TYPE});
				}
			} catch (java.lang.NoSuchMethodException e) {
				throw new MUSCLERuntimeException(e);
			}

			try {
				if (remainder == null) {
					filter = filterConstructor.newInstance();
				}
				else {
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
		}
		else {
			return tailFilter;
		}
	}
}
