cmake_minimum_required (VERSION 2.6)

project (example_mpiring C)
set(MPIRING example_mpiring)
set(LHC example_LHC)
set(PSB example_PSB)

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
	
#find_library(MUSCLE2_LIBRARY ${MUSCLE2_LIB} NO_DEFAULT_PATH)
#if (${MUSCLE2_LIBRARY} STREQUAL "MUSCLE2_LIBRARY-NOTFOUND")
#	return()
#endif (${MUSCLE2_LIBRARY} STREQUAL "MUSCLE2_LIBRARY-NOTFOUND")


set (CMAKE_C_COMPILER ${MPI_COMPILER})

# load include dirs for muscle and jni
include_directories(${CMAKE_SOURCE_DIR}/src/cpp/muscle2)

# add our library that is built from the source files
add_library (${MPIRING} SHARED mpiringlib.c mpiringlib.h)
add_executable(${LHC} LHC.c mpiringlib.c mpiringlib.h)
add_executable(${PSB} PSB.c mpiringlib.c mpiringlib.h)

target_link_libraries (${MPIRING} ${MUSCLE2_LIB})
target_link_libraries (${LHC} ${MUSCLE2_LIB})
target_link_libraries (${PSB} ${MUSCLE2_LIB})

target_link_libraries(${MPIRING} ${MPI_LIBRARY}) 
target_link_libraries(${LHC} ${MPI_LIBRARY}) 
target_link_libraries(${PSB} ${MPI_LIBRARY}) 

#set_target_properties (${MPIRING} PROPERTIES LINKER_LANGUAGE C)
#set_target_properties (${LHC} PROPERTIES LINKER_LANGUAGE C)
#set_target_properties (${PSB} PROPERTIES LINKER_LANGUAGE C)
#set_target_properties (LHC PROPERTIES LINKER_LANGUAGE C)

if (MPI_FLAGS)
	message ( "Using custom MPI_FLAGS: " ${MPI_FLAGS})
	set_target_properties (${MPIRING} PROPERTIES COMPILE_FLAGS ${MPI_FLAGS})
	set_target_properties (${MPIRING} PROPERTIES LINK_FLAGS ${MPI_FLAGS})
	set_target_properties (${LHC} PROPERTIES COMPILE_FLAGS ${MPI_FLAGS})
	set_target_properties (${LHC} PROPERTIES LINK_FLAGS ${MPI_FLAGS})
	set_target_properties (${PSB} PROPERTIES COMPILE_FLAGS ${MPI_FLAGS})
	set_target_properties (${PSB} PROPERTIES LINK_FLAGS ${MPI_FLAGS})
endif()	

install(TARGETS ${MPIRING} ${LHC} ${PSB} DESTINATION share/muscle/examples/mpiring)

