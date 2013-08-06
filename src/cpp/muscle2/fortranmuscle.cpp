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
#include "fortranmuscle.h"
#include "cppmuscle.hpp"

#include <string>
#include <cstring>
#include <cstdlib>
#include <ctype.h>

using namespace muscle;

char *f2cstr(const char *str, const int len)
{
	const char * const term = (char *)memchr(str, '\0', len);
	const size_t sz = (term == NULL) ? len : term - str;

	char *cstr = (char *)malloc(sz+1);
	memcpy(cstr, str, sz);
	char *end = cstr + sz - 1;
	
	if (term == NULL) {
		while(end > cstr && isspace(*end)) end--;

		if (end != cstr + sz - 1)
			logger::warning("Fortran string ''%s'' was not null-terminated, trimmed spaces", cstr);
	}
	*(end+1) = '\0';

	return cstr;
}

void c2fstr(const char *cstr, char *fstr, const int reslen)
{
	size_t len = strlen(cstr);
	if (len > reslen) {
		logger::severe("Can not store string ''%s'' of size %d in Fortran string of size %d", cstr, len, reslen);
		len = reslen;
	}
	memcpy(fstr, cstr, len);
	char *end = fstr + reslen;
	fstr += len;
	while (fstr < end) *(fstr++) = ' ';
}

void muscle_init_(int *argc, char *argv, int len)
{
	// Construct a pointer array for identifying arguments in the long string
	// argv
	char *(ptrs[100]);
	char **cptrs = ptrs;
	char margv[25600];
	char *cargv = margv;
	memcpy(cargv, argv, len);
	
	size_t prevSz = 0;
	for (int i = 0; i <= *argc; i++) {
		// Only move to next word if there is one; the first time increment with 0.
		cargv += prevSz;
		ptrs[i] = cargv;
		prevSz = strlen(ptrs[i])+1;
	}
	// C expects argc to be one larger (program name) than Fortran provides.
	(*argc)++;
	muscle::env::init(argc, &cptrs);
	(*argc)--;
}

void muscle_kernel_name_(char* result, int reslen)
{
	c2fstr(muscle::cxa::kernel_name().c_str(), result, reslen);
}

void muscle_get_property_(const char* name, char *result, int len, int reslen)
{
	char *cstr = f2cstr(name, len);
	std::string sname(cstr);
	free(cstr);
	c2fstr(muscle::cxa::get_property(sname).c_str(), result, reslen);
}

void muscle_has_property_(const char* name, int *result, int len)
{
	char *cstr = f2cstr(name, len);
	std::string sname(cstr);
	free(cstr);
	*result = muscle::cxa::has_property(sname);
}

void muscle_receive_(const char *entrance_name, void *array, int *size, muscle_datatype_t *type, int len)
{
	char *cstr = f2cstr(entrance_name, len);
	std::string sname(cstr);
	free(cstr);
	size_t sz;
	muscle::env::receive(sname, array, sz, *type);
	*size = (int)sz;
}

void muscle_send_(const char *exit_name, void *array, int *size, muscle_datatype_t *type, int len)
{
	char *cstr = f2cstr(exit_name, len);
	std::string sname(cstr);
	free(cstr);
	muscle::env::send(sname, array, *size, *type);
}

void muscle_will_stop_(int *result) {
	*result = muscle::env::will_stop();
}

void muscle_finalize_(void)
{
	muscle::env::finalize();
}
