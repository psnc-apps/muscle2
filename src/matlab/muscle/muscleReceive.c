#include "mex.h" 
#include "cmuscle.h"
#include <string.h>
#include <assert.h>

#define NAME_ARG prhs[0]
#define DATATYPE_ARG prhs[1]
#define OUT_A plhs[0]



void mexFunction(int nlhs, mxArray *plhs[], /* Output variables */ int nrhs, const mxArray *prhs[]) /* Input variables */
{
	char name[128];
	char datatype[128];
	int count;
	void *ptr;
	int datatype_code;
	size_t size;
	void *data;
	
	mxGetString(NAME_ARG, name, 128);
	mxGetString(DATATYPE_ARG, datatype, 128);

#ifdef TRACE
	mexPrintf("MUSCLE Receive: %s datatype: %s\n", name, datatype);
#endif

	if (strcmp("MUSCLE_DOUBLE", datatype) == 0) {
		datatype_code = MUSCLE_DOUBLE;
	} else if (strcmp("MUSCLE_STRING", datatype) == 0) {
		datatype_code = MUSCLE_STRING;
	} else if (strcmp("MUSCLE_INT32", datatype) == 0) {
		datatype_code = MUSCLE_INT32;
	} else if (strcmp("MUSCLE_BOOLEAN", datatype) == 0) {
		datatype_code = MUSCLE_BOOLEAN;
	} else {
                mexErrMsgTxt("Unsupported data type");
		assert(0);
        }

	data = MUSCLE_Receive(name, NULL, &size, datatype_code); 

#ifdef TRACE
	mexPrintf("MUSCLE Receive: size=%d\n", size);
#endif

	if (datatype_code == MUSCLE_DOUBLE) {
		OUT_A = mxCreateDoubleMatrix(1, size, mxREAL);
		memcpy(mxGetPr(OUT_A), data, sizeof(double) * size);		
	} else if (datatype_code == MUSCLE_STRING) {
		OUT_A = mxCreateString(data);
	} else if (datatype_code == MUSCLE_INT32) {
		OUT_A = mxCreateNumericMatrix(1, size, mxINT32_CLASS, mxREAL);
		memcpy(mxGetPr(OUT_A), data, 4 * size);		
	} else if (datatype_code == MUSCLE_BOOLEAN) {
		OUT_A = mxCreateLogicalMatrix(1, size);
		assert(sizeof(bool) == 1);
		memcpy(mxGetLogicals(OUT_A), data, size);
	}
	
	free(data);

	return;
}
