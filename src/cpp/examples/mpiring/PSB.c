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
		int will_stop = 0;

		MUSCLE_Init(&argc, &argv); /* MUSCLE calls are only permitted in the rank 0 process */

		will_stop = MUSCLE_Will_Stop();
		energy = atof(MUSCLE_Get_Property("PSB:InitialEnergy"));
		deltaEnergy = atof(MUSCLE_Get_Property("PSB:DeltaEnergy"));
		maxEnergy = atof(MUSCLE_Get_Property("PSB:MaxEnergy"));
		
		while (!will_stop) {
			/* wrapper over MPI_Bcast */
			Ring_Broadcast_Params(&deltaEnergy, &maxEnergy, &will_stop);

			printf("PSB: Inserting proton into Proton Synchrotron Booster (PSB). Initial energy: %f\n", energy);
			energy = insertProton(energy, maxEnergy, energy_callback);

			printf("Proton energy after PSB:  %f. Injecting into LHC.\n", energy);
			MUSCLE_Send("pipe-out", &energy, 1, MUSCLE_DOUBLE);
			will_stop = MUSCLE_Will_Stop();
		}
		Ring_Broadcast_Params(&deltaEnergy, &maxEnergy, &will_stop);

    	MUSCLE_Finalize();
	}
	else
	{
		double deltaEnergy = -1, maxEnergy = -1;
		int will_stop = 0;

		while (1) {
			/* wrapper over MPI_Bcast */
			Ring_Broadcast_Params(&deltaEnergy, &maxEnergy, &will_stop);
			if (will_stop) break;

			accelerateProtons(deltaEnergy, maxEnergy);
		}
	}

	Ring_Cleanup();
	return 0;
}

