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


import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import muscle.behaviour.MoveBehaviour;
import muscle.exception.MUSCLERuntimeException;
import muscle.logging.AgentLogger;
import utilities.MiscTool;


/**
receives a RemoteOutputStream
@author Jan Hegewald
*/
class RemoteOutputStreamReceiverAgent extends Agent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	transient private AgentLogger logger;


	//
	@Override
	final protected void setup() {

		this.logger = AgentLogger.getLogger(this);

		Object[] rawArgs = this.getArguments();

		if(rawArgs.length != 1 ) {
			this.logger.severe("got invalid args count to configure from -> terminating");
			this.doDelete();
			return;
		}

		if(! (rawArgs[0] instanceof RemoteOutputStreamReceiverAgentArgs)) {
			this.logger.severe("got invalid args to configure from <"+javatool.ClassTool.getName(rawArgs[0].getClass())+"> -> terminating");
			this.doDelete();
			return;
		}

		final RemoteOutputStreamReceiverAgentArgs args = (RemoteOutputStreamReceiverAgentArgs)rawArgs[0];

		this.addBehaviour(new MoveBehaviour(args.getTargetLocation(), this) {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void callback(Agent ownerAgent) {

				try {
					new FileOutputStream("outtest.txt");
				} catch (FileNotFoundException e) {
					throw new MUSCLERuntimeException(e);
				}
				ownerAgent.addBehaviour(new RemoteOutputStreamReceiverBehaviour(ownerAgent, args.getTemplate(), RemoteOutputStreamReceiverAgent.this.createOutputStream(args.getOutStreamName())));
			}
		});
	}


	//
	private OutputStream createOutputStream(String name) {

		if(name.equals("System.out")) {
			return System.out;
		} else if(name.equals("System.err")) {
			return System.err;
		} else {
			try {
System.out.println("creating file: <"+MiscTool.joinPaths(MiscTool.pwd(), name)+">");
				return new FileOutputStream(name);
			} catch (FileNotFoundException e) {
				throw new MUSCLERuntimeException(e);
			}
		}
	}


	//
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

		// do default deserialization
		in.defaultReadObject();

		// init transient fields
		this.logger = AgentLogger.getLogger(this);
	}


	//
	static public class RemoteOutputStreamReceiverAgentArgs implements java.io.Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final Location targetLocation;
		private final MessageTemplate template;
		private final String outStreamName;

		//
		public RemoteOutputStreamReceiverAgentArgs(Location newTargetLocation, MessageTemplate newTemplate, String newOutStreamName) {

			this.targetLocation = newTargetLocation;
			this.template = newTemplate;
			this.outStreamName = newOutStreamName;
		}

		//
		public Location getTargetLocation() {

			return this.targetLocation;
		}

		//
		public MessageTemplate getTemplate() {

			return this.template;
		}

		//
		public String getOutStreamName() {

			return this.outStreamName;
		}
	}


	//
	static public class RemoteOutputStreamReceiverBehaviour extends SimpleBehaviour {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		MessageTemplate template;
		OutputStream outStream;


		//
		public RemoteOutputStreamReceiverBehaviour(Agent ownerAgent, MessageTemplate newTemplate, OutputStream newOutStream) {
			super(ownerAgent);

			this.template = newTemplate;
			this.outStream = newOutStream;
		}


		//
		@Override
		public void action() {

			// try to read new fileinput
			ACLMessage msg = this.myAgent.receive(this.template);
			if(msg == null) {
				this.block();
			} else {
				// read new input from message
				byte[] byteContent = msg.getByteSequenceContent();
				if(byteContent != null) {
					// append input to our file
					try {
						this.outStream.write(byteContent);
					} catch (IOException e) {
						throw new MUSCLERuntimeException(e);
					}
				}
				else {
					// close the stream
					// do not close stream if it is System.out or System.err
					if(!this.outStream.equals(System.out) || !this.outStream.equals(System.err)) {
						try {
							this.outStream.close();
						} catch (IOException e) {
							throw new MUSCLERuntimeException(e);
						}
					}
					this.outStream = null;

//					myAgent.removeBehaviour(this);
					this.myAgent.doDelete();
				}
			}
		}


		//
		@Override
		public boolean done() {

			return false;
		}
	}

}