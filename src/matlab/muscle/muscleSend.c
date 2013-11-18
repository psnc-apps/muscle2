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
	char buf[4096];
	
	
	mxGetString(NAME_ARG, name, 128);

	count = mxGetN(DATA_ARG);

#ifdef TRACE
	mexPrintf("arguments = %d\n", nrhs);
#endif

	if (mxIsDouble(DATA_ARG)) {
		data_type = MUSCLE_DOUBLE;
		ptr = mxGetPr(DATA_ARG);
	} else if (mxIsChar(DATA_ARG)) {
		data_type = MUSCLE_STRING;
		mxGetString(DATA_ARG, buf, sizeof(buf));	
		mexPrintf("String = %s\n", buf);
		ptr = buf;
		count = strlen(buf) + 1;
	} else if (mxIsInt32(DATA_ARG)) {
		data_type = MUSCLE_INT32;
		ptr = mxGetPr(DATA_ARG);
	} else if (mxIsLogical(DATA_ARG)) {
		data_type = MUSCLE_BOOLEAN; /*TODO size?*/
		ptr = mxGetLogicals(DATA_ARG);
	} else {
		mexErrMsgTxt("Unsupported data type");
	}

	
	MUSCLE_Send(name, ptr, count,  data_type);	

	return;
}
