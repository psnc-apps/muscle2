#include "mex.h" 

void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{

	MUSCLE_Init(NULL, NULL); 

	return;
}
