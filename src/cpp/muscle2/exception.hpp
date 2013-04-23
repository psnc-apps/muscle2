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
