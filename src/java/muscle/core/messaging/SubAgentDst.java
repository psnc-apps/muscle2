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

package muscle.core.messaging;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import utilities.MiscTool;
import muscle.core.kernel.RawKernel;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import muscle.Constant;
import muscle.exception.MUSCLERuntimeException;
import muscle.core.wrapper.DataWrapper;
import utilities.jni.JNIMethod;


/**
@author Jan Hegewald
*/
public interface SubAgentDst<T extends java.io.Serializable> {


}
