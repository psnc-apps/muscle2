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

package muscle.logging;

import jade.core.AID;
import jade.core.Agent;

import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import muscle.exception.MUSCLERuntimeException;


/**
a logger which prefixes every log message with the agent
@author Jan Hegewald
*/
public class AgentLogger extends java.util.logging.Logger {

	private AID aid;


	//
	private AgentLogger(String name, String resourceBundleName, AID newAid) {
		super(name, resourceBundleName);
		this.aid = newAid;
	}


	/**
	warning: if there are multiple agents of the same class, all will use the same (first) logger if called via<br>
	logger = AgentLogger.getLogger(getClass().getName(), getAID());<br>
	so better use e.g.<br>
	logger = AgentLogger.getLogger(getClass().getName()+getAID().getName(), getAID());<br>
	to get a unique logger for this agent
	*/
	private static synchronized AgentLogger getLogger(String loggerName, AID aid) {

		// make logger name unique for this agent, so we can create individual loggers for multiple agents of the same class (e.g. conduit)
		loggerName += "."+aid.getName();

		LogManager manager = LogManager.getLogManager();
		AgentLogger result = null;
		try {
			result = (AgentLogger)manager.getLogger(loggerName);
		}
		catch(java.lang.ClassCastException e) {
			throw new MUSCLERuntimeException("can not create AgentLogger -- a Logger already exists but instance is from other class: <"+manager.getLogger(loggerName).getClass().getName()+">");
		}

		if(result == null) {
			result = new AgentLogger(loggerName, null, aid);
			manager.addLogger(result);
			result = (AgentLogger)manager.getLogger(loggerName);
		}

		return result;
	}
	public static synchronized AgentLogger getLogger(Agent agent) {
		return AgentLogger.getLogger(agent.getClass().getName()+"."+agent.getAID().getName(), agent.getAID());
	}

	//
	@Override
	public void log(LogRecord record) {

		// if we do not call this here, the SourceMethodName will be set to this very method, i.e. log, which will be the method name appearing in the log message
		record.getSourceMethodName();

		record.setMessage("["+this.aid.getLocalName()+"] "+record.getMessage());

		super.log(record);
	}
}
