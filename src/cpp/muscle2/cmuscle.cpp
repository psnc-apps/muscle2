#include "cmuscle.h"
#include "cppmuscle.hpp"

#include <string.h>
#include <cstring>

muscle_error_t MUSCLE_Init(int *argc, char ***argv)
{
	return muscle::env::init(argc, argv);
}

const char* MUSCLE_Kernel_Name(void)
{
	return strdup(muscle::cxa::kernel_name().c_str()); /* TODO store the pointer and release it in Finalize */
}

const char* MUSCLE_Get_Property(const char* name)
{
	return strdup(muscle::cxa::get_property(std::string(name)).c_str()); /* TODO store the pointer and release it in Finalize */
}

void* MUSCLE_Receive(const char *entrance_name, void *array, size_t &size, muscle_datatype_t type)
{
	return muscle::env::receive(std::string(entrance_name), array, size, type);
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
