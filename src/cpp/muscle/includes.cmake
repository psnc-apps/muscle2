#includes for the muscle core library

GET_FILENAME_COMPONENT(PARENT_DIR ${CMAKE_CURRENT_LIST_FILE} PATH)

# make sure the compiler can find include files for Java JNI
INCLUDE(${PARENT_DIR}/FindJNI.cmake)
include_directories(${JAVA_INCLUDE_PATH}) # for jni.h
include_directories(${JAVA_INCLUDE_PATH2}) # for jni_md.h

# add our src root
include_directories(${PARENT_DIR})
