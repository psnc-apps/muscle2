#include <mpi.h>
#include <stdio.h>
#include <assert.h>

#include <muscle2/cmuscle.h>

#ifdef __cplusplus
extern "C" {
#endif


#define TAG 1

static const char *ringName = NULL;
static int rank = -1, size = -1;


void Ring_Init(const char *_ringName)
{
  ringName = _ringName;

  MPI_Init (NULL, NULL);

  MPI_Comm_rank (MPI_COMM_WORLD, &rank);
  MPI_Comm_size (MPI_COMM_WORLD, &size);

  printf("Initialized %d node in ring %s.\n", rank, ringName);
}

void Ring_Broadcast_Params(double *deltaE, double *maxE, int *will_stop)
{
	MPI_Bcast(deltaE, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	MPI_Bcast(maxE, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
	MPI_Bcast(will_stop, 1, MPI_DOUBLE, 0, MPI_COMM_WORLD);
}


/* To be used by rank 0 process */
double insertProton(double initialEnergy, double maxEnergy,  void (*energy_callback)(double protonE))
{
	MPI_Status status;
	double protonE;
	
	assert(rank == 0);
	assert(size >= 2);

	protonE = initialEnergy;

	MPI_Send(&protonE, 1, MPI_DOUBLE, 1, TAG, MPI_COMM_WORLD);

	while (protonE < maxEnergy)
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
void accelerateProtons(double deltaE, double maxEnergy)
{
	MPI_Status status;
	int next = (rank + 1) % size;
  	int from = (rank + size - 1) % size;
	double protonE = 0;
	
	assert(rank != 0);

	while (protonE < maxEnergy)
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

#ifdef TEST
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
	MUSCLE_Init();

	Ring_Init(MUSCLE_Kernel_Name(), atof(MUSCLE_Get_Property("DeltaEnergy")), atof(MUSCLE_Get_Property("MaxEnergy")));

	if (isMasterNode())
		insertProton(atof(MUSCLE_Get_Property("InitialEnergy")), energy_callback);
	else
		accelerateProtons();

	Ring_Cleanup();

	MUSCLE_Finalize();
}
#endif

#ifdef __cplusplus
}
#endif
