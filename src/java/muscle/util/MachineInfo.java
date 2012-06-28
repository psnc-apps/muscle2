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

package muscle.util;


/**
information about host machine
@author Jan Hegewald
*/
public class MachineInfo {
	
	public static String getOSName() {

		return System.getProperty("os.name");		
	}
	
	
	//
	public static String getOSVersion() {

		return System.getProperty("os.version");		
	}


	//
	public static String getJavaVersion() {

		return System.getProperty("java.version");
	}


	//
	public static String getJavaClasspath() {

		return System.getProperty("java.class.path");
	}


	//
	public static String getUserName() {

		return System.getProperty("user.name");
	}


	// the maximum amount of memory that the virtual machine will attempt to use
	// measured in bytes
	public static long getMaxMemory() {
		
		return Runtime.getRuntime().maxMemory();
	}


	//
	public static long getFreeMemory() {
		
		return getMaxMemory() - getUsedMemory();
	}


	//
	public static long getUsedMemory() {

		long totalMem = Runtime.getRuntime().totalMemory();
		long freeMem = Runtime.getRuntime().freeMemory();
		long usedMem = totalMem - freeMem;
		return usedMem;
	}
	
	
	//
	public static String getFullInfo() {
	
		StringBuilder info = new StringBuilder();
	
		info.append("OS name: ");
		info.append(MachineInfo.getOSName());
		
		info.append("\nOS version: ");
		info.append(MachineInfo.getOSVersion());
		
		info.append("\nuser: ");
		info.append(MachineInfo.getUserName());
		
		info.append("\nmaximum jvm memory: ");
		info.append(bytesToString(MachineInfo.getMaxMemory()));
		
		info.append("\nused memory: ");
		info.append(bytesToString(MachineInfo.getUsedMemory()));
		
		info.append("\nfree memory: ");
		info.append(bytesToString(MachineInfo.getFreeMemory()));
		
		info.append("\nclasspath: ");
		info.append(MachineInfo.getJavaClasspath());
		
		info.append("\nJava version: ");
		info.append(MachineInfo.getJavaVersion());

		return info.toString();
	}
	
	
	//
	private static String bytesToString(long byteCount) {
	
		return String.valueOf((long)((double)byteCount/1042.0/1024.0))+" Mbytes";
	}

}


