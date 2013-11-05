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
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h> // usleep
#include <muscle2/cmuscle.h>

int main(int argc, char *argv[])
{
	// Initialize MUSCLE first, then MPI. If your MPI implementation complains and fails, try the other
	// way around.
	MUSCLE_Init(&argc, &argv);
	MPI_Init(&argc, &argv);
	
	// Data that will be sent from MUSCLE
	double *data;
	
	int rank, mpi_size, will_stop;
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	MPI_Comm_size(MPI_COMM_WORLD, &mpi_size);
	
	char *barrier;
	size_t len;
	int result = MUSCLE_Barrier_Init(&barrier, &len, mpi_size);
	MPI_Bcast(&result, 1, MPI_INT, 0, MPI_COMM_WORLD);
	
	if (result == -1) {
		MPI_Finalize();
		MUSCLE_Finalize();
		return 1;
	}

	MPI_Bcast(barrier, len, MPI_CHAR, 0, MPI_COMM_WORLD);
	
	// We will communicate in root only, so allocate memory for that
	if (rank == 0) {
		data = (double *)malloc(mpi_size * sizeof(double));
		will_stop = MUSCLE_Will_Stop();
	}
	
	// Check that will_stop does not start false
	MPI_Bcast(&will_stop, 1, MPI_INT, 0, MPI_COMM_WORLD);
	
	double i = rank;
	while (!will_stop) {
		// Gather the data, being the ranks, plus the iteration number (i).
		MPI_Gather(&i, 1, MPI_DOUBLE, data, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
		i++;
		
		if (rank == 0) {
			// Send the data
			MUSCLE_Send("data", data, mpi_size, MUSCLE_DOUBLE);
			// After sending, check and broadcast the will_stop condition again
			will_stop = MUSCLE_Will_Stop();
			usleep(500000);
		}
		MUSCLE_Barrier(barrier);
		MPI_Bcast(&will_stop, 1, MPI_INT, 0, MPI_COMM_WORLD);
	}
	
	// Free allocated memory
	if (rank == 0) {
		free(data);
	}
	
	// Finalize the frameworks in reverse order of initializing
	MPI_Finalize();
	MUSCLE_Barrier_Destroy(barrier);
	MUSCLE_Finalize();
	return 0;
}
