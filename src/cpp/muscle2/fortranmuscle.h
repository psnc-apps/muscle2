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
#ifndef FORTRANMUSCLE_H
#define FORTRANMUSCLE_H

#include "muscle_types.h"

#ifdef __cplusplus
extern "C" {
#endif

/** Initializes MUSCLE from Fortran.
 @param *argv a character string containing *argc arguments separated by nul characters.
 */
void muscle_init_(int* argc, char* argv, int len);
/** Finalizes MUSCLE. */
void muscle_finalize_(void);

/** Get the kernel name. Result (const) string is stored in *result. */
void muscle_kernel_name_(char * result, int reslen);
/** Get a property. Result (const) string is stored in *result. */
void muscle_get_property_(const char* name, char* result, int len, int reslen);
/** Whether a property exists. Result (const) boolean is stored in *result. */
void muscle_has_property_(const char* name, int* result, int len);
/** Whether the current instantiation should stop. Result (const) boolean is stored in *result. */
void muscle_will_stop_(int *result);

/** Send a message. Use MUSCLE datatypes to specify what is sent. From Fortran, only send arrays. */
void muscle_send_(const char *exit_name, void *array, int *size, muscle_datatype_t *type, int len);
/** Send a message. Use MUSCLE datatypes to specify what is sent. From Fortran, only receive arrays. 
 Also, make sure that enough memory is allocated to receive the next message.*/
void muscle_receive_(const char *entrance_name, void *array, int *size, muscle_datatype_t *type, int len);

#ifdef __cplusplus
}
#endif

#endif /* CMUSCLE_H */
