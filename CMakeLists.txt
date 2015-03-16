cmake_minimum_required(VERSION 2.8.3)

PROJECT(MUSCLE)

option(BUILD_CPP "Build C/C++ part of MUSCLE" ON)
option(BUILD_FORTRAN "Build FORTRAN part of MUSCLE (depends on C++)" ON)
option(BUILD_PYTHON "Build PYTHON part of MUSCLE (depends on C++)" ON)
option(BUILD_MATLAB "Build MATLAB part of MUSCLE (depends on C++)" OFF)
option(BUILD_EXAMPLES "Build example models" ON)
option(BUILD_MPI_EXAMPLES "Build MPI examples (depends on C++)" ON)
option(BUILD_MTO "Build Muscle Transport Overlay service" ON)
option(BUILD_STATIC_MTO "Link Muscle Transport Overlay to static libraries" OFF)
option(BUILD_TEST "Build tests" OFF)
option(BUILD_STATIC_LIBS "Build additional static MUSCLE library" OFF)

# Check if the dependencies are correctly set
if(BUILD_FORTRAN AND NOT BUILD_CPP)
    message( WARNING "BUILD_FORTRAN requires BUILD_CPP. Enabling the disabled BUILD_CPP." )
    set(BUILD_CPP ON CACHE BOOL ON FORCE)
endif()
if (BUILD_PYTHON AND NOT BUILD_CPP)
    message( WARNING "BUILD_PYTHON requires BUILD_CPP. Enabling the disabled BUILD_CPP." )
    set(BUILD_CPP ON CACHE BOOL ON FORCE)
endif()
if (BUILD_MPI_EXAMPLES AND NOT BUILD_CPP)
    message( WARNING "BUILD_MPI_EXAMPLES requires BUILD_CPP. Enabling the disabled BUILD_CPP." )
    set(BUILD_CPP ON CACHE BOOL ON FORCE)
endif()
if (BUILD_MPI_EXAMPLES AND NOT BUILD_EXAMPLES)
    message( WARNING "BUILD_MPI_EXAMPLES requires BUILD_EXAMPLES. Enabling the disabled BUILD_EXAMPLES." )
    set(BUILD_EXAMPLES ON CACHE BOOL ON FORCE)
endif()
if (BUILD_MTO AND NOT BUILD_CPP)
    message( WARNING "BUILD_MTO requires BUILD_CPP. Enabling the disabled BUILD_CPP." )
    set(BUILD_CPP ON CACHE BOOL ON FORCE)
endif()

if (BUILD_STATIC_MTO AND NOT BUILD_STATIC_LIBS)
    message( WARNING "BUILD_STATIC_MTO requires BUILD_STATIC_LIBS. Enabling the disabled BUILD_STATIC_LIBS." )
    set(BUILD_STATIC_LIBS ON CACHE BOOL ON FORCE)
endif()
# end check if the dependencies are correctly set

option(BUILD_CPACK_SCRIPTS "Build scripts for packaging" OFF)
mark_as_advanced(BUILD_CPACK_SCRIPTS)
option(BUILD_JAVA "Build Java part of MUSCLE" ON)
mark_as_advanced(BUILD_JAVA)

set(MUSCLE_INSTALL_PREFIX "/opt/muscle" CACHE PATH "MUSCLE install prefix. Set to the empty value for a default system location")

if(NOT MUSCLE_INSTALL_PREFIX STREQUAL "")
	set(CMAKE_INSTALL_PREFIX ${MUSCLE_INSTALL_PREFIX})
endif(NOT MUSCLE_INSTALL_PREFIX STREQUAL "")

set(CMAKE_INSTALL_NAME_DIR "${CMAKE_INSTALL_PREFIX}/lib") #this is importat for MAC OS Dynamic Library Linking
set(CMAKE_INSTALL_RPATH "${CMAKE_INSTALL_PREFIX}/lib")
set(CMAKE_INSTALL_RPATH_USE_LINK_PATH TRUE)

set(MUSCLE2_LIB muscle2_cpp)
if (BUILD_STATIC_LIBS)
	set(MUSCLE2_LIB_STATIC muscle2_cpp_static)
	set(MPWIDE_LIB_STATIC mpwide_cpp_static)
