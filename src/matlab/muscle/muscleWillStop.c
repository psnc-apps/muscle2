#include "mex.h" 

void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{

	plhs[0] = mxCreateLogicalScalar( MUSCLE_Will_Stop() );

	return;
}
