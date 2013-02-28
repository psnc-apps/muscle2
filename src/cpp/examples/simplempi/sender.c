#include <stdlib.h>
#include <muscle2/cmuscle.h>
#include <mpi.h>

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
	
	// We will communicate in root only, so allocate memory for that
	if (rank == 0) {
		MPI_Comm_size(MPI_COMM_WORLD, &mpi_size);
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
		}
		MPI_Bcast(&will_stop, 1, MPI_INT, 0, MPI_COMM_WORLD);
	}
	
	// Free allocated memory
	if (rank == 0) {
		free(data);
	}
	
	// Finalize the frameworks in reverse order of initializing
	MPI_Finalize();
	MUSCLE_Finalize();
	return 0;
}
