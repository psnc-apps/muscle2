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

import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.behaviour.MoveBehaviour;
import utilities.MiscTool;
import muscle.exception.MUSCLERuntimeException;


/**
receives a RemoteOutputStream
@author Jan Hegewald
*/
class RemoteOutputStreamReceiverAgent extends Agent {
	
	transient private final static Logger logger = Logger.getLogger(RemoteOutputStreamReceiverAgent.class.getName());
		
	
	//
	final protected void setup() {
		Object[] rawArgs = getArguments();
		
		if(rawArgs.length != 1 ) {
			logger.severe("got invalid args count to configure from -> terminating");
			doDelete();		
			return;
		}				
		
		if(! (rawArgs[0] instanceof RemoteOutputStreamReceiverAgentArgs)) {
			logger.log(Level.SEVERE, "got invalid args to configure from <{0}> -> terminating", javatool.ClassTool.getName(rawArgs[0].getClass()));
			doDelete();
			return;		
		}
		
		final RemoteOutputStreamReceiverAgentArgs args = (RemoteOutputStreamReceiverAgentArgs)rawArgs[0];
		
		addBehaviour(new MoveBehaviour(args.getTargetLocation(), this) {
			
			public void callback(Agent ownerAgent) {

				OutputStream outStream;
				try {
					outStream = new FileOutputStream("outtest.txt");
				} catch (FileNotFoundException e) {
					throw new MUSCLERuntimeException(e);
				}
				ownerAgent.addBehaviour(new RemoteOutputStreamReceiverBehaviour(ownerAgent, args.getTemplate(), createOutputStream(args.getOutStreamName())));
			}
		});
	}


	//
	private OutputStream createOutputStream(String name) {
	
		if(name.equals("System.out"))
			return System.out;
		else if(name.equals("System.err"))
			return System.err;
		// else we assume <name> is a file name where we should write to
		else {
			try {
System.out.println("creating file: <"+MiscTool.joinPaths(MiscTool.pwd(), name)+">");
				return new FileOutputStream(name);
			} catch (FileNotFoundException e) {
				throw new MUSCLERuntimeException(e);
			}
		}
	}

	//
	static public class RemoteOutputStreamReceiverAgentArgs implements java.io.Serializable {

		private final Location targetLocation;
		private final MessageTemplate template;
		private final String outStreamName;

		//
		public RemoteOutputStreamReceiverAgentArgs(Location newTargetLocation, MessageTemplate newTemplate, String newOutStreamName) {
	
			targetLocation = newTargetLocation;
			template = newTemplate;
			outStreamName = newOutStreamName;
		}

		//
		public Location getTargetLocation() {
		
			return targetLocation;
		}

		//
		public MessageTemplate getTemplate() {
		
			return template;
		}

		//
		public String getOutStreamName() {
		
			return outStreamName;
		}
	}
	

	//
	static public class RemoteOutputStreamReceiverBehaviour extends SimpleBehaviour {
	
		MessageTemplate template;
		OutputStream outStream;

		
		//
		public RemoteOutputStreamReceiverBehaviour(Agent ownerAgent, MessageTemplate newTemplate, OutputStream newOutStream) {
			super(ownerAgent);
			
			template = newTemplate;
			outStream = newOutStream;
		}

		
		//
		public void action() {

			// try to read new fileinput
			ACLMessage msg = myAgent.receive(template);
			if(msg == null)
				block();
			else {
				// read new input from message
				byte[] byteContent = msg.getByteSequenceContent();
				if(byteContent != null) {
					// append input to our file
					try {
						outStream.write(byteContent);
					} catch (IOException e) {
						throw new MUSCLERuntimeException(e);
					}
				}
				else {
					// close the stream
					// do not close stream if it is System.out or System.err
					if(!outStream.equals(System.out) || !outStream.equals(System.err)) {
						try {
							outStream.close();
						} catch (IOException e) {
							throw new MUSCLERuntimeException(e);
						}
					}
					outStream = null;
					
//					myAgent.removeBehaviour(this);
					myAgent.doDelete();
				}
			}
		}

		
		//
		public boolean done() {

			return false;
		}
	}

}