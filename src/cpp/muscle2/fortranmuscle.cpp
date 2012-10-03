#include "fortranmuscle.h"
#include "cppmuscle.hpp"

#include <string>
#include <cstring>
#include <stdlib.h>

void muscle_init_(int *argc, char *argv)
{
	// Construct a pointer array for identifying arguments in the long string
	// argv.
	char **ptrs = (char **)malloc(*argc * sizeof(char *));
	
	size_t prevSz = 0;
	for (int i = 0; i <= *argc; i++) {
		// Only move to next word if there is one; the first time increment with 0.
		argv += prevSz;
		ptrs[i] = argv;
		prevSz = strlen(ptrs[i])+1;
	}
	// C expects argc to be one larger (program name) than Fortran provides.
	(*argc)++;
	muscle::env::init(argc, &ptrs);
	(*argc)--;
}

void muscle_kernel_name_(char* result)
{
	strcpy(result, muscle::cxa::kernel_name().c_str());
}

void muscle_get_property_(const char* name, char *result)
{
	strcpy(result, muscle::cxa::get_property(std::string(name)).c_str());
}

void muscle_receive_(const char *entrance_name, void *array, int *size, muscle_datatype_t *type)
{
	size_t sz;
	muscle::env::receive(std::string(entrance_name), array, sz, *type);
	*size = (int)sz;
}

void muscle_send_(const char *exit_name, void *array, int *size, muscle_datatype_t *type)
{
	muscle::env::send(std::string(exit_name), array, *size, *type);
}

void muscle_will_stop_(int *result) {
	*result = muscle::env::will_stop();
}

void muscle_finalize_(void)
{
	muscle::env::finalize();
}
