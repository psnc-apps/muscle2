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

package muscle.core.messaging.signal;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import muscle.Constant;
import muscle.core.kernel.RawKernel;
import muscle.exception.MUSCLERuntimeException;
import muscle.core.wrapper.DataWrapper;
import utilities.jni.JNIMethod;
import javatool.ArraysTool;
import utilities.MiscTool;


/**
@author Jan Hegewald
*/
public interface Signal extends java.io.Serializable {
		
//	//
//	public void put(E d);	
//
//	//
//	public E take();
//	//
//	public E poll();
//
//	//
//	public String id();
}



