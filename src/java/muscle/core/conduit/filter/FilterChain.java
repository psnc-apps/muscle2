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

package muscle.core.conduit.filter;

import muscle.core.conduit.*;
import jade.core.AID;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.io.OutputStreamWriter;
import muscle.core.DataTemplate;
import muscle.core.messaging.jade.DataMessage;


/**
the inner filter mechanism of a conduit
@author Jan Hegewald
*/
public class FilterChain {

	AID entranceAgent;
	String entranceName;
	private DataTemplate entranceDataTemplate;
	AID exitAgent;
	String exitName;
	private DataTemplate exitDataTemplate;
	private ArrayList<Object> optionalArgs;
	private MessageTemplate receiveTemplate;
	
	private ResourceStrategy resourceStrategy;
	
	private OutputStreamWriter traceReceiveWriter;
	private OutputStreamWriter traceSendWriter;
	

	//
	public ArrayList<Object> getOptionalArgs() {
	
		return optionalArgs;
	}
	
	
	//
	public DataTemplate getEntranceDataTemplate() {
	
		return entranceDataTemplate;
	}


	//
	public DataTemplate getExitDataTemplate() {
	
		return exitDataTemplate;
	}
	
	
	//
	public FilterChain() {
	
	}
	
	
	//
	protected Filter initFilterChain(Filter filterTail) {
	
		// we do not insert other filters by default
		// by default we directly pass the entrance data to the exit
		return filterTail;
	}

	//
	private Filter initHeadFilters(Filter remainingFilters) {
			
		return remainingFilters;
	}

	//
	protected Filter initTailFilters(Filter lastFilter) {
		
		return lastFilter;
	}


	
	public Filter buildFilterChain(Filter<DataMessage> senderFilter) {

		// init filters

		Filter filters = initTailFilters(senderFilter);

		filters = initFilterChain(filters);

		filters = initHeadFilters(filters);

      return filters;
	}

}
