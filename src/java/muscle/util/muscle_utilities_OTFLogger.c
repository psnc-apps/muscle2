#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <otf.h>
#include <unistd.h>
#include "muscle_utilities_OTFLogger.h"

uint64_t clockGet(void);

static OTF_FileManager *manager = NULL;
static OTF_Writer *writer = NULL;
static OTF_HandlerArray* handlers = NULL;

/* Length of kernels list - number of kernels*/
static int kernelsLength;

/* Length of conduits list - number of conduits */
static int conduitsLength;

static uint64_t timestamp = 0;

uint64_t clockGet(void) {
	struct timespec tp;

	clock_gettime(CLOCK_REALTIME, &tp); /* Elapsed time */

	return ((tp.tv_sec * 1.0e9L + tp.tv_nsec) - timestamp);
}

JNIEXPORT jint JNICALL Java_muscle_utilities_OTFLogger_begin(JNIEnv *env,
		jobject obj, jstring jpath) {

	const char *path = (*env)->GetStringUTFChars(env, jpath, 0);
	uint32_t number_of_streams = 100;
	manager = OTF_FileManager_open(number_of_streams);

	if (manager == NULL) {
		printf("OTFLogger libotf manager failed\n");
		return 1;
	}

	if ((writer = OTF_Writer_open((char *) path, number_of_streams, manager)) == 0) {
		printf("OTF_Writer_open failed\n");
		(*env)->ReleaseStringUTFChars(env, jpath, path);
		return 1;
	}
	(*env)->ReleaseStringUTFChars(env, jpath, path);

	handlers = OTF_HandlerArray_open();
	
	if (handlers == NULL) {
		printf("OTFLogger libotf handlers failed!!\n");
		return 1;
	}
	
	OTF_Writer_setCompression(writer, OTF_FILECOMPRESSION_UNCOMPRESSED);
	OTF_Writer_writeDefTimerResolution(writer, 1, 1.0e9L);

	return 0;
}

JNIEXPORT jint JNICALL Java_muscle_utilities_OTFLogger_define
(JNIEnv *env, jobject obj, jobjectArray kernelArray, jobjectArray conduitArray)
{	
	kernelsLength = (*env)->GetArrayLength(env, kernelArray);
	conduitsLength = (*env)->GetArrayLength(env, conduitArray);
	const char ** kernels = (const char **)calloc(kernelsLength, sizeof (char *));
	const char ** conduits = (const char **)calloc(conduitsLength, sizeof (char *));
	
	if(kernels == NULL || conduits == NULL)
		return 1;
	
	int i = 0;

	for(i = 0; i < kernelsLength; i++)
	{
		kernels[i] = (*env)->GetStringUTFChars(env,(*env)->GetObjectArrayElement(env, kernelArray, i),0);
		OTF_Writer_writeDefProcess(writer, 1, i + 1, kernels[i], 0);
	}

	OTF_Writer_writeDefFunctionGroup(writer, 1, 1, "Kernel State");

	for(i = 0; i < conduitsLength; i++)
	{
		conduits[i] = (*env)->GetStringUTFChars(env,(*env)->GetObjectArrayElement(env, conduitArray, i),0);
		OTF_Writer_writeDefFunction(writer, 1, i + 2, conduits[i], 1, 0);

	}

	for(i = 0; i < kernelsLength; i++)
	{
		(*env)->ReleaseStringUTFChars(env, (*env)->GetObjectArrayElement(env, kernelArray, i), kernels[i]);
	}

	for(i = 0; i < conduitsLength; i++)
	{
		(*env)->ReleaseStringUTFChars(env, (*env)->GetObjectArrayElement(env, conduitArray, i), conduits[i]);
	}

	timestamp = clockGet();

	for(i = 0; i < kernelsLength; i++)
	{
		OTF_Writer_writeBeginProcess (writer, clockGet() + 1, i + 1);
	}

	return 0;
}

JNIEXPORT int JNICALL Java_muscle_utilities_OTFLogger_end
(JNIEnv *env, jobject obj)
{
	int i = 0;
	
	for(i = 0; i < kernelsLength; i++)
	{
		OTF_Writer_writeEndProcess (writer, clockGet()+1, i+1);
	}
	OTF_HandlerArray_close(handlers);
	if( OTF_Writer_close(writer) != 1)
		return 1;

	OTF_FileManager_close(manager);
	
	return 0;
}

JNIEXPORT void JNICALL Java_muscle_utilities_OTFLogger_conduitBegin
(JNIEnv *env, jobject obj, jint function, jint process)
{
	OTF_Writer_writeEnter (writer, clockGet(), function + 1, process, 0);
}

JNIEXPORT void JNICALL Java_muscle_utilities_OTFLogger_conduitEnd
(JNIEnv *env, jobject obj, jint function, jint process)
{
	OTF_Writer_writeLeave (writer, clockGet(), function + 1, process, 0);
}

JNIEXPORT void JNICALL Java_muscle_utilities_OTFLogger_send
(JNIEnv *env, jobject obj, jint from, jint to, jint size)
{
	OTF_Writer_writeSendMsg(writer, clockGet(), from, to, 0, 0, size, 0);
}

JNIEXPORT void JNICALL Java_muscle_utilities_OTFLogger_receive
(JNIEnv *env, jobject obj, jint from, jint to , jint size)
{
	OTF_Writer_writeRecvMsg(writer, clockGet(), to, from, 0, 0, size, 0);
}

