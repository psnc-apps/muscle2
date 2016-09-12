#include "muscle_perf.h"
#include "cppmuscle.hpp"

/* Expose static getter function from `env' C++ class as a C function */
int MUSCLE_Perf_Get_Counter(muscle_perf_counter_t id, uint64_t *value) {
	return muscle::env::get_perf_counter(id, value);
}

bool MUSCLE_Perf_In_Call(struct timespec *start_time, muscle_perf_counter_t *id) {
	return muscle::env::is_in_call(start_time, id);
}

const char * MUSCLE_Perf_Dump(void) {
	return muscle::env::get_perf_string();
}
