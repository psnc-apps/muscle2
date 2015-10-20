#include "mex.h" 
#include "cmuscle.h"
#include <string.h>

#define KEY_ARG prhs[0]
#define VALUE_OUT plhs[0]


void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{
	char key[256];
	
	mxGetString(KEY_ARG, key, 256);

	const char *prop = MUSCLE_Get_Property(key);
	VALUE_OUT = mxCreateString(prop);

	return;
}
