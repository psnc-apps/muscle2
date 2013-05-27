/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
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
		/** Initialize data with given datatype and number of dimensions. */
		ComplexData(muscle_complex_t type, std::vector<int>* dimensions);
		/** Initialize with given data and datatype, and a number of dimensions.
		 * The given data must be allocated by MUSCLE or with malloc, and will be freed by MUSCLE.
		 * If datatype is not an array or matrix, dimensions are allowed to be null. */
		ComplexData(void *data, muscle_complex_t type, std::vector<int>* dimensions);
		/** Initialize a ComplexData with a simple MUSCLE datatype of an array, with given length. */
		ComplexData(void *data, muscle_datatype_t type, size_t len);
		~ComplexData();
		/** The data as a void-pointer. It is the responsibility of the caller to do the right casting, based on the complex type. Do not delete or free the data within. */
		void *getData();
		/** The data as a void-pointer. It is the responsibility of the caller to do the right casting, based on the complex type. Do not delete or free the data within. */
		const void *getData() const;
		/** Type of data contained within. */
		muscle_complex_t getType() const;
		/** Get a vector containing the dimensions of the data. This will return an empty vector if the data is not an array or matrix.*/
		std::vector<int> getDimensions() const;
		/** Get the total length of the data. */
		size_t length() const;
		/** Get the size of the most primitive datatype that is contained. For a double vector, that would be sizeof(double) = 8. */
		size_t sizeOfPrimitive() const;
		/** Get the index of the returned array. This will give an exception if it is out of bounds, or the wrong number of dimensions. */
		int index(int x, int y) const;
		int index(int x, int y, int z) const;
		int index(int x, int y, int z, int zz) const;
		/** Fast get the index of the returned array. This is inlined and does not do any bounds checking. */
		int fidx(int x, int y) const { return x*dims[1]+y; }
		int fidx(int x, int y, int z) const { return (x*dims[1] + y)*dims[2]+z; }
		int fidx(int x, int y, int z, int zz) const { return ((x*dims[1] + y)*dims[2]+z)*dims[3]+zz; }
		// properties of muscle_complex_t
		/** Get the size of the most primitive datatype that is contained. For a double vector, that would be sizeof(double) = 8. */
		static size_t sizeOfPrimitive(muscle_complex_t type);
		/** Get the number of dimensions of a given datatype. For a vector that would be 1, for a 3-D matrix, 3. */
		static int dimensions(muscle_complex_t type);
		/** Type of complex data that represents a muscle datatype. */
		static muscle_complex_t getType(muscle_datatype_t type);
	private:
		static void checkDimensions(muscle_complex_t type, std::vector<int>* dimensions);
		muscle_complex_t type;
		void *value;
		std::vector<int> dims;
};

}

#endif
