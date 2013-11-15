#include "mex.h" 
#include "cmuscle.h"

#define NAME_ARG prhs[0]

void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{
	char name[128];
	int count;
	void *ptr;
	int data_type;
	
	
	mxGetString(NAME_ARG,name, 128);


	mexPrintf("MUSCLE Receive: %s\n", name);

	return;
}
