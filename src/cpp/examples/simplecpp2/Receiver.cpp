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
a native kernel which receives and logs an array of double
\author JB
*/
int main(int argc, char **argv)
{
	size_t len;
	double *data;

	try
	{
		cout << "c++: before init " <<endl;
		muscle::env::init(&argc, &argv);

		cout << "c++: begin "<< argv[0] <<endl;
		cout << "Kernel Name: " << muscle::cxa::kernel_name() << endl;

		for(int time = 0; muscle::env::has_next("data"); time ++) {
			data = (double *)muscle::env::receive("data", (void *)0, len, MUSCLE_DOUBLE);
			// process data
			muscle::logger::info("Length: %zu; min: %f; max: %f", len, data[0], data[len - 1]);
		}

		muscle::env::finalize();
	}
	catch(muscle_exception& ex)
	{
		logger::severe("Error occurred in receiver: %s", ex.what());
	}
	catch(...)
	{
		cerr << "Error occured" << endl;
	}
	
	return 0;
}
