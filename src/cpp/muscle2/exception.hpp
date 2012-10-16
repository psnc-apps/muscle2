#ifndef MUSCLE_EXCEPTION_HPP
#define MUSCLE_EXCEPTION_HPP

#include "logger.hpp"
#include <exception>

namespace muscle {

class muscle_exception : std::exception {

public:
	muscle_exception (std::string msg) throw() {
		desc = "MUSCLE exception: " + msg;
		logger::severe(desc.c_str());
	};
	virtual const char* what() const throw() { return desc.c_str(); };
	virtual ~muscle_exception() throw() {};
private:
	std::string desc;
};

}

#endif