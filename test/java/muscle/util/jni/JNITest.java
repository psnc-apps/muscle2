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

package muscle.util.jni;

import java.lang.reflect.Array;
import java.util.Arrays;
import muscle.util.MiscTool;
import muscle.util.jni.JNIMethod;


/**
internal testing utility
runs tests on our JNI helper tools and tests communication to/from C++ and fortran
@author Jan Hegewald
*/
public class JNITest {


	//
	static {
		
		System.load("CppController"); // src/native/cpp/test/bin/libCppController.dylib
	}
	
	
	//
	public static void main (String args[]) {

		new SingleBoolean();
		new SingleInt();
		new SingleFloat();
		new SingleDouble();

		new BooleanArray();
		new IntArray();
		new FloatArray();
		new DoubleArray();
	}

	
}
