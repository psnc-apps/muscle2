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
//#include <fstream>
#include <climits>
#include <cstring>
#include <stdio.h>
#include <cmath>
#include <vector>
#include <stdlib.h>
#include <muscle2/cppmuscle.hpp>
#include <stdexcept>
#include <muscle2/complex_data.hpp>

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

void *sendRecv(const void *data, const muscle_datatype_t type, const size_t d_sz, const size_t sz)
{
	const int datatype_i = type;
	size_t msg_sz = 1;
	
	muscle::env::send("datatype", &datatype_i, msg_sz, MUSCLE_INT32);
	muscle::env::send("out", data, sz, type);
	void *newdata = muscle::env::receive("in", (void *)0, msg_sz, type);

	if (sz == msg_sz) {
		return newdata;
	} else
	{
		muscle::env::free_data(newdata, type);
		if (type == MUSCLE_STRING)
			logger::severe("Size %u of sent data '%s' with sizeof %u is not equal to size %u of received data '%s'", sz, data, d_sz, msg_sz, newdata);
		else
			logger::severe("Size %u of sent data %d with sizeof %u is not equal to size %u of received data", sz, datatype_i, d_sz, msg_sz);
		return (void *)0;
	}
}

bool dataMatches(const unsigned char *data, const unsigned char *newdata, const muscle_datatype_t type, const size_t d_sz, const size_t sz)
{
	bool matches = true;
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
		int misfits = 0;
		for (int i = 0; i < sz; i += d_sz) {
			unsigned long value = 0, newvalue = 0;
			for (int j = 0; j < d_sz; j++) {
				value = (value << 8) | data[i+j];
				newvalue = (newvalue << 8) | newdata[i+j];
			}
			if (newvalue != value) {
				misfits++;
//				logger::fine("%d:   from %lx to %lx", i, value, newvalue);
			}
		}

		logger::info("Failed %d/%u", misfits, sz);
		matches = false;
	}
		
	return matches;
}

void *generateRandom(const size_t sz) {
	size_t l_sz = sz/8;
	if (l_sz * 8 < sz) {
		l_sz++;
	}
	unsigned long *data = (unsigned long *)malloc(l_sz*8);
	
	for (int i = 0; i < l_sz; i++) {
		// random returns 31 bits, we need 64, so we'll use three calls to random.
		data[i] = (((unsigned long)random()) << 62) | (((unsigned long)random()) << 31) | ((unsigned long)random());
	}
	
	return data;
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
			c_data = (unsigned char *)generateRandom(sz*d_sz);
		}
	}
	// Excludes empty string
	if (c_data) {
		data = (const unsigned char *)c_data;
	}
	unsigned char *newdata = (unsigned char *)sendRecv(data, type, d_sz, send_size);
	bool matches = dataMatches(data, newdata, type, d_sz, send_size);
	muscle::env::free_data(newdata, type);
	
	// Excludes empty string
	if (c_data) {
		free(c_data);
	}
	
	return matches;
}

