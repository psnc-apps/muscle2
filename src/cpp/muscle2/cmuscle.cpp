
#include "cmuscle.h"
#include "cppmuscle.hpp"

#include <string.h>

int MUSCLE_Init(void)
{
	muscle::env::init();

	return 0;
}

const char* MUSCLE_Get_Kernel_Name(void)
{
	return strdup(muscle::cxa::kernel_name().c_str()); /* TODO store the pointer and release it in Finalize */
}

const char* MUSCLE_Get_Property(const char* name)
{
	return strdup(muscle::cxa::get_property(std::string(name)).c_str()); /* TODO store the pointer and release it in Finalize */
}

void* MUSCLE_Receive(const char *entrance_name, void *array, size_t *size, muscle_datatype_t type)
{
	return muscle::env::receive(std::string(entrance_name), array, *size, type);
}

int MUSCLE_Send(const char *exit_name, void *array, size_t size, muscle_datatype_t type)
{
	muscle::env::send(std::string(exit_name), array, size, type);

	return 0;
}


void MUSCLE_Finalize(void)
{
	muscle::env::finalize();
}
