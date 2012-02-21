#ifndef MUSCLECPP_H
#define MUSCLECPP_H

#include <string>

#include "MuscleTypes.h"

namespace muscle {

	class Entrance
	{
	  public:
		void Send(double *aray, size_t size);
	};

	class Exit
	{
	  public:
		double* Receive(double *array, size_t &size);
	};

	class MUSCLE
	{
	  public:

		static void Init(void);
		static void Cleanup(void);

		static bool WillStop(void);

		static Entrance* AddEntrance(std::string name, int rate, MUSCLE_Datatype_t type);
		static Exit* AddExit(std::string name, int rate, MUSCLE_Datatype_t type);
	};

	class CXA
	{
	  public:
		static std::string KernelName();
		static std::string GetProperty(std::string name);
	};


} // EO namespace muscle
#endif



