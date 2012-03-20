cmake_minimum_required(VERSION 2.6)

PROJECT(example_simplecpp CXX)

# find the muscle project, do not add pure relative paths
set (MUSCLE_SOURCE ${CMAKE_SOURCE_DIR}/src/cpp/muscle)

# load include dirs for muscle and jni
INCLUDE(${MUSCLE_SOURCE}/includes.cmake)

# make sure the linker can find the muscle library once it is built
link_directories (${MUSCLE_LIB_DIR})

# add our library that is built from the source files
SET(LIB_NAME ${PROJECT_NAME}_lib)
add_library (${LIB_NAME} SHARED Sender.cpp)
SET_TARGET_PROPERTIES(${LIB_NAME} PROPERTIES LINKER_LANGUAGE CXX INSTALL_NAME_DIR "")
# link the executable to the muscle library.
target_link_libraries (${LIB_NAME} ${MUSCLE_LIB})

install(TARGETS ${LIB_NAME} DESTINATION share/muscle/examples/simplecpp)
