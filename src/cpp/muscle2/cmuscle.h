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
#ifndef CMUSCLE_H
#define CMUSCLE_H

#include "muscle_types.h"

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

muscle_error_t MUSCLE_Init(int* argc, char*** argv);
void MUSCLE_Finalize(void);

const char* MUSCLE_Kernel_Name(void);
const char* MUSCLE_Get_Property(const char* name);
int MUSCLE_Has_Property(const char* name);
int MUSCLE_Will_Stop(void);
int MUSCLE_Barrier_Init(char **barrier, size_t *len, int num_procs);
int MUSCLE_Barrier(const char *barrier);
void MUSCLE_Barrier_Destroy(char *barrier);

muscle_error_t MUSCLE_Send(const char *exit_name, void *array, size_t size, muscle_datatype_t type);
void* MUSCLE_Receive(const char *entrance_name, void *array, size_t *size, muscle_datatype_t type);

#ifdef __cplusplus
}
#endif

#endif /* CMUSCLE_H */
