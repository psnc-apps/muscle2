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

#include <SingleFromJavaTool.h>

namespace muscle {
/**
see header for class description
\author Jan Hegewald
*/


// template specializations
//

// it looks like we can get rid of these by now (-:

template<>
void SingleFromJavaTool<jboolean>::get(jboolean & data)
{
	data = method->call();
}

// do not cast bool to jboolean, so better provide a dedicated function here
template<>
void SingleFromJavaTool<bool>::get(bool & data)
{
	jboolean result = method->call();		
	data = result ? JNI_TRUE : JNI_FALSE;
}

template<>
void SingleFromJavaTool<jint>::get(jint & data)
{
	data = method->call();
}


template<>
void SingleFromJavaTool<jfloat>::get(jfloat & data)
{
	data = method->call();
}


template<>
void SingleFromJavaTool<jdouble>::get(jdouble & data)
{
	data = method->call();
}


} // EO namespace muscle
