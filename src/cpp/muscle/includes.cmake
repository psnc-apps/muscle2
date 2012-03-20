#includes for the muscle core library


# make sure the compiler can find include files for Java JNI
INCLUDE(${CMAKE_SOURCE_DIR}/src/cpp/muscle/FindJNI.cmake)
include_directories(${JAVA_INCLUDE_PATH}) # for jni.h
include_directories(${JAVA_INCLUDE_PATH2}) # for jni_md.h

# add our src root
include_directories(${CMAKE_SOURCE_DIR}/src/cpp/muscle)
