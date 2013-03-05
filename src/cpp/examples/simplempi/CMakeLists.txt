project (example_simplempi C)
set(SENDER mpisender)

set (CMAKE_C_COMPILER ${MPI_C_COMPILER})

# load include dirs for muscle and jni
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")

# add our library that is built from the source files
add_executable(${SENDER} sender.c)

target_link_libraries (${SENDER} ${MUSCLE2_LIB})
target_link_libraries(${SENDER} ${MPI_LIBRARY})

if (MPI_C_FLAGS)
	message ( "Using custom MPI_FLAGS: " ${MPI_C_FLAGS})
	set_target_properties (${SENDER} PROPERTIES COMPILE_FLAGS ${MPI_C_FLAGS})
	set_target_properties (${SENDER} PROPERTIES LINK_FLAGS ${MPI_C_FLAGS})
endif()

install(TARGETS ${SENDER} DESTINATION share/muscle/examples/simplempi)
