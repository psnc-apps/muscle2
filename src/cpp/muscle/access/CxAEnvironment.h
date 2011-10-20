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

#ifndef CXAENVIRONMENT_H
#define CXAENVIRONMENT_H

#include <jni.h> 
#include <string>

namespace muscle {

/**
provides access to the CxA environment, e.g. path settings
\author Jan Hegewald
*/
class CxAEnvironment
{
//
public:

	static std::string getPath(JNIEnv* & env, jobject & delegate); // delegate is usually a muscle.core.kernel.CAController.class
	static std::string getProperties(JNIEnv* & env, jobject & delegate); // delegate is usually a muscle.core.kernel.CAController.class
	static std::string getProperty(JNIEnv* & env, jobject & delegate, std::string key, bool & isNull); // delegate is usually a muscle.core.kernel.CAController.class
	
private:
};

} // EO namespace muscle
#endif
