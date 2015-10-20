#include "mex.h" 
#include "cmuscle.h"
#include <string.h>

#define VALUE_OUT plhs[0]


void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{
	const char *name = MUSCLE_Kernel_Name();
	VALUE_OUT = mxCreateString(name);

	return;
}
