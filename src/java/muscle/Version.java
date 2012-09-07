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

import muscle.util.JVM;

/**
just the version of this MUSCLE
@author Jan Hegewald
*/
public class Version {
	private final static String VERSION_NUM = "2.0-RC";
	private final static String INFO_TEXT = "This is the Multiscale Coupling Library and Environment (MUSCLE) " + VERSION_NUM + " (SVN revision: $Rev$ from $Date$):";
	
	public static String info() {
		 // remove the $$ in case the version text has not been modified; also remove all other SVN keywords
		return INFO_TEXT.replaceAll("$$", "").replace("$Rev$", "");
	}

	public static void main(String[] args) {
		String mode = JVM.is64bitJVM() ? "64":"32";
		System.out.println(info()+" running in "+mode+"-bit mode");		
	}
}
