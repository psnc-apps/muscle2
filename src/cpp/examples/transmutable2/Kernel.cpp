/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of muscle (Multiscale Coupling Library and Environment).

    muscle is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    muscle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with muscle.  If not, see <http://www.gnu.org/licenses/>.
*/
#include <exception>
#include <iostream>

#include <cppmuscle.hpp>

using namespace muscle;
using namespace std;

/**
a new muscle CPP API native kernel which sends and receives an array of double
\author Mariusz Mamonski
based o
*/
int main(int argc, char **argv)
{
	try
	{
		cout << "c++: begin " << argv[0] <<endl;

		muscle::env::init();

		cout << "kernel tmp path: " << muscle::env::get_tmp_path() << endl;
		cout << "CxA property 'max_timesteps': " << muscle::cxa::get_property("max_timesteps") << endl;
		cout << "CxA properties:\n" << muscle::cxa::get_properties() <<endl;

		for(int i = 0; !muscle::env::will_stop(); i++)
		{
			cout << "t: " << i << " " << endl;
			/* initialize data */
			size_t size = 42;
			double *data = new double[size];
			for (int j=1; j < size; j++)
				data[j] = 0;

			data[0] = 0.42;
			data[size-1] = 4200.99;

			/* send */
			muscle::env::send("writer", data, size, MUSCLE_DOUBLE);

			/* receive */
			double *data2 = (double *)muscle::env::receive("reader", (void *)0, size, MUSCLE_DOUBLE);

			cout << "data in c++ : " << data2[0] << " " << data2[size-1] << endl;

			delete [] data;
			muscle::env::free_data(data2, MUSCLE_DOUBLE); /* we must use muscle:free because the array was allocated by muscle::receive() */
		}


		muscle::logger::warning("my warning message");
		muscle::logger::info("my info message");
		muscle::logger::fine("my fine message");

		muscle::env::finalize();

	}
	catch(exception &e)
	{
		cerr << "Exception: " << e.what() << endl;
	}
	catch(...)
	{
		cerr << "Unknown error" << endl;
	}
}



