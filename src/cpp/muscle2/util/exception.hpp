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
#ifndef MUSCLE_EXCEPTION_HPP
#define MUSCLE_EXCEPTION_HPP

#include "logger.hpp"

#include <exception> // std::exception
#include <stdexcept> // std::runtime_error
#include <string>  // std::string
#include <errno.h> // errno
#include <cstring> // strerror
#include <cstdlib>
#include <execinfo.h>
#include <cxxabi.h>

namespace muscle {

class muscle_exception : public std::runtime_error {
public:
    const int error_code;

    muscle_exception(const std::exception& ex) throw() : std::runtime_error(ex.what()), error_code(errno)
    { log(); }
	muscle_exception (std::string msg) throw() : std::runtime_error(msg), error_code(errno)
	{ log(); }
	muscle_exception (std::string msg, int code) throw() : std::runtime_error(msg), error_code(code)
    { log(); }

	void log()
	{
		const char *w = what();
		if (error_code)
		{
			const char *err = strerror(error_code);
			logger::severe("%s: %s", w, err);
		}
		else
			logger::severe(w);
	}
    muscle_exception(std::string msg, int code, bool silent) throw() : std::runtime_error(msg), error_code(code) { if (!silent) { log(); print_stacktrace(); } }
	
	// stacktrace.h (c) 2008, Timo Bingmann from http://idlebox.net/
	// published under the WTFPL v2.0

	/** Print a demangled stack backtrace of the caller function to FILE* out. */
	static void print_stacktrace(unsigned int max_frames = 63)
	{
		size_t len;
		char *str = new char[max_frames*512];
		len = sprintf(str, "stack trace:\n");
		
		// storage array for stack trace address data
		void* addrlist[max_frames+1];
		
		// retrieve current stack addresses
		int addrlen = backtrace(addrlist, (int)(sizeof(addrlist) / sizeof(void*)));
		
		if (addrlen == 0) {
			len += sprintf(str+len, "  <empty, possibly corrupt>\n");
			logger::severe(str);
			delete [] str;
			return;
		}
		
		// resolve addresses into strings containing "filename(function+address)",
		// this array must be free()-ed
		char** symbollist = backtrace_symbols(addrlist, addrlen);
		
		// allocate string which will be filled with the demangled function name
		size_t funcnamesize = 256;
		char* funcname = (char*)malloc(funcnamesize);
		
		// iterate over the returned symbol lines. skip the first, it is the
		// address of this function.
		for (int i = 1; i < addrlen; i++) {
			char *begin_name = 0, *begin_offset = 0, *end_offset = 0;
			
			// find parentheses and +address offset surrounding the mangled name:
			// ./module(function+0x15c) [0x8048a6d]
			for (char *p = symbollist[i]; *p; ++p) {
				if (*p == '(')
					begin_name = p;
				else if (*p == '+')
					begin_offset = p;
				else if (*p == ')' && begin_offset) {
					end_offset = p;
					break;
				}
			}
			
			if (begin_name && begin_offset && end_offset
				&& begin_name < begin_offset) {
				*begin_name++ = '\0';
				*begin_offset++ = '\0';
				*end_offset = '\0';
				
				// mangled name is now in [begin_name, begin_offset) and caller
				// offset in [begin_offset, end_offset). now apply
				// __cxa_demangle():
				
				int status;
				char* ret = abi::__cxa_demangle(begin_name,
												funcname, &funcnamesize, &status);
				if (status == 0) {
					funcname = ret; // use possibly realloc()-ed string
					len += sprintf(str+len, "  %s : %s+%s\n",
							symbollist[i], funcname, begin_offset);
				} else {
					// demangling failed. Output function name as a C function with
					// no arguments.
					len += sprintf(str+len, "  %s : %s()+%s\n",
							symbollist[i], begin_name, begin_offset);
				}
			} else {
				// couldn't parse the line? print the whole line.
				len += sprintf(str+len, "  %s\n", symbollist[i]);
			}
		}
		
		free(funcname);
		free(symbollist);
		
		logger::severe(str);
		delete [] str;
	}
};

}

#endif
