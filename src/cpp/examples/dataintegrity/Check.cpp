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
#include <fstream>
#include <climits>
#include <cstring>
#include <stdio.h>
#include <cmath>
#include <vector>
#include <stdlib.h>
#include <cppmuscle.hpp>
#include <stdexcept>

using namespace muscle;
using namespace std;

union udouble {
  double d;
  unsigned long u;
};
union ufloat {
  double f;
  unsigned u;
};

vector<double> readDoubleFile(string filename) {
        ifstream infile(filename.c_str());
        vector <double> ret;
        
        while (infile.good()) {
                string s;
                getline(infile, s, ' ');
                double d = atof(s.c_str());
                if (std::isnan(d)) logger::severe("Found a nan: %s", s.c_str());
                ret.push_back(atof(s.c_str()));
        }
        if (!infile.eof()) {
                logger::severe("Could not read file");
                throw runtime_error("no file");
        }
        // Removing last zero value, from reading stream
        ret.pop_back();
        
        return ret;
}

bool do_test(const unsigned char *data, const muscle_datatype_t type, const size_t d_sz, const size_t sz)
{
	const int datatype_i = type;
	size_t msg_sz = 1;
	
	muscle::env::send("datatype", &datatype_i, msg_sz, MUSCLE_INT32);
	muscle::env::send("out", data, sz, type);
	unsigned char *newdata = (unsigned char *)muscle::env::receive("in", (void *)0, msg_sz, type);
	
	bool matches = true;
	if (sz != msg_sz) {
		if (type == MUSCLE_STRING)
			logger::severe("Size %u of sent data '%s' with sizeof %u is not equal to size %u of received data '%s'", sz, data, d_sz, msg_sz, newdata);
		else
			logger::severe("Size %u of sent data %d with sizeof %u is not equal to size %u of received data", sz, datatype_i, d_sz, msg_sz);
		matches = false;	
	}
	if (matches)
	{
		if (type == MUSCLE_FLOAT)
		{
			float *fdata = (float *)data;
			float *fnewdata = (float *)newdata;
			bool isn;
			for (int i = 0; i < sz; i++)
			{
				isn = isnan(fdata[i]);
				if (isn != isnan(fnewdata[i]) || (!isn && fdata[i] != fnewdata[i]))
				{
					ufloat uf, unf;
					uf.f = fdata[i];
					unf.f = fnewdata[i];
					logger::fine("%d:  from %x to %x", i, uf.u, unf.u);
					matches = false;
				}
			}
			if (!matches) {
				logger::severe("Float data sent with size %u does not match what is received (taking into account multiple representations of NaN).", sz);
			}
		} else if (type == MUSCLE_DOUBLE)
		{
			double *ddata = (double *)data;
			double *dnewdata = (double *)newdata;

			for (int i = 0; i < sz; i++)
			{
				bool isn = isnan(ddata[i]);
				if (isn != isnan(dnewdata[i]) || (!isn && ddata[i] != dnewdata[i]))
				{
					udouble ud, und;
					ud.d = ddata[i];
					und.d = dnewdata[i];
					logger::fine("%d:  from %lx to %lx", i, ud.u, und.u);
					matches = false;
				}
			}
			if (!matches) {
				logger::severe("Double data sent with size %u does not match what is received (taking into account multiple representations of NaN).", sz);
			}
		}
		else if (memcmp(data, newdata, sz*d_sz) != 0)
		{
			for (int i = 0; i < sz; i += d_sz) {
				unsigned long value = 0, newvalue = 0;
				for (int j = 0; j < d_sz; j++) {
					value = (value << 8) | data[i+j];
					newvalue = (newvalue << 8) | newdata[i+j];
				}
				if (newvalue != value) {
					logger::fine("%d:   from %lx to %lx", i, value, newvalue);
				}
			}

			matches = false;
		}
	}
	
	muscle::env::free_data(newdata, type);
	
	return matches;
}


bool do_full_test(const muscle_datatype_t type, const size_t d_sz, const size_t sz)
{
	unsigned char *c_data = (unsigned char *)0;
	const unsigned char *data;
	size_t send_size = sz;
	if (type == MUSCLE_BOOLEAN)
	{
		bool *arr = (bool *)malloc(sz);
		for (int i = 0; i < sz; i++) {
			arr[i] = (random() & 01) == 01;
		}
		c_data = (unsigned char *)arr;
	}
	else
	{
		if (type == MUSCLE_STRING)
		{
			if (sz == 0)
			{
				data = (const unsigned char *)"";
				send_size = 1;
			}
			else if (sz >= 64*1024) return true;
			else
			{
				c_data = (unsigned char *)malloc(send_size);
				// With a string, null terminate and don't send null characters
				for (int i = 0; i < send_size - 1; i++) {
					c_data[i] = 1 + (((unsigned int)random())%254);
				}
				c_data[send_size - 1] = 0;
			}
		}
		else
		{
			const size_t l_sz = sz*d_sz / sizeof(long);
			send_size = l_sz*sizeof(long)/d_sz;
			unsigned long *l_data = (unsigned long *)malloc(l_sz*sizeof(long));
			for (int i = 0; i < l_sz; i++) {
				// random returns one bit too little, so we need three randoms
				l_data[i] = (((unsigned long)random()) << 62) | (((unsigned long)random()) << 31) | ((unsigned long)random());
			}
			c_data = (unsigned char *)l_data;
		}
	}
	// Excludes empty string
	if (c_data) {
		data = (const unsigned char *)c_data;
	}
	bool matches = do_test(data, type, d_sz, send_size);
	
	// Excludes empty string
	if (c_data) {
		free(c_data);
	}
	
	return matches;
}

bool do_test_suite(const char *name, const muscle_datatype_t type, const size_t d_sz)
{
	const int test_sizes[] = {0, 1, 8, 50, 1024, 1024*64, 1024*1024};
	const int num_tests = 7;
	bool succeed = true;
	
	logger::info("Testing %s", name);
	for (int i = 0; i < num_tests; i++) {
		succeed = do_full_test(type, d_sz, test_sizes[i]) && succeed;
	}
	logger::info("Testing %s %s", name, succeed ? "succeeded" : "failed");
	return succeed;
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
		
		bool all_succeed = true;

		for (int seed = 0; seed < 5; seed++) {
			srandom(seed);

			all_succeed = do_test_suite("MUSCLE_BOOLEAN", MUSCLE_BOOLEAN, 1) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_RAW", MUSCLE_RAW, 1) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_INT32", MUSCLE_INT32, 4) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_FLOAT", MUSCLE_FLOAT, 4) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_DOUBLE", MUSCLE_DOUBLE, 8) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_INT64", MUSCLE_INT64, 8) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_STRING", MUSCLE_STRING, 1) && all_succeed;
		}
		
		
		// Stop Bounce
		int datatype = -1;
		muscle::env::send("datatype", &datatype, 1, MUSCLE_INT32);
		
		if (all_succeed) {
			logger::info("tests passed");
		}
		else {
			logger::severe("some tests failed");
		}

		env::finalize();
	}
	catch(exception& ex)
	{
		logger::severe("Error occurred in Check: %s", ex.what());
		env::finalize();
		return 1;
	}
	catch(...)
	{
		logger::severe("Error occured in Check");
		env::finalize();
		return 2;
	}
	
	return 0;
}
