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

#include <SingleToJavaTool.h>

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/

using namespace std;



// template specializations
//

template<>
void SingleToJavaTool<jboolean>::put(const jboolean & data)
{	
	method->call(VABEGIN::FIRST, data);	
}

// do not cast bool to jboolean, so better provide a dedicated function here
template<>
void SingleToJavaTool<bool>::put(const bool & data)
{	
	if(data == true)
		method->call(VABEGIN::FIRST, JNI_TRUE);
	else
		method->call(VABEGIN::FIRST, JNI_FALSE);
}

template<>
void SingleToJavaTool<jint>::put(const jint & data)
{	
	method->call(VABEGIN::FIRST, data);
}

template<>
void SingleToJavaTool<jfloat>::put(const jfloat & data)
{	
	method->call(VABEGIN::FIRST, data);
}

template<>
void SingleToJavaTool<jdouble>::put(const jdouble & data)
{	
	method->call(VABEGIN::FIRST, data);
}


} // EO namespace muscle
