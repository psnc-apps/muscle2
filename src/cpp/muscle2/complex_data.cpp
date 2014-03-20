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
#include <stdlib.h>
#include <stdexcept>
#include "complex_data.hpp"
#include "muscle_types.h"
#include "util/logger.hpp"

static const bool is_muscle_complex_t_array[] = {
false, true, true, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
true, true, true, true, true, true, true, true,// string_arr + simple types array
false, false, false, false, false, false, false,// simple types matrix 2D
false, false, false, false, false, false, false,// simple types matrix 3D
false, false, false, false, false, false, false, // simple types matrix 4D
true // java object byte array
};

static const bool is_muscle_complex_t_matrix_2D[] = {
false, false, false, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
false, false, false, false, false, false, false, false,// string_arr + simple types array
true, true, true, true, true, true, true,// simple types matrix 2D
false, false, false, false, false, false, false,// simple types matrix 3D
false, false, false, false, false, false, false, // simple types matrix 4D
false // java object byte array
};
static const bool is_muscle_complex_t_matrix_3D[] = {
false, false, false, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
false, false, false, false, false, false, false, false,// string_arr + simple types array
false, false, false, false, false, false, false,// simple types matrix 2D
true, true, true, true, true, true, true,// simple types matrix 3D
false, false, false, false, false, false, false, // simple types matrix 4D
false // java object byte array
};
static const bool is_muscle_complex_t_matrix_4D[] = {
false, false, false, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
false, false, false, false, false, false, false, false,// string_arr + simple types array
false, false, false, false, false, false, false,// simple types matrix 2D
false, false, false, false, false, false, false,// simple types matrix 3D
true, true, true, true, true, true, true, // simple types matrix 4D
false // java object byte array
};

static const size_t muscle_complex_t_sizeof[] = {
0, 0, 0, // null, map, collection don't have inherent size, this is exported to the values of the keys.
0, sizeof(bool), sizeof(char), sizeof(short), sizeof(int), sizeof(long), sizeof(float), sizeof(double), // string + simple types
0, sizeof(bool), sizeof(char), sizeof(short), sizeof(int), sizeof(long), sizeof(float), sizeof(double),// string_arr + simple types array
sizeof(bool), sizeof(char), sizeof(short), sizeof(int), sizeof(long), sizeof(float), sizeof(double),// simple types matrix 2D
sizeof(bool), sizeof(char), sizeof(short), sizeof(int), sizeof(long), sizeof(float), sizeof(double),// simple types matrix 3D
sizeof(bool), sizeof(char), sizeof(short), sizeof(int), sizeof(long), sizeof(float), sizeof(double), // simple types matrix 4D
sizeof(char) // java object
};

static const muscle_complex_t muscle_data_t_as_complex[] = {
	COMPLEX_DOUBLE_ARR, // MUSCLE_DOUBLE
	COMPLEX_FLOAT_ARR, // MUSCLE_FLOAT
	COMPLEX_INT_ARR, // MUSCLE_INT32
	COMPLEX_LONG_ARR, // MUSCLE_INT64
	COMPLEX_STRING, //MUSCLE_STRING
	COMPLEX_BOOLEAN_ARR, // MUSCLE_BOOLEAN
	COMPLEX_BYTE_ARR // MUSCLE_RAW
};

