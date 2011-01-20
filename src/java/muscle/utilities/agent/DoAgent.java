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

package muscle.utilities.agent;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jadetool.ContainerControllerTool;
import jadetool.MessageTool;

import java.util.ArrayList;
import java.util.logging.Logger;

import muscle.Constant;
import muscle.exception.MUSCLERuntimeException;
import muscle.logging.AgentLogger;



/**
helper agent to perform a task (usually a behaviour) and send back results to its owner agent
@author Jan Hegewald
*/
public class DoAgent extends Agent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static final int MANDATORY_ARG_COUNT = 1;
	private AgentLogger logger;
//	private DoAgentArgs args;
	private Agent ownerAgent;
	private DataStore resultData;


	//
	public static MessageTemplate spawn(Class<? extends DoAgent> doAgentClass, Agent newOwnerAgent, Object ... optionalArgs) {

		// do not create a simple logger with an agent class name because we might use the agent class name to create an AgentLogger
		Logger simpleLogger = java.util.logging.Logger.getLogger(javatool.ClassTool.getName(doAgentClass)+".spawn");

		Object[] rawArgs = new Object[DoAgent.MANDATORY_ARG_COUNT+optionalArgs.length];
		int i = 0;
		rawArgs[i] = newOwnerAgent;
		i++;
		for(Object o : optionalArgs) {
			rawArgs[i] = o;
			i++;
		}

		String agentName = doAgentClass.getName()+"<"+newOwnerAgent.here().getName()+"><"+System.currentTimeMillis()+">";
		simpleLogger.info("spawning agent: <"+agentName+">");

		AgentController controller = ContainerControllerTool.createUniqueNewAgent(newOwnerAgent.getContainerController(), agentName, javatool.ClassTool.getName(doAgentClass), rawArgs);

		// get the AID of the agent
		AID spawnedAID = null;
		try {
			spawnedAID = new AID(controller.getName(), AID.ISGUID);
		} catch (jade.wrapper.StaleProxyException e) {
			throw new MUSCLERuntimeException(e);
		}

		// start agent
		try {
			controller.start();
		} catch (jade.wrapper.StaleProxyException e) {
			throw new MUSCLERuntimeException(e);
		}

		MessageTemplate template = MessageTool.concatenate(
												MessageTemplate.MatchProtocol(Constant.Protocol.DOAGENT_RESULTS)
												, MessageTemplate.MatchSender(spawnedAID)
												);

		return template;
	}


	//
	@Override
	final protected void setup() {

		this.logger = AgentLogger.getLogger(this);

		Object[] rawArgs = this.getArguments();

		if(rawArgs.length < DoAgent.MANDATORY_ARG_COUNT) {
			this.logger.severe("got no args to configure from -> terminating");
			this.doDelete();
			return;
		}

		// split args in mandatory and optional part
		Object[] mandatoryArgs = new Object[DoAgent.MANDATORY_ARG_COUNT];
		Object[] optionalArgs = new Object[rawArgs.length - DoAgent.MANDATORY_ARG_COUNT];
		System.arraycopy(rawArgs, 0, mandatoryArgs, 0, mandatoryArgs.length);
		System.arraycopy(rawArgs, DoAgent.MANDATORY_ARG_COUNT, optionalArgs, 0, optionalArgs.length);

		// process optional args
		this.optionalSetup(optionalArgs);


		if(! (mandatoryArgs[0] instanceof Agent)) {
			this.logger.severe("got invalid args to configure from <"+javatool.ClassTool.getName(mandatoryArgs[0].getClass())+"> -> terminating");
			this.doDelete();
			return;
		}

		this.ownerAgent = (Agent)mandatoryArgs[0];

		SequentialBehaviour mainBehaviour = new SequentialBehaviour(this) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int onEnd() {

				DoAgent.this.sendReply();
				DoAgent.this.doDelete();
				return super.onEnd();
			}
		};
		this.resultData = mainBehaviour.getDataStore();
		this.addBehaviour(mainBehaviour);

		ArrayList<Behaviour> subBehaviours = this.getSubBehaviours();
		for( Behaviour b : subBehaviours ) {
			b.setAgent(this);
			b.setDataStore(this.resultData);
			mainBehaviour.addSubBehaviour(b);
		}

	}


	/**
	overwrite this to process optional args
	called before getSubBehaviours
	*/
	protected void optionalSetup(Object[] args) {

		// do nothing by default
	}


	/**
	overwrite this to initialize special behaviours in derived classes
	called after optionalSetup
	*/
	protected ArrayList<Behaviour> getSubBehaviours() {

		// do nothing by default
		return new ArrayList<Behaviour>();
	}


	//
	private void sendReply() {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol(Constant.Protocol.DOAGENT_RESULTS);
		msg.addReceiver(this.ownerAgent.getAID());

		try {
			msg.setContentObject(this.resultData);
		}
		catch(java.io.IOException e) {
			e.printStackTrace();
		}

		this.send(msg);
	}

}