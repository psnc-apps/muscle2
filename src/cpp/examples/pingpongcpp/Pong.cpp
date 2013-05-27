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
#include <cstring>
#include <stdio.h>
#include <stdlib.h>
#include <muscle2/cppmuscle.hpp>
#include <exception>

using namespace muscle;
using namespace std;

void do_computation(const int runs, const int steps)
{
	void *data = (void *)0;
	size_t count;

	for (int test = 0; test < runs; test++)
	{
		for (int i = 0; i < steps; i++)
		{
			data = env::receive("in", data, count, MUSCLE_RAW);
			env::send("out", data, count, MUSCLE_RAW);
		}
	}
    
    env::free_data(data, MUSCLE_RAW);
}

/**
a native kernel which sends an array of double
\author JB
*/
int main(int argc, char **argv)
{
	try
	{
		env::init(&argc, &argv);
		// How many steps for a single test
		const int steps = atoi(cxa::get_property("steps").c_str());

		// How many steps total will be done
		const int runs = atoi(cxa::get_property("same_size_runs").c_str());
		
		// Number of steps to warm up the system
		const int prepare_steps = atoi(cxa::get_property("preparation_steps").c_str());

		const int tests_count = atoi(cxa::get_property("tests_count").c_str());
		
		// helper with results for a single test
		size_t preparation_size;
		void *data = (void *)0;

		// Making noise in order to give time for JVM to stabilize
		for (int i = 0; i < prepare_steps; i++)
		{
			data = env::receive("in", data, preparation_size, MUSCLE_RAW);
			env::send("out", data, preparation_size, MUSCLE_RAW);
		}
		cout << endl;
		env::free_data(data, MUSCLE_RAW);
		
		for (int i = 0; i < tests_count; i++)
		{
			do_computation(runs, steps);
		}
		env::finalize();
	}
	catch(exception& ex)
	{
		logger::severe("Error occurred in Ping: %s", ex.what());
	}
	catch(...)
	{
		cerr << "Error occured in Pong" << endl;
	}
	
	return 0;
}
