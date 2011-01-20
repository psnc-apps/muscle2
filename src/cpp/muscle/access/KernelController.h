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

#ifndef KernelController_4320807E_C59B_410B_A7BD_25D2A79270AA
#define KernelController_4320807E_C59B_410B_A7BD_25D2A79270AA

#include <jni.h> 
#include <string>
#include<vector>

#if __GNUC__ >= 4
	#include <tr1/memory>
#else
	#include <memory>
#endif

#include <access/JNICounterpart.h>
#include <access/CxAEnvironment.h>
#include <access/KernelEnvironment.h>
#include <access/NativeAccess.h>

namespace muscle {

class Logger;

/**
complementary class for the Java class muscle.core.kernel.CAController
\brief communicate with the kernel (willStop, properties, logging, paths)
\author Jan Hegewald
*/
class KernelController : public JNICounterpart
{
//
public:

	//
	KernelController(JNIEnv* newEnv, jobject newKernelController);
	KernelController(JNIEnv* newEnv, jobject newKernelController, std::string newCLASSNAME);
	~KernelController();
	
	bool willStop();
	Logger& getLogger();	
	
	std::string getCxAPath();
	std::string getKernelPath();
	std::string getTmpPath();
	std::string getCxAProperty(const std::string & key, bool & isNull);
	std::string getCxAProperties();

// disallow copy constructor and assignment
// because we would need a custom implementation
private:
	KernelController(/*const*/ KernelController& other)
		: JNICounterpart(other.getEnv(), NULL, muscle_core_kernel_CAController::_CLASSNAME()), logger(NULL)
	{
	}
	KernelController& operator=(/*const*/ KernelController& other)
	{
		return other;// never use this, only here to make the compiler happy
	}

//
private:
	std::tr1::shared_ptr<muscle::AbstractMethodCaller<jboolean> > willStopMethod;
	Logger* logger;
};


} // EO namespace muscle
#endif
