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

import muscle.core.kernel.RawKernel;
import utilities.Transmutable;
import utilities.jni.JNIMethod;


/**
exit which can directly be called from native code<br>
C for conduit type, R for raw jni type
@author Jan Hegewald
*/
public class JNIConduitExit<C,R> extends ConduitExit<C> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Class<R> jniClass;
	private Transmutable<C,R> transmuter;


	//
	public JNIConduitExit(Transmutable<C,R> newTransmuter, Class<R> newJNIClass, PortalID newPortalID, RawKernel newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		super(newPortalID, newOwnerAgent, newRate, newDataTemplate);
		this.transmuter = newTransmuter;
		this.jniClass = newJNIClass;
	}


	//
	public JNIMethod fromJavaJNIMethod() {
		return new JNIMethod(this, "fromJava", new Class[0], null, Object.class, this.jniClass);
	}

	//
	public R fromJava() {
		C data = this.receive();
		R rawData = this.transmuter.transmute(data);
		return rawData;
	}
}
