
#include "MuscleCPP.hpp"


namespace muscle {

void Entrance::Send(double *aray, size_t size)
{

}

double* Exit::Receive(double *array, size_t &size)
{

	return 0;
};

void MUSCLE::Init(void)
{


}

void MUSCLE::Cleanup(void)
{


}

bool MUSCLE::WillStop(void)
{
	return true;
}

Entrance* MUSCLE::AddEntrance(std::string name, int rate, MUSCLE_Datatype_t type)
{

}

Exit* MUSCLE::AddExit(std::string name, int rate, MUSCLE_Datatype_t type)
{

}

std::string CXA::KernelName()
{

	return 0;
}

std::string CXA::GetProperty(std::string name)
{

	return 0;
}


} // EO namespace muscle
