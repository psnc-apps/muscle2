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

package muscle.test.jni;

import utilities.MiscTool;
import utilities.jni.JNIMethod;


/**
internal testing utility
@author Jan Hegewald
*/
class SingleBoolean {
		private Object nativeData;

		//
		public SingleBoolean() {
			System.out.println(this.getClass().getName()+" begin test ...");
			this.callNative(0, new JNIMethod(this, "fromJava"), new JNIMethod(this, "toJava", boolean.class));
			this.confirm();
			System.out.println(this.getClass().getName()+" end test\n");
		}

		private native void callNative(int mode, JNIMethod fromJava, JNIMethod toJava);

		//
		private boolean fromJava() {
			return true;
		}

		private void confirm() {
			boolean expectedData = this.fromJava();
			expectedData = !expectedData;

		if( !MiscTool.equalObjectValues(expectedData, this.nativeData) ) {
			throw new RuntimeException("test failed");
		}
		}
	}
