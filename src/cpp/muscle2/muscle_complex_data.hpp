#ifndef MUSCLE_COMPLEX_DATA_HPP
#define MUSCLE_COMPLEX_DATA_HPP

#include <vector>
#include <cstring>
#include "muscle_types.h"

// For 
// Keep in sync with muscle.util.data.SerializableDatatype!
typedef enum {
	COMPLEX_NULL, COMPLEX_MAP, COMPLEX_COLLECTION,
	COMPLEX_STRING, COMPLEX_BOOLEAN, COMPLEX_BYTE, COMPLEX_SHORT, COMPLEX_INT, COMPLEX_LONG, COMPLEX_FLOAT, COMPLEX_DOUBLE,
	COMPLEX_STRING_ARR, COMPLEX_BOOLEAN_ARR, COMPLEX_BYTE_ARR, COMPLEX_SHORT_ARR, COMPLEX_INT_ARR, COMPLEX_LONG_ARR, COMPLEX_FLOAT_ARR, COMPLEX_DOUBLE_ARR,
	COMPLEX_BOOLEAN_MATRIX_2D, COMPLEX_BYTE_MATRIX_2D, COMPLEX_SHORT_MATRIX_2D, COMPLEX_INT_MATRIX_2D, COMPLEX_LONG_MATRIX_2D, COMPLEX_FLOAT_MATRIX_2D, COMPLEX_DOUBLE_MATRIX_2D,
	COMPLEX_BOOLEAN_MATRIX_3D, COMPLEX_BYTE_MATRIX_3D, COMPLEX_SHORT_MATRIX_3D, COMPLEX_INT_MATRIX_3D, COMPLEX_LONG_MATRIX_3D, COMPLEX_FLOAT_MATRIX_3D, COMPLEX_DOUBLE_MATRIX_3D,
	COMPLEX_BOOLEAN_MATRIX_4D, COMPLEX_BYTE_MATRIX_4D, COMPLEX_SHORT_MATRIX_4D, COMPLEX_INT_MATRIX_4D, COMPLEX_LONG_MATRIX_4D, COMPLEX_FLOAT_MATRIX_4D, COMPLEX_DOUBLE_MATRIX_4D,
	COMPLEX_JAVA_BYTE_OBJECT } muscle_complex_t;

namespace muscle {

class ComplexData {
	public:
		ComplexData(void *data, muscle_complex_t type, std::vector<int>* dimensions);
		ComplexData(void *data, muscle_datatype_t type, size_t len);
		~ComplexData();
		void *getData();
		muscle_complex_t getType();
		std::vector<int>& getDimensions();
		int length();
		size_t sizeOfPrimitive();
		static size_t sizeOfPrimitive(muscle_complex_t type);
		static int dimensions(muscle_complex_t type);
		static muscle_complex_t getType(muscle_datatype_t type);
	private:
		muscle_complex_t type;
		void *value;
		std::vector<int> dims;
};

}

#endif
