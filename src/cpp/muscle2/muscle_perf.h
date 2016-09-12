#ifndef MUSCLE_PERF_H
#define MUSCLE_PERF_H

#ifdef __cplusplus
extern "C" {
#endif //__cplusplus

#include <stdint.h>
#include <stdbool.h>
#include <time.h>

/* Enum for various MUSCLE2 performance metrics */
typedef enum {
	MUSCLE_PERF_COUNTER_SEND_CALLS,       // Number of send calls via MUSCLE2 so far (cumulative value)
	MUSCLE_PERF_COUNTER_SEND_DURATION,    // Nanoseconds spent with MUSCLE2 send operations so far (cumulative value)
	MUSCLE_PERF_COUNTER_SEND_SIZE,        // Number of bytes sent via MUSCLE2 so far (cumulative value)
	MUSCLE_PERF_COUNTER_RECEIVE_CALLS,    // Number of receive calls via MUSCLE2 so far (cumulative value)
	MUSCLE_PERF_COUNTER_RECEIVE_DURATION, // Nanoseconds spent with MUSCLE2 receive operations so far (cumulative value)
	MUSCLE_PERF_COUNTER_RECEIVE_SIZE,     // Number of bytes received via MUSCLE2 so far (cumulative value)
	MUSCLE_PERF_COUNTER_BARRIER_CALLS,    // Number of calls to MUSCLE2 barrier
	MUSCLE_PERF_COUNTER_BARRIER_DURATION, // Nanoseconds spent in MUSCLE2 barriers
	MUSCLE_PERF_COUNTER_LAST      // LAST element for iterating over the enum
} muscle_perf_counter_t;

/* Struct to store values of a metric type (i.e. sends) */
struct muscle_perf_t {
	uint64_t calls;    // Total number of calls
	uint64_t duration; // Total time spent in ns
	uint64_t size;     // Total number of sent/received bytes
};

/* Returns a C string managed internally by MUSCLE2, with a pretty print of the performance values. The return
 * pointer is valid only until the next call to this function. */
const char * MUSCLE_Perf_Dump(void);

/* Writes a performance metric value denoted by id to the location pointed to by value. 
 * Internally forwards the call to the env::MUSCLE_Perf_Get_Counter(muscle_perf_counter_t id, uint64_t * value) 
 * C++ function.
 * Returns 0 on success, -1 on failure. */
int MUSCLE_Perf_Get_Counter(muscle_perf_counter_t id, uint64_t *value);

/* Returns true if MUSCLE is inside an API call (send, receive, barrier). 
 * If it is, writes the timestamp of the start of the call to start_time and the counter id of the call duration counter
 * to id (e.g. MUSCLE_PERF_COUNTER_BARRIER_DURATION). If we aren't in any call, sets both start_time and id to NULL. */
bool MUSCLE_Perf_In_Call(struct timespec *start_time, muscle_perf_counter_t *id);

#ifdef __cplusplus
}
#endif //__cplusplus

#endif //MUSCLE_PERF_H
