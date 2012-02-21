#ifndef MUSCLETYPES_H
#define MUSCLETYPES_H

#ifdef __cplusplus
extern "C" {
#endif

typedef enum { MUSCLE_Double_Type,
	MUSCLE_Float_Type,
	MUSCLE_Int32_Type,
	MUSCLE_UInt32_Type,
	MUSCLE_Int64_Type,
	MUSCLE_UInt64_Type,
	MUSCLE_String_Type,
	MUSCLE_Boolean_Type,
	MUSCLE_Raw_Type
} MUSCLE_Datatype_t;

#ifdef __cplusplus
}
#endif

#endif /* MUSCLETYPES_H */
