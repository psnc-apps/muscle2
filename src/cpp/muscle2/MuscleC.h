#ifndef MUSCLEC_H
#define MUSCLEC_H

#include "MuscleTypes.h"

#ifdef __cplusplus
extern "C" {
#endif


typedef void* MUSCLE_Entrance_t;
typedef void* MUSCLE_Exit_t;

int MUSCLE_Init(void);

const char* MUSCLE_Get_Kernel_Name(void);
const char* MUSCLE_Get_Property(const char* name);


MUSCLE_Entrance_t MUSCLE_Add_Entrance(const char *name, int rate, MUSCLE_Datatype_t datatype);

MUSCLE_Exit_t MUSCLE_Add_Exit(const char *name, int rate, MUSCLE_Datatype_t datatype);


void* MUSCLE_Receive(MUSCLE_Exit_t exit, void *array, size_t *size);

int MUSCLE_Send(MUSCLE_Entrance_t exit, void *array, size_t size);


void MUSCLE_Cleanup(void);

#ifdef __cplusplus
}
#endif

#endif /* MUSCLEC_H */
