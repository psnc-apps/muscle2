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
#include <vector>

#include <cppmuscle.hpp>
#include <muscle_complex_data.hpp>

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

		muscle::env::init(&argc, &argv);

		cout << "kernel name" << muscle::cxa::kernel_name() << endl;
		cout << "kernel tmp path: " << muscle::env::get_tmp_path() << endl;
		cout << "CxA property 'max_timesteps': " << muscle::cxa::get_property("max_timesteps") << endl;
		cout << "CxA properties:\n" << muscle::cxa::get_properties() <<endl;

		for(int i = 0; !muscle::env::will_stop(); i++)
		{
			cout << "t: " << i << " " << endl;
			/* initialize data */
			size_t size = 1024;
			size_t size2 = size*size;
			char *data = (char *)malloc(size*size*sizeof(char));
			vector<int> dims(2);
			dims[0] = size;
			dims[1] = size;

			ComplexData cdata(data, COMPLEX_BYTE_MATRIX_2D, &dims);

			for (int j=0; j < size; j++) {
				for (int k=0; k < size; k++) {
					data[cdata.fidx(j, k)] = 15;
				}
			}

			data[cdata.fidx(0,0)] = 65;
			data[cdata.fidx(size-1, size-1)] = 77;

			/* send */
			muscle::env::send("writer", &cdata, size2, MUSCLE_COMPLEX);

			/* receive */
			ComplexData *cdata2 = (ComplexData *)muscle::env::receive("reader", (void *)0, size2, MUSCLE_COMPLEX);
			char *data2 = (char *)(cdata2->getData());
			logger::info("data in c++ : %d %d", data2[cdata2->index(0,0)], data2[cdata2->index(size-1,size-1)]);

			// char **data is freed by destructor of ComplexData
			muscle::env::free_data(cdata2, MUSCLE_COMPLEX); /* we must use muscle:free because the array was allocated by muscle::receive() */
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
	
	return 0;
}



