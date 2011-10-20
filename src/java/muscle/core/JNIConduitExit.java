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

package muscle.core;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import java.util.concurrent.LinkedBlockingQueue;
import utilities.MiscTool;

import muscle.core.kernel.RawKernel;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import muscle.Constant;
import muscle.exception.MUSCLERuntimeException;
import muscle.core.wrapper.DataWrapper;
import utilities.jni.JNIMethod;
import utilities.Transmutable;


/**
exit which can directly be called from native code<br>
C for conduit type, R for raw jni type
@author Jan Hegewald
*/
public class JNIConduitExit<C,R> extends ConduitExit<C> {

	private Class<R> jniClass;
	private Transmutable<C,R> transmuter;

	
	//
	public JNIConduitExit(Transmutable<C,R> newTransmuter, Class<R> newJNIClass, PortalID newPortalID, RawKernel newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);
		transmuter = newTransmuter;
		jniClass = newJNIClass;
	}	
	
	
	//
	public JNIMethod fromJavaJNIMethod() {
		return new JNIMethod(this, "fromJava", new Class[0], null, Object.class, jniClass);
	}
	
	
	//
	public R fromJava() {
		C data = receive();
		R rawData = transmuter.transmute(data);
		return rawData;
	}			
}
