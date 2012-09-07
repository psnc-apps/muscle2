cmake_minimum_required(VERSION 2.6)
PROJECT(MUSCLE)

option(BUILD_CPP "Build CPP part of MUSCLE" ON)
option(BUILD_CPP_MUSCLE1 "Build MUSCLE1 part of CPP" ON)
option(BUILD_MTO "Build Muscle Transport Overlay service" ON)

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

set(MUSCLE_LIB muscle_cpp)
set(MUSCLE2_LIB muscle2_cpp)
set(MUSCLE_JAVA muscle_java)

if (BUILD_JAVA)
	add_subdirectory(src/java)
endif (BUILD_JAVA)

if (BUILD_CPP)
	if (BUILD_CPP_MUSCLE1)
		# muscle1 CPP core 
		add_subdirectory(src/cpp/muscle)
		# Examples CPP
		add_subdirectory(src/cpp/examples/simplecpp)
		add_subdirectory(src/cpp/examples/transmutable)
	endif (BUILD_CPP_MUSCLE1)
	# muscle2 CPP core 
	add_subdirectory(src/cpp/muscle2)
	# Examples CPP
	add_subdirectory(src/cpp/examples/simplecpp2)
	add_subdirectory(src/cpp/examples/pingpongcpp)
	add_subdirectory(src/cpp/examples/dataintegrity)
	add_subdirectory(src/cpp/examples/mpiring)
	add_subdirectory(src/cpp/examples/transmutable2)
endif (BUILD_CPP)

if (BUILD_MTO)
	add_subdirectory(src/cpp/mto)
endif (BUILD_MTO)

# Examples CXA
add_subdirectory(src/cxa)

add_subdirectory(src/resources)
add_subdirectory(src/ruby)
# Helper scripts
add_subdirectory(scripts)
add_subdirectory(doc)

if (BUILD_CPACK_SCRIPTS)
	include(cmake_scripts/CPackMuscle.cmake)
endif (BUILD_CPACK_SCRIPTS)

include(test/regression/CMakeLists.txt)
install(FILES README.md DESTINATION share/muscle)