bool do_test_suite(const char *name, const muscle_datatype_t type, const size_t d_sz)
{
	const int test_sizes[] = {0, 1, 3, 8, 50, 1024, 1024*64, 1024*1024};
	const int num_tests = 8;
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
		const int seeds = atoi(cxa::get_property("num_seeds").c_str());
		
		bool all_succeed = true;

		for (int seed = 1; seed <= seeds; seed++) {
			srandom(seed);

			all_succeed = do_test_suite("MUSCLE_BOOLEAN", MUSCLE_BOOLEAN, 1) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_RAW", MUSCLE_RAW, 1) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_INT32", MUSCLE_INT32, 4) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_FLOAT", MUSCLE_FLOAT, 4) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_DOUBLE", MUSCLE_DOUBLE, 8) && all_succeed;
			all_succeed = do_test_suite("MUSCLE_INT64", MUSCLE_INT64, 8) && all_succeed;
		}
		
		{
			logger::info("Testing MUSCLE_STRING");
			std::string largeStr(64*1024,'.');
			const char *test_strs[] = {"", "some simple", "&!!9121234567';:><{}][@#$%^&*-=_+QWERTYUIOPASDFGHJKLZXCVBNM,.", largeStr.c_str()};
			bool success = true;
			for (int i = 0; i < 4; i++) {
				const char *str = test_strs[i];
				char *newstr = (char *)sendRecv(str, MUSCLE_STRING, 1, strlen(str)+1);
				success = dataMatches((const unsigned char *)str, (const unsigned char *)newstr, MUSCLE_STRING, 1, strlen(str)+1);
				muscle::env::free_data(newstr, MUSCLE_STRING);
			}
			logger::info("Testing MUSCLE_STRING %s", success ? "succeeded" : "failed");
			all_succeed = success && all_succeed;
		}
		
		{
			bool success = true;
			logger::info("Testing COMPLEX_DOUBLE_MATRIX_3D");
			 int i_dims[] = {15,30,21};
			vector<int> dims(i_dims, i_dims + sizeof(i_dims) / sizeof(int));
			size_t len = dims[0]*dims[1]*dims[2];
			
			double *data = (double *)generateRandom(len*8);
			ComplexData cdata(data, COMPLEX_DOUBLE_MATRIX_3D, &dims);
			ComplexData *cnewdata = (ComplexData *)sendRecv(&cdata, MUSCLE_COMPLEX, 8, len);
			
			double *olddata = (double *)cdata.getData();
			double *newdata = (double *)cnewdata->getData();
			
			int prevIdx = -1;
			for (int i = 0; i < dims[0] && success; i++) {
				for (int j = 0; j < dims[1] && success; j++) {
					for (int k = 0; k < dims[2] && success; k++) {
						int idx = cdata.fidx(i, j, k);
						if (idx == prevIdx + 1) {
							prevIdx++;
						} else {
							logger::severe("Index (%d, %d, %d) = %d does not follow previous index %d", i, j, k, idx, prevIdx);
							success = false;
						}
						if (idx != cnewdata->index(i, j, k)) {
							logger::severe("Index (%d, %d, %d) = %d of sent data does not match index %d of received data", i, j, k, idx, cnewdata->index(i, j, k));
							success = false;
						}
						bool isn = isnan(olddata[idx]);
						if (isn != isnan(newdata[idx]) || (!isn && olddata[idx] != newdata[idx])) {
							udouble ud, und;
							ud.d = olddata[i];
							und.d = newdata[i];
							logger::fine("%d:  from %lx to %lx", i, ud.u, und.u);
							success = false;
						}
					}
				}
			}
			muscle::env::free_data(cnewdata,MUSCLE_COMPLEX);
			logger::info("Testing COMPLEX_DOUBLE_MATRIX_3D %s", success ? "succeeded" : "failed");
			all_succeed = success && all_succeed;
		}
		{
			bool success = true;
			logger::info("Testing COMPLEX_INT_MATRIX_2D");
			 int i_dims[] = {15,30};
			vector<int> dims(i_dims, i_dims + sizeof(i_dims) / sizeof(int));
			size_t len = dims[0]*dims[1];
			
			int *data = (int *)generateRandom(len*sizeof(int));
			ComplexData cdata(data, COMPLEX_INT_MATRIX_2D, &dims);
			ComplexData *cnewdata = (ComplexData *)sendRecv(&cdata, MUSCLE_COMPLEX, 8, len);
			
			int *olddata = (int *)cdata.getData();
			int *newdata = (int *)cnewdata->getData();
			
			int prevIdx = -1;
			for (int i = 0; i < dims[0] && success; i++) {
				for (int j = 0; j < dims[1] && success; j++) {
					int idx = cdata.fidx(i, j);
					if (idx == prevIdx + 1) {
						prevIdx++;
					} else {
						logger::severe("Index (%d, %d) = %d does not follow previous index %d", i, j, idx, prevIdx);
						success = false;
					}
					if (idx != cnewdata->index(i, j)) {
						logger::severe("Index (%d, %d) = %d of sent data does not match index %d of received data", i, j, idx, cnewdata->index(i, j));
						success = false;
					}
					if (olddata[idx] != newdata[idx]) {
						logger::fine("%d:  from %d to %d", i, olddata[idx], newdata[idx]);
						success = false;
					}
				}
			}
			muscle::env::free_data(cnewdata,MUSCLE_COMPLEX);
			logger::info("Testing COMPLEX_INT_MATRIX_2D %s", success ? "succeeded" : "failed");
			all_succeed = success && all_succeed;
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


//vector<double> readDoubleFile(string filename) {
//        ifstream infile(filename.c_str());
//        vector <double> ret;
//        
//        while (infile.good()) {
//                string s;
//                getline(infile, s, ' ');
//                double d = atof(s.c_str());
//                if (std::isnan(d)) logger::severe("Found a nan: %s", s.c_str());
//                ret.push_back(atof(s.c_str()));
//        }
//        if (!infile.eof()) {
//                logger::severe("Could not read file");
//                throw runtime_error("no file");
//        }
//        // Removing last zero value, from reading stream
//        ret.pop_back();
//        
//        return ret;
//}
