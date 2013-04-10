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

muscle_error_t MUSCLE_Send(const char *exit_name, void *array, size_t size, muscle_datatype_t type);
void* MUSCLE_Receive(const char *entrance_name, void *array, size_t *size, muscle_datatype_t type);

#ifdef __cplusplus
}
#endif

#endif /* CMUSCLE_H */
