#ifndef CMUSCLE_H
#define CMUSCLE_H

#include "muscle_types.h"

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif


int MUSCLE_Init(void);
void MUSCLE_Finalize(void);


const char* MUSCLE_Get_Kernel_Name(void);
const char* MUSCLE_Get_Property(const char* name);

int MUSCLE_Send(const char *exit_name, void *array, size_t size, muscle_datatype_t type);
void* MUSCLE_Receive(const char *entrance_name, void *array, size_t *size, muscle_datatype_t type);


#ifdef __cplusplus
}
#endif

#endif /* CMUSCLE_H */
