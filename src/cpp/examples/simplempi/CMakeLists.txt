cmake_minimum_required(VERSION 2.6)
GET_FILENAME_COMPONENT(PARENT_DIR ${CMAKE_CURRENT_LIST_FILE} PATH)

PROJECT(example_simplempi CXX)

# find the muscle project, do not add pure relative paths
FIND_PATH(MUSCLE_SOURCE JNITool.h
	${PARENT_DIR}/../../../cpp/muscle
)

FIND_PACKAGE(MPI)

IF(NOT MPI_FOUND)
	message( "MPI has not been found")
ELSE()
	set(CMAKE_C_COMPILE_FLAGS ${CMAKE_C_COMPILE_FLAGS} ${MPI_COMPILE_FLAGS} --as-needed --disable-visibility)
	set(CMAKE_C_LINK_FLAGS ${CMAKE_C_LINK_FLAGS} ${MPI_LINK_FLAGS} --as-needed --disable-visibility)

	set(CMAKE_CXX_COMPILE_FLAGS ${CMAKE_CXX_COMPILE_FLAGS} ${MPI_COMPILE_FLAGS} --as-needed --disable-visibility)
	set(CMAKE_CXX_LINK_FLAGS ${CMAKE_CXX_LINK_FLAGS} ${MPI_LINK_FLAGS} --as-needed --disable-visibility)

	set(CMAKE_C_COMPILER ${MPI_COMPILER})
	set(CMAKE_CXX_COMPILER ${MPI_COMPILER})

	include_directories(MPI_INCLUDE_PATH)

	#
	SET(MUSCLE_LIB_NAME muscle CACHE STRING "base name of the muscle core lib")
	SET(EXTERNAL_MUSCLE_LIB ON CACHE BOOL "link to an already compiled muscle lib, else build it within this project")
	IF(EXTERNAL_MUSCLE_LIB)
		FIND_FILE(MUSCLE_LIB_DIR build
			${PARENT_DIR}/../../../../
		)
		FIND_LIBRARY(MUSCLE_LIB_NAME ${MUSCLE_LIB_NAME}
			PATHS ${MUSCLE_LIB_DIR}
		)
	ELSE()
		SET( MUSCLE_LIB_DIR ${${CMAKE_PROJECT_NAME}_BINARY_DIR})
		# also compile the muscle core
		add_subdirectory (${MUSCLE_SOURCE} ${MUSCLE_LIB_DIR})
	ENDIF()

	# load include dirs for muscle and jni
	INCLUDE(${MUSCLE_SOURCE}/includes.cmake)

	# make sure the linker can find the muscle library once it is built
	link_directories (${MUSCLE_LIB_DIR})


	# add our library that is built from the source files
	SET(LIB_NAME simplempi)
	add_library (${LIB_NAME} SHARED Test.cpp)
	SET_TARGET_PROPERTIES(${LIB_NAME} PROPERTIES LINKER_LANGUAGE CXX)
	# link the executable to the muscle library.
	target_link_libraries (${LIB_NAME} ${MUSCLE_LIB_NAME})
	target_link_libraries(${LIB_NAME} ${MPI_LIBRARIES}) 

	#
	SET(INSTALL_DIR ${MUSCLE_LIB_DIR} CACHE STRING "target dir where to put compiled results")
	IF(INSTALL_DIR)
		install(
			TARGETS ${LIB_NAME}
			DESTINATION ${INSTALL_DIR}
		)
	ENDIF()
ENDIF()
