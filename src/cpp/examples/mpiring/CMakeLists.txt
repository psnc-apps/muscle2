cmake_minimum_required (VERSION 2.6)

project (example_mpiring C)
set(MPIRING mpiring)
set(LHC LHC)
set(PSB PSB)
FILE (GLOB THIS_JARS *.jar)

set (CMAKE_C_COMPILER ${MPI_C_COMPILER})

# load include dirs for muscle and jni
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")

# add our library that is built from the source files
add_library (${MPIRING} SHARED mpiringlib.c mpiringlib.h)
add_executable(${LHC} LHC.c mpiringlib.c mpiringlib.h)
add_executable(${PSB} PSB.c mpiringlib.c mpiringlib.h)

target_link_libraries (${MPIRING} ${MUSCLE2_LIB})
target_link_libraries (${LHC} ${MUSCLE2_LIB})
target_link_libraries (${PSB} ${MUSCLE2_LIB})

target_link_libraries(${MPIRING} ${MPI_C_LIBRARIES}) 
target_link_libraries(${LHC} ${MPI_C_LIBRARIES}) 
target_link_libraries(${PSB} ${MPI_C_LIBRARIES}) 

#set_target_properties (${MPIRING} PROPERTIES LINKER_LANGUAGE C)
#set_target_properties (${LHC} PROPERTIES LINKER_LANGUAGE C)
#set_target_properties (${PSB} PROPERTIES LINKER_LANGUAGE C)
#set_target_properties (LHC PROPERTIES LINKER_LANGUAGE C)

if (MPI_C_FLAGS)
	message ( "Using custom MPI_FLAGS: " ${MPI_C_FLAGS})
	set_target_properties (${MPIRING} PROPERTIES COMPILE_FLAGS ${MPI_C_FLAGS})
	set_target_properties (${MPIRING} PROPERTIES LINK_FLAGS ${MPI_C_FLAGS})
	set_target_properties (${LHC} PROPERTIES COMPILE_FLAGS ${MPI_C_FLAGS})
	set_target_properties (${LHC} PROPERTIES LINK_FLAGS ${MPI_C_FLAGS})
	set_target_properties (${PSB} PROPERTIES COMPILE_FLAGS ${MPI_C_FLAGS})
	set_target_properties (${PSB} PROPERTIES LINK_FLAGS ${MPI_C_FLAGS})
endif()	

install(TARGETS ${MPIRING} ${LHC} ${PSB} DESTINATION share/muscle/examples/mpiring)
install(FILES ${THIS_JARS} DESTINATION share/muscle/examples/lib)
