#include <mpi.h>
#include "Test.h"

#include <JNITool.h>

#include <access/ConduitExitArray.h>
#include <access/ConduitExitSingle.h>
#include <access/ConduitEntranceArray.h>
#include <access/ConduitEntranceSingle.h>


#include <logging/StreamLogHandler.h>
#include <logging/Logger.h>
#include <logging/JavaLogHandler.h>
#include <access/KernelController.h>
#include <Version.h>
#include <string.h>


using namespace muscle;
using namespace std;

/**
a native kernel which sends an array of double
\author Jan Hegewald
*/
JNIEXPORT void JNICALL Java_examples_simplempi_Test_callNative
  (JNIEnv* env, jobject obj)
{
	MPI::Init();
	
// 	try
// 	{
		cout << endl << "c++: begin " << __FILE__ << endl;

// 		KernelController kernel(env, obj);
// 		for(int time = 0; !kernel.willStop(); time ++) {
// 			cout << "step " << time << endl;
// 		}
		
		const int rank = MPI::COMM_WORLD.Get_rank();
		const int size = MPI::COMM_WORLD.Get_size();
		
		if(rank==0)
		{
			for (int i = 1 ; i < size ; ++i)
			{
				int length;
				MPI::COMM_WORLD.Recv(&length, 1, MPI::INT, i, MPI::ANY_TAG);
				char * hi = new char[length];
				MPI::COMM_WORLD.Recv(hi, length, MPI::CHAR, i, MPI::ANY_TAG);
				cout << hi << endl;
				delete hi;
			}
			cout << "Greetings from the Rank 0!" << endl;
			
		}
		else
		{
			stringstream ss;
			ss << "Hello from " << rank << "/" << size-1 << "!";
			
			const char * greeting = ss.str().c_str();
			
			int length = strlen(greeting)+1;
			
			MPI::COMM_WORLD.Send(&length, 1, MPI::INT, 0, 0);
			MPI::COMM_WORLD.Send(greeting, length, MPI::CHAR, 0, 0);
		}
		
		
		cout << endl << "c++: end " << __FILE__ << endl;
// 	}
// 	catch(std::runtime_error& e)
// 	{
// 		std::cerr<<"\nRUNTIME ERROR: "<<e.what()<<"\n"<<std::endl;
// 	}
// 	catch(...)
// 	{
// 		std::cerr<<"unknown error"<<std::endl;
// 	}
	
	MPI::Finalize();
	
}
