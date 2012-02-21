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

package muscle.behaviour;


import jade.core.Agent;
import jade.proto.AchieveREInitiator;
import jadetool.MessageTool;
import jade.lang.acl.ACLMessage;
import javatool.LoggerTool;
import java.util.logging.Level;
import utilities.data.Env;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.logging.Logger;


/**
tells the platform to shutdown
@author Jan Hegewald
*/
public class KillPlatformBehaviour extends AchieveREInitiator {
	private transient final static Logger logger = Logger.getLogger(KillPlatformBehaviour.class.getName());
	
	//
	public KillPlatformBehaviour(Agent owner) {
		super(owner, MessageTool.createShutdownPlatformRequest(owner));
	}


	//
	protected void handleInform(ACLMessage inform) {		
		System.out.println("\nkilling platform\n");
		System.out.flush();
		
		// load logging.after_teardown.properties into LogManager
		// this way we can configure the system to be silent after the teardown signal

		logger.finest("loading logging_after_teardown_properties into LogManager");

		Env env = muscle.Env.ONLY.subenv(this.getClass());

		InputStream loggingConfig = null;
		String fileName = (String)env.get("logging_after_teardown_properties_path");
		if(fileName != null) {
			try {
				 loggingConfig = new FileInputStream(fileName);
			} catch (java.io.FileNotFoundException e) {
				loggingConfig = null;
				logger.warning(e.getMessage());
			}
		}

		if( loggingConfig != null ) {
			try {
				java.util.logging.LogManager.getLogManager().readConfiguration(loggingConfig);
			}
			catch(java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}