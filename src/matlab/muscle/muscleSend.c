#include "mex.h" 
#include "muscle_types.h"
#include "cmuscle.h"

#define NAME_ARG prhs[0]
#define DATA_ARG prhs[1]

void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{
	char name[128];
	int count;
	void *ptr;
	int data_type;
	
	
	mxGetString(NAME_ARG,name, 128);

	count = mxGetN(DATA_ARG);

	ptr = mxGetPr(DATA_ARG);

	if (mxIsDouble(DATA_ARG)) {
		data_type = MUSCLE_DOUBLE;
	} else if (mxIsChar(DATA_ARG)) {
		data_type = MUSCLE_STRING;
	} else if (mxIsInt32(DATA_ARG)) {
		data_type = MUSCLE_INT32;
	} else if (mxIsLogical(DATA_ARG)) {
		data_type = MUSCLE_BOOLEAN; /*TODO size?*/
	} else {
		mexErrMsgTxt("Unsupported data type");
	}

	/* mexPrintf("MUSCLE Send nlhs=%d nrhs=%d  mxGetN=%d name=%s\n", nlhs, nrhs,  mxGetN(prhs[1]), name); 

	for (i=0 ; i < mxGetN(prhs[1]); i++) {
		printf("data[%d]=%f\n", i, mxGetPr(prhs[1])[i]);
	} */

	
	MUSCLE_Send(name, ptr, count,  data_type);	

	return;
}
