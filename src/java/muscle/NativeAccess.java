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

package muscle;

import utilities.jni.MakeNative;

/**
internal class to generate cpp source code for the native muscle
@author Jan Hegewald
*/
public class NativeAccess {

//	// woho Java, why are there no heredoc strings?
	private static String license = "/**\nCopyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium\n\nGNU Lesser General Public License\n\nThis file is part of MUSCLE (Multiscale Coupling Library and Environment).\n\n    MUSCLE is free software: you can redistribute it and/or modify\n    it under the terms of the GNU Lesser General Public License as published by\n    the Free Software Foundation, either version 3 of the License, or\n    (at your option) any later version.\n\n    MUSCLE is distributed in the hope that it will be useful,\n    but WITHOUT ANY WARRANTY; without even the implied warranty of\n    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n    GNU Lesser General Public License for more details.\n\n    You should have received a copy of the GNU Lesser General Public License\n    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.\n*/\n\n";
//	private static String cppDoc = "/**\nconstant strings to provide names and signatures to access Java objects via JNI\n\\author Jan Hegewald\n*/\n";
	private static final String nl = System.getProperty("line.separator");
	//
	public static void main (String args[])  {
						
		String def = "NativeAccess"+"_DAABC73E_18CF_4601_BBA5_C7CF536C34FF";

		System.out.println(
		license+nl
		+"#ifndef "+def+nl
		+"#define "+def+nl
		+MakeNative.includeCode()
		+"namespace muscle {"+nl
		//
		+(new MakeNative(muscle.core.kernel.RawKernel.class)).toClass()
		+(new MakeNative(javatool.LoggerTool.class)).toClass()
		+(new MakeNative(java.util.logging.Logger.class)).toClass()
		+(new MakeNative(muscle.core.JNIConduitEntrance.class)).toClass()
		+(new MakeNative(muscle.core.JNIConduitExit.class)).toClass()
		+(new MakeNative(utilities.jni.JNIMethod.class)).toClass()
		//
		+"} // EO namespace muscle"+nl
		+"#endif"+nl
		);
	}

}
