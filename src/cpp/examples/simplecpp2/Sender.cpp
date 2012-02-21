/*
Copyright 2012 MAPPER consortium

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

#include <iostream>

#include <MuscleCPP.hpp>

using namespace muscle;
using namespace std;

/**
a native kernel which sends an array of double
\author MM
*/
int main(int argc, char **argv)
{
	double dataA[5];

	try
	{
		MUSCLE::Init();

		Entrance *entrance = MUSCLE::AddEntrance("data", 1, MUSCLE_Double_Type);

		cout<<"c++: begin "<<__FILE__<<endl;

		for(int time = 0; !MUSCLE::WillStop(); time ++) {
								
			// process data
			for(int i = 0; i < 5; i++) {
				dataA[i] = i;
			}
						
			// dump to our portals
			entrance->Send(dataA, 5);
		}

		MUSCLE::Cleanup();

	}
	catch(...)
	{
		std::cerr << "Error occured" << std::endl;
	}
}
