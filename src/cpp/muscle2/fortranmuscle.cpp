#include "fortranmuscle.h"
#include "cppmuscle.hpp"
#include "logger.hpp"

#include <string>
#include <cstring>
#include <stdlib.h>
#include <ctype.h>

using namespace muscle;

char *f2cstr(const char *str, int len)
{
	size_t sz = len;
	bool term;
	if (memchr(str, '\0', sz) == NULL) {
		term = false;
	} else {
		term = true;
		sz = strlen(str);
	}
	char *cstr = (char *)malloc(sz+1);
	memcpy(cstr, str, sz);
	char *end = cstr + sz - 1;
	
	if (!term) {
		logger::warning("Fortran string ''%s'' was not null-terminated, trimming spaces", cstr);
		while(end > cstr && isspace(*end)) end--;
	}
	*(end+1) = '\0';
	return cstr;
}

void c2fstr(const char *cstr, char *fstr, int reslen)
{
	int len = strlen(cstr);
	if (len > reslen) {
		logger::severe("Can not store string ''%s'' of size %d in Fortran string of size %d", cstr, len, reslen);
		len = reslen;
	}
	memcpy(fstr, cstr, len);
	char *end = fstr + reslen;
	fstr += len;
	while (fstr != end) *(fstr++) = ' ';
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
