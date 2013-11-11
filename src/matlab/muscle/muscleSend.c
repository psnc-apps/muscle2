#include "mex.h" 
#include "muscle_types.h"
#include "cmuscle.h"

void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{
	char name[128];
	int count;
	void *arr;
	int i;
	
	mxGetString(prhs[0],name, 128);
	/* mexPrintf("MUSCLE Send nlhs=%d nrhs=%d  mxGetN=%d name=%s\n", nlhs, nrhs,  mxGetN(prhs[1]), name); 

	for (i=0 ; i < mxGetN(prhs[1]); i++) {
		printf("data[%d]=%f\n", i, mxGetPr(prhs[1])[i]);
	} */

	MUSCLE_Send(name, mxGetPr(prhs[1]), mxGetN(prhs[1]),  MUSCLE_DOUBLE);	

	return;
}