endif()
set(MUSCLE2_PYTHON muscle2_python)
set(MUSCLE_JAVA muscle_java)
set(MPWIDE_LIB mpwide)

if (BUILD_JAVA)
	add_subdirectory(src/java)
endif (BUILD_JAVA)

if (BUILD_CPP)
    if (BUILD_TEST)
	    add_subdirectory(test/cpp)
    endif (BUILD_TEST)

	# muscle2 CPP core 
	add_subdirectory(src/cpp/muscle2)
	add_subdirectory(src/cpp/mpimuscle)
    if (BUILD_EXAMPLES)
    	# Examples CPP
    	add_subdirectory(src/cpp/examples/simplecpp2)
    	add_subdirectory(src/cpp/examples/pingpongcpp)
    	add_subdirectory(src/cpp/examples/dataintegrity)
    	add_subdirectory(src/cpp/examples/transmutable2)
    endif (BUILD_EXAMPLES)
endif (BUILD_CPP)

if (BUILD_MTO)
	add_subdirectory(src/cpp/mpwide)
	add_subdirectory(src/cpp/mto)
endif (BUILD_MTO)

if (BUILD_FORTRAN AND BUILD_EXAMPLES)
	add_subdirectory(src/fortran/examples/simplefortran)
endif ()

if (BUILD_MPI_EXAMPLES)
	# Get MPI variables from the command-line
	if (NOT DEFINED MPI_C_FLAGS AND DEFINED ENV{MPI_C_FLAGS})
		set(MPI_C_FLAGS $ENV{MPI_C_FLAGS})
	endif()
	if (NOT DEFINED MPI_C_FLAGS AND DEFINED ENV{MPI_FLAGS})
		set(MPI_C_FLAGS $ENV{MPI_FLAGS})
	endif()
	if (NOT DEFINED MPI_C_COMPILER AND DEFINED ENV{MPI_C_COMPILER})
		set(MPI_C_COMPILER $ENV{MPI_C_COMPILER})
	endif()
	if (NOT DEFINED MPI_C_COMPILER AND DEFINED ENV{MPI_COMPILER})
		set(MPI_C_COMPILER $ENV{MPI_COMPILER})
	endif()
	if (NOT DEFINED MPI_C_LIBRARIES AND DEFINED ENV{MPI_C_LIBRARIES})
		set(MPI_C_LIBRARIES $ENV{MPI_C_LIBRARIES})
	endif()
	if (NOT DEFINED MPI_C_INCLUDE_PATH AND DEFINED ENV{MPI_C_INCLUDE_PATH})
		set(MPI_C_INCLUDE_PATH $ENV{MPI_C_INCLUDE_PATH})
	endif()
	
	message ( STATUS "Searching for available MPI implementations" )

	find_package(MPI)

	if (MPI_C_FOUND)
		message (STATUS "Found MPI Compiler: " ${MPI_C_COMPILER} )
        add_subdirectory(src/cpp/examples/simplempi)
	    add_subdirectory(src/cpp/examples/mpiring)
	else(MPI_C_FOUND)
		message ("MPI has not been found; not building MPI examples")
	endif (MPI_C_FOUND)
endif (BUILD_MPI_EXAMPLES)

if (BUILD_PYTHON)
    add_subdirectory(src/python/muscle)
    if (BUILD_EXAMPLES)
        add_subdirectory(src/python/examples/simplepython)
    endif (BUILD_EXAMPLES)
endif (BUILD_PYTHON)

if (BUILD_MATLAB)
    add_subdirectory(src/matlab/muscle)
    if (BUILD_EXAMPLES)
        add_subdirectory(src/matlab/examples/simplematlab)
    endif (BUILD_EXAMPLES)
endif (BUILD_MATLAB)
    
# Examples CXA
if (BUILD_EXAMPLES)
    add_subdirectory(src/cxa)
endif (BUILD_EXAMPLES)

add_subdirectory(src/resources)
add_subdirectory(src/ruby)
# Helper scripts
add_subdirectory(scripts)
add_subdirectory(doc)

if (BUILD_CPACK_SCRIPTS)
	include(CPackMuscle)
endif (BUILD_CPACK_SCRIPTS)

include(test/regression/CMakeLists.txt)
install(FILES README.md DESTINATION share/muscle)
