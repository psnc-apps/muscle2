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

import java.lang.reflect.Array;

import javatool.ArraysTool;
import utilities.MiscTool;
import utilities.jni.JNIMethod;


/**
internal testing utility
@author Jan Hegewald
*/
class FloatArray {
	private Object nativeData;

	//
	public FloatArray() {
		System.out.println(this.getClass().getName()+" begin test ...");
		int[] modes = {0,1,2,3};
		for (int mode : modes) {
			System.out.println(this.getClass().getName()+" mode: "+mode);
			this.callNative(mode, new JNIMethod(this, "fromJava"), new JNIMethod(this, "toJava", float[].class));
			this.confirm();
		}
		System.out.println(this.getClass().getName()+" end test\n");
	}

	private native void callNative(int mode, JNIMethod fromJava, JNIMethod toJava);

	//
	private float[] fromJava() {
		float[] origData = {1.2f, 1.3f, -42.0f, 1234.56789f};
		return origData;
	}

	private void confirm() {
		float[] expectedData = this.fromJava();
		for(int i = 0; i < Array.getLength(expectedData); i++) {
			expectedData[i] *= 10.0f;
		}

		try {
			ArraysTool.assertEqualArrays(expectedData, this.nativeData, 1e-15);
		}
		catch(MiscTool.NotEqualException e) {
			throw new RuntimeException("test failed "+e.toString());
		}
	}
}
