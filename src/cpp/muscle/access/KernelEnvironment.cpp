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

#include <access/KernelEnvironment.h> 

#include <JNITool.h>
#include <access/NativeAccess.h>

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/

std::string KernelEnvironment::getTmpPath(JNIEnv* & env, jobject & delegate)
{
	muscle_core_kernel_CAController controller(env, delegate);
	jstring path = controller.getTmpPath();
	
	bool isNull;
	return JNITool::stringFromJString(env, path, isNull);
}


std::string KernelEnvironment::getPath(JNIEnv* & env, jobject & delegate)
{	
	muscle_core_kernel_CAController controller(env, delegate);
	jstring path = controller.getKernelPath();
	
	bool isNull;
	return JNITool::stringFromJString(env, path, isNull);
}
	

} // EO namespace muscle
