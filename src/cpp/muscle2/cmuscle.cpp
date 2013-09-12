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
#include "cmuscle.h"
#include "cppmuscle.hpp"

#include <string>
#include <cstring>

muscle_error_t MUSCLE_Init(int *argc, char ***argv)
{
	return muscle::env::init(argc, argv);
}

const char* MUSCLE_Kernel_Name(void)
{
	return strdup(muscle::cxa::kernel_name().c_str());
}

const char* MUSCLE_Get_Property(const char* name)
{
	return strdup(muscle::cxa::get_property(std::string(name)).c_str());
}

int MUSCLE_Has_Property(const char *name)
{
	return muscle::cxa::has_property(name);
}

void* MUSCLE_Receive(const char *entrance_name, void *array, size_t *size, muscle_datatype_t type)
{
	return muscle::env::receive(std::string(entrance_name), array, *size, type);
}

muscle_error_t MUSCLE_Send(const char *exit_name, void *array, size_t size, muscle_datatype_t type)
{
	muscle::env::send(std::string(exit_name), array, size, type);

	return MUSCLE_SUCCESS;
}

int MUSCLE_Will_Stop(void) {
	return muscle::env::will_stop();
}

void MUSCLE_Finalize(void)
{
	muscle::env::finalize();
}

int MUSCLE_Barrier_Init(char **barrier, size_t *len, int num_procs)
{
	return muscle::env::barrier_init(barrier, len, num_procs);
}
int MUSCLE_Barrier(const char *barrier) {
	return muscle::env::barrier(barrier);
}

void MUSCLE_Barrier_Destroy(char *barrier) {
	return muscle::env::barrier_destroy(barrier);
}
