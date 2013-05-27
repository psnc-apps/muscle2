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
