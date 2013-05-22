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

#include <cstring>
#include <muscle2/cppmuscle.hpp>
#include <stdexcept>

using namespace std;
using namespace muscle;

/**
a native kernel that simply returns any data that is sent to it.
\author JB
*/
int main(int argc, char **argv)
{
	try
	{
		int datatype_i;
		size_t sz;
		
		env::init(&argc, &argv);
		
		while (true) {
			muscle::env::receive("datatype", &datatype_i, sz, MUSCLE_INT32);
			if (datatype_i == -1) {
				break;
			}
			muscle_datatype_t muscle_t = (muscle_datatype_t)datatype_i;
			void *data = muscle::env::receive("in", (void *)0, sz, muscle_t);
			muscle::env::send("out", data, sz, muscle_t);
			muscle::env::free_data(data, muscle_t);
		}
		
		env::finalize();
	}
	catch(exception& ex)
	{
		logger::severe("Error occurred in Bounce: %s", ex.what());
		env::finalize();
		return 1;
	}
	catch(...)
	{
		logger::severe("Error occured in Bounce");
		env::finalize();
		return 1;
	}
	
	return 0;
}