namespace muscle {


ComplexData::ComplexData(muscle_complex_t type, std::vector<int>* dimensions)
{
	int nprod = 1;
	this->type = type;
	if (dimensions != NULL) {
		this->dims = *dimensions;
		for(std::vector<int>::const_iterator it = dims.begin(); it != dims.end(); ++it)
		{
			nprod *= *it;
		}
	}
	ComplexData::checkDimensions(type, &this->dims);
	
	this->value = malloc(nprod*ComplexData::sizeOfPrimitive(type));
}

ComplexData::ComplexData(void *data, muscle_complex_t type, std::vector<int>* dimensions)
{
	this->value = data;
	this->type = type;
	if (dimensions != NULL) {
		this->dims = *dimensions;
	}
	ComplexData::checkDimensions(type, &this->dims);
}

ComplexData::ComplexData(void *data, muscle_datatype_t type, size_t len)
{
	if (type == MUSCLE_COMPLEX)
	{
		ComplexData *cdata = (ComplexData *)data;
		this->value = cdata->value;
		this->type = cdata->type;
		this->dims = cdata->dims;
	}
	else
	{
		this->value = data;
		this->type = muscle_data_t_as_complex[type];
		if (is_muscle_complex_t_array[this->type])
		{
			this->dims.push_back((int)len);
		}
	}
	ComplexData::checkDimensions(this->type, &this->dims);
}

ComplexData::~ComplexData()
{
	free(value);
}

void * ComplexData::getData()
{
	return value;
}

const void * ComplexData::getData() const
{
	return value;
}

muscle_complex_t ComplexData::getType() const
{
	return type;
}

std::vector<int> ComplexData::getDimensions() const
{
	return dims;
}

size_t ComplexData::length() const
{
	size_t nprod = 1;
	for(std::vector<int>::const_iterator it = dims.begin(); it != dims.end(); ++it)
	{
		nprod *= *it;
	}
	return nprod;
}

int ComplexData::index(int x, int y) const
{
	if (!is_muscle_complex_t_matrix_2D[type]) {
		throw std::logic_error("Can only compute the 2D index of a 2D matrix");
	} if (x < 0) {
		throw std::range_error("x is less than 0");
	} if (x > dims[0]) {
		throw std::range_error("x is larger than the row size");
	} if (y < 0) {
		throw std::range_error("y is less than 0");
	} if (y > dims[1]) {
		throw std::range_error("y is larger than the column size");
	}
	return fidx(x, y);
}
int ComplexData::index(int x, int y, int z) const
{
	if (!is_muscle_complex_t_matrix_3D[type]) {
		throw std::logic_error("Can only compute the 3D index of a 3D matrix");
	} if (x < 0) {
		throw std::range_error("x is less than 0");
	} if (x > dims[0]) {
		throw std::range_error("x is larger than the row size");
	} if (y < 0) {
		throw std::range_error("y is less than 0");
	} if (y > dims[1]) {
		throw std::range_error("y is larger than the column size");
	} if (z < 0) {
		throw std::range_error("z is less than 0");
	} if (z > dims[2]) {
		throw std::range_error("z is larger than the column size");
	}
	return fidx(x, y, z);
}

int ComplexData::index(int x, int y, int z, int zz) const
{
	if (!is_muscle_complex_t_matrix_4D[type]) {
		throw std::logic_error("Can only compute the 4D index of a 4D matrix");
	} if (x < 0) {
		throw std::range_error("x is less than 0");
	} if (x > dims[0]) {
		throw std::range_error("x is larger than the row size");
	} if (y < 0) {
		throw std::range_error("y is less than 0");
	} if (y > dims[1]) {
		throw std::range_error("y is larger than the column size");
	} if (z < 0) {
		throw std::range_error("z is less than 0");
	} if (z > dims[2]) {
		throw std::range_error("z is larger than the column size");
	} if (zz < 0) {
		throw std::range_error("zz is less than 0");
	} if (zz > dims[3]) {
		throw std::range_error("zz is larger than the column size");
	}
	return fidx(x, y, z, zz);
}

size_t ComplexData::sizeOfPrimitive() const
{
	return muscle_complex_t_sizeof[type];
}
// Static
size_t ComplexData::sizeOfPrimitive(muscle_complex_t type)
{
	return muscle_complex_t_sizeof[type];
}

int ComplexData::dimensions(muscle_complex_t type)
{
	if (is_muscle_complex_t_array[type]) {
		return 1;
	} else if (is_muscle_complex_t_matrix_2D[type]) {
		return 2;
	} else if (is_muscle_complex_t_matrix_3D[type]) {
		return 3;
	} else if (is_muscle_complex_t_matrix_4D[type]) {
		return 4;
	} else {
		return 0;
	}
}

muscle_complex_t ComplexData::getType(muscle_datatype_t type)
{
	return muscle_data_t_as_complex[type];
}

void ComplexData::checkDimensions(muscle_complex_t type, std::vector<int>* dimensions)
{
	size_t size = dimensions->size();
	if (is_muscle_complex_t_array[type] && size != 1) {
		throw std::invalid_argument("An array should only have a single dimension");
	}
	if (is_muscle_complex_t_matrix_2D[type] && size != 2) {
		throw std::invalid_argument("A 2D matrix should only 2 dimensions");
	}
	if (is_muscle_complex_t_matrix_3D[type] && size != 3) {
		throw std::invalid_argument("A 3D matrix should only 3 dimensions");
	}
	if (is_muscle_complex_t_matrix_4D[type] && size != 4) {
		throw std::invalid_argument("A 4D matrix should only 4 dimensions");
	}
}


} // EO namespace muscle
