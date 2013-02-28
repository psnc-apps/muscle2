cmake_minimum_required (VERSION 2.6)

project (example_simplempi C)
set(SENDER mpisender)

if (NOT MPI_COMPILER)
	message ( STATUS "Searching for available MPI implementations" )
	find_package (MPI)
	message (STATUS "Found MPI Compiler: " ${MPI_COMPILER} )
else()
	message (STATUS "Using provided MPI Compiler: " ${MPI_COMPILER} )
	set (MPI_FOUND TRUE)
endif()
	
if (NOT MPI_FOUND)
	message ("MPI has not been found")
	return()
endif (NOT MPI_FOUND)

set (CMAKE_C_COMPILER ${MPI_COMPILER})

# load include dirs for muscle and jni
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")

# add our library that is built from the source files
add_executable(${SENDER} sender.c)

target_link_libraries (${SENDER} ${MUSCLE2_LIB})
target_link_libraries(${SENDER} ${MPI_LIBRARY}) 

if (MPI_FLAGS)
	message ( "Using custom MPI_FLAGS: " ${MPI_FLAGS})
	set_target_properties (${SENDER} PROPERTIES COMPILE_FLAGS ${MPI_FLAGS})
	set_target_properties (${SENDER} PROPERTIES LINK_FLAGS ${MPI_FLAGS})
endif()	

install(TARGETS ${SENDER} DESTINATION share/muscle/examples/simplempi)
