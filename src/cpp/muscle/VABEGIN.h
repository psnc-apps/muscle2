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

#ifndef VABEGIN_E892B405_4281_47D3_9AD0_DD3AF6501CFE
#define VABEGIN_E892B405_4281_47D3_9AD0_DD3AF6501CFE


namespace muscle {


/**
helper class to introduce a new type which we can use as the first mandatory arg in a varargs function
this way it is not ambiguous to call a varargs function with only one argument (i.e. no varargs)
\author Jan Hegewald
*/
class VABEGIN
{

public:
	static const VABEGIN FIRST;

};


} // EO namespace muscle
#endif
