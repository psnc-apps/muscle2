#include <stdio.h>
#include <stdlib.h>

#include <cmuscle.h>

#include "mpiringlib.h"

static void energy_callback(double energy)
{
        static int loop = 0;

        printf("PSB:Energy in loop %d: %f\n", ++loop, energy);
}


int main(int argc, char **argv)
{

	Ring_Init("PSB");

	if (isMasterNode())
	{
		double energy = -1, deltaEnergy = -1, maxEnergy = -1;

		MUSCLE_Init(); /* MUSCLE calls are only permitted in the rank 0 process */

		energy = atof(MUSCLE_Get_Property("PSB:InitialEnergy"));
		deltaEnergy = atof(MUSCLE_Get_Property("PSB:DeltaEnergy"));
		maxEnergy = atof(MUSCLE_Get_Property("PSB:MaxEnergy"));

		/* wrapper over MPI_Bcast */
		Ring_Broadcast_Params(&deltaEnergy, &maxEnergy);

    	printf("PSB: Inserting proton into Proton Synchrotron Booster (PSB). Initial energy: %f\n", energy);
		energy = insertProton(energy, maxEnergy, energy_callback);

    	printf("Proton energy after PSB:  %f. Injecting into LHC.\n", energy);
    	MUSCLE_Send("pipe-out", &energy, 1, MUSCLE_DOUBLE);

    	MUSCLE_Finalize();
	}
	else
	{
		double deltaEnergy = -1, maxEnergy = -1;

		/* wrapper over MPI_Bcast */
		Ring_Broadcast_Params(&deltaEnergy, &maxEnergy);

		accelerateProtons(deltaEnergy, maxEnergy);
	}

	Ring_Cleanup();
}

