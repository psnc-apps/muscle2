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

#ifndef MethodIDDescription_FD48F0B5_BBBF_4BE7_B92D_CA0DB24BB0BA
#define MethodIDDescription_FD48F0B5_BBBF_4BE7_B92D_CA0DB24BB0BA

#include <jni.h> 
#include <string>

namespace muscle {

/**
groups the name and signature strings required to creare a jmethodID
\author Jan Hegewald
*/
class MethodIDDescription
{
public:

	MethodIDDescription(const std::string newName, const std::string newSignature)
		: name(newName), signature(newSignature)
	{
	}
	
public:
	const std::string name;
	const std::string signature;
};

} // EO namespace muscle
#endif
