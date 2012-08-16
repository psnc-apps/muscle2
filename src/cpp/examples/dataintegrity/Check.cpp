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
#include <climits>
#include <cstring>
#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <sys/time.h>
#include <cppmuscle.hpp>
#include <exception>

using namespace muscle;
using namespace std;

bool do_test(const char *data, const muscle_datatype_t type, const size_t d_sz, const size_t sz)
{
	const int datatype_i = type;
	size_t msg_sz = 1;
	
	muscle::env::send("datatype", &datatype_i, msg_sz, MUSCLE_INT32);
	muscle::env::send("out", data, sz, type);
	char *newdata = (char *)muscle::env::receive("in", (void *)0, msg_sz, type);
	
	bool matches = true;
	if (sz != msg_sz) {
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
					matches = false;
					break;
				}
			}
			if (!matches) {
				logger::severe("Float data sent with size %u does not match what is received (taking into account multiple representations of NaN).", datatype_i, sz);
			}
		} else if (type == MUSCLE_DOUBLE)
		{
			double *ddata = (double *)data;
			double *dnewdata = (double *)newdata;
			bool isn;
			for (int i = 0; i < sz; i++)
			{
				isn = isnan(ddata[i]);
				if (isn != isnan(dnewdata[i]) || (!isn && ddata[i] != dnewdata[i]))
				{
					matches = false;
					break;
				}
			}
			if (!matches) {
				logger::severe("Double data sent with size %u does not match what is received (taking into account multiple representations of NaN).", datatype_i, sz);
			}
		}
		else if (memcmp(data, newdata, sz*d_sz) != 0)
		{

			for (int i = 0; i < sz; i += d_sz) {
				unsigned long value = 0, newvalue = 0;
				for (int j = 0; j < d_sz; j++) {
					value = (value << 8) + data[i+j];
					newvalue = (newvalue << 8) + newdata[i+j];
				}
				if (newvalue != value) {
					logger::fine(" from %x to %x", value, newvalue);
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
	const size_t l_sz = sz*d_sz / sizeof(long);
	long *data = (long *)malloc(l_sz*sizeof(long));
	for (int i = 0; i < l_sz; i++) {
		data[i] = random();
	}
	bool matches = do_test((const char *)data, type, d_sz, l_sz*sizeof(long)/d_sz);
	
	free(data);
	
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

			{
				logger::info("Testing MUSCLE_BOOLEAN");
				const int test_sizes[] = {0, 1, 8, 50, 1024, 1024*64, 1024*1024};
				const int num_tests = 7;
				bool succeed = true;
				bool arr[1024*1024];
				for (int i = 0; i < 1024*1024; i++) {
					arr[i] = (random() & 01) == 01;
				}
				for (int i = 0; i < num_tests; i++) {
					succeed = do_test((const char *)arr, MUSCLE_BOOLEAN, 1, test_sizes[i]) && succeed;
				}
				all_succeed = succeed && all_succeed;
				logger::info("Testing MUSCLE_BOOLEAN %s", succeed ? "succeeded" : "failed");
			}


			all_succeed = do_test_suite("MUSCLE_RAW", MUSCLE_RAW, 1) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_INT32", MUSCLE_INT32, 4) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_FLOAT", MUSCLE_FLOAT, 4) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_DOUBLE", MUSCLE_DOUBLE, 8) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_INT64", MUSCLE_INT64, 8) && all_succeed;
			//all_succeed = do_test_suite("MUSCLE_STRING", MUSCLE_STRING, 1) && all_succeed;
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
	}
	catch(...)
	{
		logger::severe("Error occured in Check");
	}
	
	return 0;
}
