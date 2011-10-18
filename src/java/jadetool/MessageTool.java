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

package jadetool;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.JADEAgentManagement.WhereIsAgentAction;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.JADEAgentManagement.QueryAgentsOnLocation;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.util.Date;
import jade.core.Location;
import muscle.exception.JADERuntimeException;


/**
create standard messages to communicate with the ams
@author Jan Hegewald
*/
public class MessageTool {


	//
	public static ACLMessage createShutdownPlatformRequest(Agent ownerAgent) {
	
		ShutdownPlatform sdAction = new ShutdownPlatform();
		Action actExpr = new Action(ownerAgent.getAMS(), sdAction);

		Codec co = new SLCodec(0);
		ownerAgent.getContentManager().registerLanguage(co);
		ownerAgent.getContentManager().setValidationMode(true);

		Ontology ontology = JADEManagementOntology.getInstance();
		ownerAgent.getContentManager().registerOntology(ontology);

		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setSender(ownerAgent.getAID());
		request.addReceiver(ownerAgent.getAMS());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

		request.setOntology(ontology.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		try {
			ownerAgent.getContentManager().fillContent(request, actExpr);
		} catch (OntologyException e) {
			throw new JADERuntimeException(e);
		} catch (jade.content.lang.Codec.CodecException e) {
			throw new JADERuntimeException(e);
		}
				
		return request;
	}


	//
	public static ACLMessage createQueryAgentsOnLocationRequest(Agent ownerAgent, Location location) {
	
		QueryAgentsOnLocation qaolAction = new QueryAgentsOnLocation();
		qaolAction.setLocation(location);
		Action actExpr = new Action(ownerAgent.getAMS(), qaolAction);

		Codec co = new SLCodec(0);
		ownerAgent.getContentManager().registerLanguage(co);
		ownerAgent.getContentManager().setValidationMode(true);

		Ontology ontology = JADEManagementOntology.getInstance();
		ownerAgent.getContentManager().registerOntology(ontology);

		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setSender(ownerAgent.getAID());
		request.addReceiver(ownerAgent.getAMS());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

		request.setOntology(ontology.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		try {
			ownerAgent.getContentManager().fillContent(request, actExpr);
		} catch (OntologyException e) {
			throw new JADERuntimeException(e);
		} catch (jade.content.lang.Codec.CodecException e) {
			throw new JADERuntimeException(e);
		}
				
		return request;
	}


	//
	public static ACLMessage createQueryPlatformLocationsRequest(Agent ownerAgent) {
	
		QueryPlatformLocationsAction qplAction = new QueryPlatformLocationsAction();
		Action actExpr = new Action(ownerAgent.getAMS(), qplAction);

		Codec co = new SLCodec(0);
		ownerAgent.getContentManager().registerLanguage(co);
		ownerAgent.getContentManager().setValidationMode(true);

		Ontology ontology = JADEManagementOntology.getInstance();
		ownerAgent.getContentManager().registerOntology(ontology);

		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setSender(ownerAgent.getAID());
		request.addReceiver(ownerAgent.getAMS());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

		request.setOntology(ontology.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		try {
			ownerAgent.getContentManager().fillContent(request, actExpr);
		} catch (OntologyException e) {
			throw new JADERuntimeException(e);
		} catch (jade.content.lang.Codec.CodecException e) {
			throw new JADERuntimeException(e);
		}
				
		return request;
	}


	//
	public static ACLMessage createWhereIsAgentRequest(Agent ownerAgent, AID targetAID) {
	
		WhereIsAgentAction wiAction = new WhereIsAgentAction();
		wiAction.setAgentIdentifier(targetAID);
		Action actExpr = new Action(ownerAgent.getAMS(), wiAction);

		Codec co = new SLCodec(0);
		ownerAgent.getContentManager().registerLanguage(co);
		ownerAgent.getContentManager().setValidationMode(true);

		Ontology ontology = JADEManagementOntology.getInstance();
		ownerAgent.getContentManager().registerOntology(ontology);

		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.setSender(ownerAgent.getAID());
		request.addReceiver(ownerAgent.getAMS());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

		request.setOntology(ontology.getName());
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		try {
			ownerAgent.getContentManager().fillContent(request, actExpr);
		} catch (OntologyException e) {
			throw new JADERuntimeException(e);
		} catch (jade.content.lang.Codec.CodecException e) {
			throw new JADERuntimeException(e);
		}
				
		return request;
	}


//	//
//	// (inspired from JADE TestSuite addon TestUtility.java)
//	public static ACLMessage createRequestMessage(Agent sender, AID receiver, String language, String ontology) {
//
//		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
//		request.setSender(sender.getAID());
//		request.addReceiver(receiver);
//		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
//		request.setLanguage(language);
//		request.setOntology(ontology);
//		request.setReplyWith("rw"+sender.getName()+(new Date()).getTime());
//		request.setConversationId("conv"+sender.getName()+(new Date()).getTime());
//		return request;
//	}


	//
	public static boolean hasContentObject(ACLMessage message) {

		// if message.hasByteSequenceContent() == true the message was filled via setContentObject
		return message.hasByteSequenceContent();
	}
//	public static boolean hasContentObject(ACLMessage message) {
//
//
//	//String c = message.getContent();
//	//System.err.println("content:"+c);
//
//
//		java.io.Serializable contentObject = null;
//		try {
//			contentObject = message.getContentObject();
//			if(contentObject != null) {
//			
//				return true;
//				
////			Foo coData = (Foo)contentObject;
////			System.err.println("contentObject:"+coData.s);
////
////			if(contentObject instanceof DFAgentDescription)
////				System.err.println("inform is:DFAgentDescription");
////			else if(contentObject instanceof String)
////				System.err.println("inform is:String");
////			else if(contentObject instanceof java.io.Serializable)
////				System.err.println("inform is:java.io.Serializable");
////			else
////				System.err.println("inform is:unknown");
////
////			System.err.println("contentObject name:"+contentObject.getClass().getName());
//
//		}
//		
//		} catch (UnreadableException e) {
//		
//			if( !e.getMessage().equals("invalid stream header") ) {		
//				e.printStackTrace();
//			}
//		}
//
//		return false;
//	}
	
	
	public static MessageTemplate concatenate(MessageTemplate ... templates) {
		
		if(templates.length == 0)
			return null;
		else if(templates.length == 1)
			return templates[0];
		
		MessageTemplate result = MessageTemplate.and(templates[0], templates[1]);
		
		if(templates.length > 2) {
		
			for(int i = 2; i < templates.length; i++) {
				result = MessageTemplate.and(result, templates[i]);
			}
		
		}
		
		return result;
	}

}