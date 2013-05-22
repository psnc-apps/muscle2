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

#include "logger.hpp"

namespace muscle {

class muscle_exception : public std::runtime_error {

public:
	muscle_exception (std::string msg) throw() : std::runtime_error(msg) {
		logger::severe(msg.c_str());
	};
};

}

#endif
