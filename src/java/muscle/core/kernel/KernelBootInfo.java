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
package muscle.core.kernel;

/**
this class should hold all info required to boot a kernel
@author Jan Hegewald
 */
public class KernelBootInfo {

	private String name; // nickname of this kernel, also name of the kernel-agent 
	private Class<? extends RawKernel> cls;
	
	public KernelBootInfo(String newName, Class<? extends RawKernel> newClass) {
		name = newName;
		cls = newClass;
	}

	public String getName() {
		return name;
	}

	public Class<? extends RawKernel> getKernelClass() {
		return cls;
	}

	public String toString() {
		return name + ":" + javatool.ClassTool.getName(cls);
	}
}
