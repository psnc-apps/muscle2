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
#include <assert.h>

#ifdef __cplusplus
extern "C" {
#endif


#define TAG 1

static const char *ringName = NULL;
static int rank = -1, size = -1;
static double deltaE;
static double energyThreshold;


void Ring_Init(const char *_ringName, double _deltaE, double _energyThreshold)
{
  ringName = _ringName;

  MPI_Init (NULL, NULL);	

  MPI_Comm_rank (MPI_COMM_WORLD, &rank);	
  MPI_Comm_size (MPI_COMM_WORLD, &size);

  deltaE = _deltaE;
  energyThreshold = _energyThreshold;

  printf("Initialized %d node in ring %s (DE=%f,MAXE=%f)\n", rank, ringName, deltaE, energyThreshold);
}

/* To be used by rank 0 process */
double insertProton(double initialEnergy,  void (*energy_callback)(double protonE))
{
	MPI_Status status;
	double protonE;
	
	assert(rank == 0);
	assert(size >= 2);

	protonE = initialEnergy;

	MPI_Send(&protonE, 1, MPI_DOUBLE, 1, TAG, MPI_COMM_WORLD);

	while (protonE < energyThreshold)
	{
		MPI_Recv(&protonE, 1, MPI_DOUBLE, size -1, TAG, MPI_COMM_WORLD, &status);
		
		energy_callback(protonE);
		
		MPI_Send(&protonE, 1, MPI_DOUBLE, 1, TAG, MPI_COMM_WORLD);
	}

	return protonE;
}

int isMasterNode()
{
	return (rank == 0);
}

/* To be used by other processes */
void accelerateProtons()
{
	MPI_Status status;
	int next = (rank + 1) % size;
  	int from = (rank + size - 1) % size;
	double protonE;
	
	assert(rank != 0);

	while (protonE < energyThreshold) 
	{
		MPI_Recv(&protonE, 1, MPI_DOUBLE, from, TAG, MPI_COMM_WORLD, &status);
		
		protonE+=deltaE;

		MPI_Send(&protonE, 1, MPI_DOUBLE, next, TAG, MPI_COMM_WORLD);
	}
}

void Ring_Cleanup()
{
  MPI_Finalize();
}

/* test code */
#define DELTA_E 0.1
#define INITIAL_E 1.4
#define MAX_E_THRESHOLD 4.8

static void energy_callback(double energy)
{
	static int loop = 0;

	printf("Energy in loop %d: %f\n", ++loop, energy);
}

int main(int argc, char **argv)
{
	Ring_Init("LHC", DELTA_E, MAX_E_THRESHOLD);

	if (isMasterNode())
		insertProton(INITIAL_E, energy_callback);
	else
		accelerateProtons();

	Ring_Cleanup();
	return 0;
}

#ifdef __cplusplus
}
#endif
