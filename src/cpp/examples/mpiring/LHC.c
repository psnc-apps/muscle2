#include <stdio.h>
#include <stdlib.h>

#include <cmuscle.h>

#include "mpiringlib.h"

static void energy_callback(double energy)
{
        static int loop = 0;

        printf("LHC:Energy in loop %d: %f\n", ++loop, energy);
}


int main(int argc, char **argv)
{

	Ring_Init("LHC");

	if (isMasterNode())
	{
		double energy = - 1, deltaEnergy = -1, maxEnergy = -1;
		size_t count = 1;

		MUSCLE_Init(&argc, &argv); /* MUSCLE calls are only permitted in the rank 0 process */

		deltaEnergy = atof(MUSCLE_Get_Property("LHC:DeltaEnergy"));
		maxEnergy = atof(MUSCLE_Get_Property("LHC:MaxEnergy"));

		/* wrapper over MPI_Bcast */
		Ring_Broadcast_Params(&deltaEnergy, &maxEnergy);

		MUSCLE_Receive("pipe-in", &energy, &count, MUSCLE_DOUBLE);

		printf("LHC:Received proton energy: %f\n", energy);

		energy = insertProton(energy, maxEnergy, energy_callback);

		printf("LHC:Final proton energy: %f\n", energy);

		MUSCLE_Finalize();

	} else
	{
		double deltaEnergy = -1, maxEnergy = -1;

		/* wrapper over MPI_Bcast */
		Ring_Broadcast_Params(&deltaEnergy, &maxEnergy);

		accelerateProtons(deltaEnergy, maxEnergy);
	}

	Ring_Cleanup();

}

