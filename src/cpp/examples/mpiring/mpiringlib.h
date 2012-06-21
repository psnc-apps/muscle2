#ifndef MPIRINGLIB_H
#define MPIRINGLIB_H

#ifdef __cplusplus
extern "C" {
#endif

void Ring_Init(const char *ringName);

void Ring_Broadcast_Params(double *deltaE, double *maxE, int *will_stop);

/* To be used by rank 0 process */
double insertProton(double initialEnergy, double maxEnergy, void (*energy_callback)(double protonE));

int isMasterNode();

/* To be used by other processes */
void accelerateProtons(double deltaEnergy, double maxEnergy);
void Ring_Cleanup(void);

#ifdef __cplusplus
}
#endif

#endif
