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

#include <exception>
#include <stdexcept>
#include <string>
#include <errno.h>

#include "logger.hpp"

namespace muscle {

class muscle_exception : public std::runtime_error {
public:
    const int error_code;

    muscle_exception(const std::exception& ex) : std::runtime_error(ex.what()), error_code(errno)    { log(); }
	muscle_exception (std::string msg) throw() : std::runtime_error(msg), error_code(errno)          { log(); }
	muscle_exception (std::string msg, int code) throw() : std::runtime_error(msg), error_code(code) { log(); }

	// Already logged in given muscle_exception
    muscle_exception(const muscle_exception& ex) : std::runtime_error(ex.what()), error_code(ex.error_code) {}

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
};

}

#endif
