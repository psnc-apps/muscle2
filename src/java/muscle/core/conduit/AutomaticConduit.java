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


import java.lang.reflect.Constructor;
import java.util.ArrayList;

import muscle.core.conduit.filter.AbstractFilter;
import muscle.core.conduit.filter.BlockAfterTimeFilter;
import muscle.core.conduit.filter.ConsoleWriterFilter;
import muscle.core.conduit.filter.DropFilter;
import muscle.core.conduit.filter.Filter;
import muscle.core.conduit.filter.LinearInterpolationFilterDouble;
import muscle.core.conduit.filter.LinearTimeInterpolationFilterDouble;
import muscle.core.conduit.filter.MultiplyFilterDouble;
import muscle.core.conduit.filter.NullFilter;
import muscle.core.conduit.filter.PipeFilter;
import muscle.core.conduit.filter.ReproduceFilterDouble;
import muscle.core.conduit.filter.TimeFactorFilter;
import muscle.core.conduit.filter.TimeOffsetFilter;
import muscle.core.conduit.filter.WrapperFilter;
import muscle.core.conduit.filter.WrapperFilterHead;
import muscle.core.conduit.filter.WrapperFilterTail;
import muscle.core.messaging.jade.DataMessage;
import muscle.core.wrapper.DataWrapper;
import muscle.exception.MUSCLERuntimeException;
import muscle.logging.AgentLogger;
import utilities.MiscTool;


/**
will automatically create a filter chain from a list of filter names<br>
e.g. in the connection scheme write<br>
exit@receiver muscle.core.conduit.AutomaticConduit#A(muscle.core.conduit.filter.MultiplyFilterDouble_42) entrance@sender<br>
to load the MultiplyFilterDouble_42 and pass a 42 to its constructor
@author Jan Hegewald
*/
public class AutomaticConduit extends BasicConduit {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


//
   @Override
	protected void constructMessagePassingMechanism() {

		// init filter chain
      Filter filters = this.initFilterChain(new DataSenderFilter());

		MessageReceiverBehaviour receiver = new MessageReceiverBehaviour(filters);
		this.addBehaviour(receiver);

	}


	//
	protected Filter initFilterChain(final Filter filterTail) {

		AgentLogger logger = AgentLogger.getLogger(this);
		logger.fine("filter args: <"+MiscTool.joinItems(this.getOptionalArgs(), ", ")+">");

		// assume our optional args are filter names
		// cast our optional args to filter names
		ArrayList<String> filterArgs = new ArrayList<String>();
		for(Object o : this.getOptionalArgs()) {
			filterArgs.add((String)o);
		}

      final DataMessage dataMessage = new DataMessage(this.exitName);
		dataMessage.addReceiver(this.exitAgent);


		final Filter<DataWrapper> wrapper2dmsg = new AbstractFilter<DataWrapper>() {

         @Override
         public void put(DataWrapper in) {
            dataMessage.store(in, null);
            filterTail.put(dataMessage);
         }
      };

      WrapperFilter tail = new WrapperFilterTail(this.getExitDataTemplate()) {

         @Override
         public void result(DataWrapper resultData) {
            wrapper2dmsg.put(resultData);
         }
      };

		WrapperFilter filters = this.automaticPipeline(filterArgs, tail);

		final WrapperFilter head;
		try {
			head = new WrapperFilterHead(filters, this.getEntranceDataTemplate());
		}
		catch (muscle.exception.DataTemplateMismatchException e) {
			throw new MUSCLERuntimeException(e);
		}


		final Filter<DataMessage<DataWrapper<?>>> dmsg2wrapper = new AbstractFilter<DataMessage<DataWrapper<?>>>() {

         @Override
         public void put(DataMessage<DataWrapper<?>> in) {
            DataWrapper wrapper = in.getStored();
            head.put(wrapper);
         }
      };

		return dmsg2wrapper;
	}


	//
	private WrapperFilter automaticPipeline(ArrayList<String> filterNames, WrapperFilter tailFilter) {

		WrapperFilter filter = tailFilter;
		for(int i = filterNames.size()-1; i > -1; i--) {
			filter = this.filterForName(filterNames.get(i), filter);
		}

		return filter;
	}


	//
	private WrapperFilter filterForName(String name, WrapperFilter tailFilter) {

		// split any args from the preceding filter name
		String[] tmp = name.split("_", 2); // 2 means only split once
		name = tmp[0];
		String remainder = null;
		if(tmp.length > 1) {
			remainder = tmp[1];
		}

		//
		// filters without args
		//

		if(name.equals("null")) {
			return new NullFilter(tailFilter);
		} else if(name.equals("pipe")) {
			return new PipeFilter(tailFilter);
		}

		else if(name.equals("console")) {
			return new ConsoleWriterFilter(tailFilter);
		} else if(name.equals("linearinterpolation")) {
			return new LinearInterpolationFilterDouble(tailFilter);
		} else if(name.equals("reproduce")) {

			return new ReproduceFilterDouble(tailFilter, Integer.valueOf(remainder));
		}

		else if(name.equals("multiply")) {
			return new MultiplyFilterDouble(tailFilter, Double.valueOf(remainder));
		}

		else if(name.equals("drop")) {
			return new DropFilter(tailFilter, Integer.valueOf(remainder));
		}

		else if(name.equals("timeoffset")) {
			return new TimeOffsetFilter(tailFilter, Integer.valueOf(remainder));
		}

		else if(name.equals("timefactor")) {
			return new TimeFactorFilter(tailFilter, Integer.valueOf(remainder));
		}

		else if(name.equals("blockafter")) {
			return new BlockAfterTimeFilter(tailFilter, Integer.valueOf(remainder));
		}

		else if(name.equals("lineartimeinterpolation")) {
			return new LinearTimeInterpolationFilterDouble(tailFilter, Integer.valueOf(remainder));
		}

		//
		// assume name refers to a class name
		//

		else {
			Class<? extends WrapperFilter> filterClass = null;
			try {
				filterClass = (Class<? extends WrapperFilter>)Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new MUSCLERuntimeException(e);
			}

			// try to find constructor with tailFilter
			Constructor<? extends WrapperFilter> filterConstructor = null;
			try {
				filterConstructor = filterClass.getDeclaredConstructor(WrapperFilter.class);
			}
			catch (java.lang.NoSuchMethodException e) {
				throw new MUSCLERuntimeException(e);
			}

			try {
				return filterConstructor.newInstance(tailFilter);
			}
			catch (java.lang.InstantiationException e) {
				throw new MUSCLERuntimeException(e);
			}
			catch (java.lang.IllegalAccessException e) {
				throw new MUSCLERuntimeException(e);
			}
			catch (java.lang.reflect.InvocationTargetException e) {
				throw new MUSCLERuntimeException(e);
			}

		}

//		throw new MUSCLERuntimeException("unknown filter: <"+name+">");
	}

}

