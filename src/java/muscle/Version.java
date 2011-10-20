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

import utilities.JVM;

/**
just the version of this MUSCLE
@author Jan Hegewald
*/
public class Version {

	private static String NATIVE_LIB_BASENAME = "muscle";
	private static String INFO_TEXT = "This is the Multiscale Coupling Library and Environment (MUSCLE) from 2010-01-11_13-51-27";


	//
	private static native String nativeInfo();
	
	// info about the native library, if available
	private static String nativeText() {
	
		if(nativeLibraryAvailable()) {
			return "native library available ("+nativeInfo()+")";
		}
		else {
			return "native library not available";
		}
	}


	// info about the native library, if available
	public static boolean nativeLibraryAvailable() {
	
		// see if we can load the native lib
		try {
			System.loadLibrary(NATIVE_LIB_BASENAME);
		}
		catch (java.lang.UnsatisfiedLinkError e) {
			return false;
		}
	
		return true;
	}


	//
	public static String info() {
	
		return INFO_TEXT.replaceAll("@@", ""); // remove the @@ in case the version text has not been modified
	}
	
	
	//
	public static void main(String[] args) {

      String mode = JVM.is64bitJVM() ? "64":"32";
		System.out.println(info()+" running in "+mode+"-bit mode, "+nativeText());		
	}
}
