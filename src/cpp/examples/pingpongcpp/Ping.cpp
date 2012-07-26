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

double time_avg(const long total, const int runs, const int steps)
{
    return total/(double)(steps*runs);
}
double stddev(const long *times, const double avg, const int runs, const int steps)
{
    double dev = 0.0;
    double dsteps = steps;
    
    for (int i = 0; i < runs; i++) {
        dev += (times[i]/dsteps - avg)*(times[i]/dsteps - avg);
    }
    return sqrt(dev/(double)(runs - 1));
}

long usec_diff(struct timeval *tpStart, struct timeval *tpEnd)
{
    return (1000000l*(long)tpEnd->tv_sec + (long)tpEnd->tv_usec) -
           (1000000l*(long)tpStart->tv_sec + (long)tpStart->tv_usec);
}

void print_stats(const int printdesc, const long *times, const long totaltime, const int size_kib, const int runs, const int steps)
{
    double avg = time_avg(totaltime, runs, steps);
    double s = stddev(times, avg, runs, steps);

    if (printdesc) {
        printf("|=%10s|=%10s|=%10s|=%10s|=%10s|=%10s|\n","Size (kiB)","Total (ms)","Avg (us)","StdDev(us)","StdDev(%)","Speed (MB/s)");
    }
    double speed = size_kib*2*1024 /avg;
    printf("| %10d| %10d| %10.0f| %10.0f| %10.1f| %10.1f|\n", size_kib, (int)(totaltime/1000), avg, s, 100*s/avg, speed);
	fflush(stdout);
}

long do_computation(long* times, const int size, const int runs, const int steps)
{
    struct timeval tpStart, tpEnd;
    size_t count = size*128; // 1024/sizeof(double)
    void *buf = malloc(count*sizeof(double));
    long total = 0;

    for (int test = 0; test < runs; test++) {
        gettimeofday(&tpStart, NULL);
        
        for (int i = 0; i < steps; i++) {
            env::send("out", buf, count, MUSCLE_DOUBLE);
            env::receive("in", buf, count, MUSCLE_DOUBLE);
        }
        
        gettimeofday(&tpEnd, NULL);
        total += times[test] = usec_diff(&tpStart, &tpEnd);
    }
    
    free(buf);
    return total;
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
		const int max_timesteps = atoi(cxa::get_property("max_timesteps").c_str()) + 1;
		
		// How many steps total will be done
		const int runs = atoi(cxa::get_property("same_size_runs").c_str());
		
		// size in B
		int size = atoi(cxa::get_property("start_kiB_per_message").c_str());
		
		// Number of steps to warm up the system
		const int prepare_steps = atoi(cxa::get_property("preparation_steps").c_str());

		const int tests_count = atoi(cxa::get_property("tests_count").c_str());
		
		// helper with results for a single test
		long *totalTimes = new long[runs];
		size_t preparation_size = 1024/8;
		double *data = (double *)malloc(preparation_size*sizeof(double));

		// Making noise in order to give time for JVM to stabilize
		cout << "Preparing";
		for (int i = 0; i < prepare_steps; i++)
		{
			env::send("out", data, preparation_size, MUSCLE_DOUBLE);
			env::receive("in", data, preparation_size, MUSCLE_DOUBLE);
			if (i % 5 == 0)
				cout << ".";
		}
		free(data);
		cout << "\n\nValues are NOT divided by 2. Each value is calculated for RTT." << endl;
		printf("Sending %d messages in total. For each data size, %d tests are performed, each sending %d messages.\n\n", max_timesteps, runs, steps);	
		
		for (int i = 0; i < tests_count; i++) {
			long sum = do_computation(totalTimes, size, runs, steps);
			
			print_stats(i==0, totalTimes, sum, size, runs, steps);
			if (size == 0) {
				size = 1;
			} else if (size < INT_MAX / 2) {
				size *= 2;
			} else {
				break;
			}
		}
		env::finalize();
		delete [] totalTimes;
	}
	catch(exception& ex)
	{
		logger::severe("Error occurred in Ping: %s", ex.what());
	}
	catch(...)
	{
		cerr << "Error occured in Ping" << endl;
	}
	
	return 0;
}
