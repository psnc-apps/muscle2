#include "mex.h" 

void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{
	mexPrintf("initializing MUSCLE\n");

	MUSCLE_Init(NULL, NULL); 

	mexPrintf("MUSCLE initialized\n");

	return;
}
