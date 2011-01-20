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

#ifndef VERSION_399DAACB_47F9_4DB2_8A9E_59A0196A500F
#define VERSION_399DAACB_47F9_4DB2_8A9E_59A0196A500F

#include <iostream>
#include <sstream>
#include <cassert> 
#include <stdexcept>


namespace muscle {

/**
version info for native part of the muscle
\author Jan Hegewald
*/
class Version
{
public:

	static std::string info()
	{
		std::stringstream text;

		text <<"built at "<<__DATE__<<" "<<__TIME__;
	
		text<<", assertions";
		bool assertionsActive(false);
		assert( (assertionsActive=true)==true );
		if(assertionsActive)
			text<<" : active";
		else
			text<<" : not active";

		text<<", exceptions";
		bool exceptionsActive(false);
		try {
			throw std::runtime_error("");
		}
		catch(std::runtime_error& e)
		{
			exceptionsActive = true;
		}
		if(exceptionsActive)
			text<<" : active";
		else
			text<<" : not active";
		
		// check defines for _DEBUG DEBUG NDEBUG
		
		text<<", _DEBUG";
		#ifdef _DEBUG
		text<<" : defined";
		#else
		text<<" : not defined";		
		#endif

		text<<", DEBUG";
		#ifdef DEBUG
		text<<" : defined";
		#else
		text<<" : not defined";		
		#endif

		text<<", NDEBUG";
		#ifdef NDEBUG
		text<<" : defined";
		#else
		text<<" : not defined";		
		#endif
		
		return text.str();
	}
};

} // EO namespace muscle


#endif
