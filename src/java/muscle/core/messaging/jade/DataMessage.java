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

package muscle.core.messaging.jade;

import jade.lang.acl.ACLMessage;


/**
@author Jan Hegewald
*/
public class DataMessage<E> extends jade.lang.acl.ACLMessage implements Cloneable {

	// note: JADE sends messages differently if they are passed to a remote container or locally within the same container
	// for the remote container, a new ACLMessage is created and filled with the proper contents
	// for the local container, a clone is created via ACLMessage#clone and thus remains a DataMessage class including transient fields
	
	private static String SINKID_KEY = DataMessage.class.toString()+"#sinkId";
   
   private transient E storedItem;

	private String sinkID;
	private Long byteCount;
	
	//
	public DataMessage(String sid) {
		super(ACLMessage.INFORM);
		sinkID = sid;
      addUserDefinedParameter(SINKID_KEY, sinkID);
	}


   //
   public static DataMessage extractFromACLMessage(ACLMessage aclmsg) {

      String sid;
      if((sid = aclmsg.getUserDefinedParameter(SINKID_KEY))==null)
         //throw new IllegalArgumentException("can not convert this ACLMessage to a DataMessage");
			return null;

      // copy some relevant settings from the 
		DataMessage dmsg = new DataMessage(sid);
		dmsg.setSender(aclmsg.getSender());
      dmsg.setLanguage(aclmsg.getLanguage());
      dmsg.setProtocol(aclmsg.getProtocol());
      dmsg.setPerformative(aclmsg.getPerformative());
      dmsg.setEnvelope(aclmsg.getEnvelope());
      dmsg.setConversationId(aclmsg.getConversationId());
      dmsg.setByteSequenceContent(aclmsg.getByteSequenceContent());

      return dmsg;
   }


	//
   @Override
	public Object clone() {
		return super.clone();
	}
	
	
	//
	public void store(E item, Long newByteCount) {

		//assert !hasByteSequenceContent();
		assert newByteCount == null || newByteCount > 0;
		byteCount = newByteCount;
		storedItem = item;
	}
	
	
	//
	public Long getByteCount() {
	
		return byteCount;
	}


	//
	public E getStored() {

		//assert !hasByteSequenceContent();
		return storedItem;
	}
	
	
	//
	public String getSinkID() {
		return sinkID;
	}	

}
