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

package muscle.utilities;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jadetool.ContainerControllerTool;
import jadetool.MessageTool;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import muscle.exception.MUSCLERuntimeException;


/**
streams remotely via an ACLMessage
@author Jan Hegewald
*/
public class RemoteOutputStream extends OutputStream {

	public static final String PROTOCOL = "RemoteOutputStream.PROTOCOL";

	private byte buffer[];
	private int validLength;

	private Agent myAgent;
	private AID receiver;

	//
	public RemoteOutputStream(Agent newMyAgent, Location newTargetLocation, String newOutputStreamName, int newBufferSize) {

		this.myAgent = newMyAgent;

		if(newBufferSize <= 0) {
			throw new IllegalArgumentException("Buffer size <= 0 <"+newBufferSize+">");
		}
		this.buffer = new byte[newBufferSize];

		// spawn receiver agent
		this.receiver = this.spawn(newTargetLocation, newOutputStreamName);
	}


	//
	@Override
	public void write(int inByte) throws IOException {

		if(this.validLength >= this.buffer.length) {
			this.flushBuffer();
		}

		this.buffer[this.validLength++] = (byte)inByte;
	}


	//
	@Override
	public void flush() throws IOException {

		super.flush();
		this.flushBuffer();
	}


	//
	@Override
	public void close() throws IOException {

		this.flush();

		// send the close message
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(this.receiver);
		msg.setProtocol(RemoteOutputStream.PROTOCOL);

		msg.setByteSequenceContent(null);
		this.myAgent.send(msg);
	}


	//
	private AID spawn(Location newTargetLocation, String outputStreamName) {

		// do not create a simple logger with an agent class name because we might use the agent class name to create an AgentLogger
		Logger simpleLogger = java.util.logging.Logger.getLogger(javatool.ClassTool.getName(RemoteOutputStreamReceiverAgent.class)+".spawn");

		// generate message template to receive on the remote side
		MessageTemplate template = MessageTool.concatenate(
											MessageTemplate.MatchProtocol(RemoteOutputStream.PROTOCOL)
											, MessageTemplate.MatchSender(this.myAgent.getAID())
											);

		RemoteOutputStreamReceiverAgent.RemoteOutputStreamReceiverAgentArgs args = new RemoteOutputStreamReceiverAgent.RemoteOutputStreamReceiverAgentArgs(
				newTargetLocation, template, outputStreamName);

		Object[] rawArgs = {args};

		String agentName = javatool.ClassTool.getName(RemoteOutputStreamReceiverAgent.class);
		AgentController controller = ContainerControllerTool.createUniqueNewAgent(this.myAgent.getContainerController(), agentName, javatool.ClassTool.getName(RemoteOutputStreamReceiverAgent.class), rawArgs);

		// get the AID of the new agent
		AID spawnedAID = null;
		try {
			spawnedAID = new AID(controller.getName(), AID.ISGUID);
		} catch (jade.wrapper.StaleProxyException e) {
			throw new MUSCLERuntimeException(e);
		}
		simpleLogger.info("spawning agent: <"+spawnedAID.getName()+">");

		try {
			controller.start();
		} catch (jade.wrapper.StaleProxyException e) {
			simpleLogger.severe("can not spawn agent: "+spawnedAID.getName());
			throw new MUSCLERuntimeException(e);
		}

		return spawnedAID;
	}


	//
	private void flushBuffer() throws IOException {

		if (this.validLength > 0) {
			//byte[] data = java.util.Arrays.copyOfRange(buffer, 0, validLength); // Java 6
			// Java 5 workaround
			byte[] data = new byte[this.validLength];
			System.arraycopy(this.buffer, 0, data, 0, data.length);
			this.sendBuffer(data);

			this.validLength = 0;
		}
	}


	//
	private void sendBuffer(byte[] data) {

		// send data to remote receiver
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(this.receiver);
		msg.setProtocol(RemoteOutputStream.PROTOCOL);

		msg.setByteSequenceContent(data);
		this.myAgent.send(msg);
	}
}
