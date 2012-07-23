#include "complex_data.hpp"
#include "muscle_types.h"

static const bool is_muscle_complex_t_array[] = {
false, true, true, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
true, true, true, true, true, true, true, true,// string_arr + simple types array
false, false, false, false, false, false, false,// simple types matrix 2D
false, false, false, false, false, false, false,// simple types matrix 3D
false, false, false, false, false, false, false, // simple types matrix 4D
true // java object
};

static const bool is_muscle_complex_t_matrix[] = {
false, false, false, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
false, false, false, false, false, false, false, false,// string_arr + simple types array
true, true, true, true, true, true, true,// simple types matrix 2D
true, true, true, true, true, true, true,// simple types matrix 3D
true, true, true, true, true, true, true, // simple types matrix 4D
false // java object
};
static const bool is_muscle_complex_t_matrix3D[] = {
false, false, false, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
false, false, false, false, false, false, false, false,// string_arr + simple types array
false, false, false, false, false, false, false,// simple types matrix 2D
true, true, true, true, true, true, true,// simple types matrix 3D
false, false, false, false, false, false, false, // simple types matrix 4D
false // java object
};
static const bool is_muscle_complex_t_matrix4D[] = {
false, false, false, // null, map, collection
false, false, false, false, false, false, false, false, // string + simple types
false, false, false, false, false, false, false, false,// string_arr + simple types array
false, false, false, false, false, false, false,// simple types matrix 2D
false, false, false, false, false, false, false,// simple types matrix 3D
true, true, true, true, true, true, true, // simple types matrix 4D
false // java object
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

ComplexData::ComplexData(void *data, muscle_complex_t type, std::vector<int>* dimensions)
{
	this->value = data;
	this->type = type;
	if (dimensions != NULL) {
		this->dims = *dimensions;
	}
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
			this->dims.push_back(len);
		}
	}
}

ComplexData::~ComplexData()
{
	free((void *)value);
}

const void * ComplexData::getData()
{
	return value;
}

muscle_complex_t ComplexData::getType()
{
	return type;
}

std::vector<int>& ComplexData::getDimensions()
{
	return dims;
}

int ComplexData::length()
{
	int nprod = 1;
	for(std::vector<int>::iterator it = dims.begin(); it != dims.end(); ++it)
	{
		nprod *= *it;
	}
	return nprod;
}

size_t ComplexData::sizeOfPrimitive()
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
	if (is_muscle_complex_t_array[type])
	{
		return 1;
	} else if (is_muscle_complex_t_matrix[type]) {
		if (is_muscle_complex_t_matrix4D[type]) {
			return 4;
		} else if (is_muscle_complex_t_matrix3D[type]) {
			return 3;
		} else {
			return 2;
		}
	} else {
		return 0;
	}
}

muscle_complex_t ComplexData::getType(muscle_datatype_t type) {
	return muscle_data_t_as_complex[type];
}


} // EO namespace muscle
