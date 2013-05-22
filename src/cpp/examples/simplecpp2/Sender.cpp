/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
*
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
*
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <iostream>

#include <muscle2/cppmuscle.hpp>

using namespace muscle;
using namespace std;

/**
a native kernel which sends an array of double
\author MM
*/
int main(int argc, char **argv)
{
	int len = 64*1024;
	double dataA[len];

	try
	{
		muscle::env::init(&argc, &argv);

		cout << "c++: begin "<< argv[0] <<endl;
		cout << "Kernel Name: " << muscle::cxa::kernel_name() << endl;

		for(int time = 0; !muscle::env::will_stop(); time ++) {
								
			// process data
			for(int i = 0; i < len; i++) {
				dataA[i] = i;
			}
						
			// dump to our portals
			muscle::env::send("data", dataA, len, MUSCLE_DOUBLE);
		}

		muscle::env::finalize();
	}
	catch(exception& ex)
	{
		logger::severe("Error occurred in sender: %s", ex.what());
	}
	catch(...)
	{
		cerr << "Error occured" << endl;
	}
	
	return 0;
}
