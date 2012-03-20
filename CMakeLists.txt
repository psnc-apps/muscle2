cmake_minimum_required(VERSION 2.6)
PROJECT(MUSCLE)

#option(FORCE_INSTALLATION_IN_OPT "Force installation in /opt/muscle regardeless of other settings" ON)
option(BUILD_CPP "Build CPP part of MUSCLE" ON)
option(BUILD_JAVA "Build Java part of MUSCLE" ON)
option(BUILD_MTO "Build Muscle Transport Overlay service" ON)
option(BUILD_CPACK_SCRIPTS "Build Muscle Transport Overlay service" OFF)


SET(MUSCLE_INSTALL_PREFIX "/opt/muscle" CACHE PATH "MUSCLE install prefix. Set to the empty value for a default system location")

IF(NOT MUSCLE_INSTALL_PREFIX STREQUAL "")
	SET(CMAKE_INSTALL_PREFIX ${MUSCLE_INSTALL_PREFIX})
ENDIF(NOT MUSCLE_INSTALL_PREFIX STREQUAL "")

#IF(FORCE_INSTALLATION_IN_OPT)
#	IF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
#		SET(CMAKE_INSTALL_PREFIX "/opt/muscle" CACHE PATH "MUSCLE install prefix" FORCE)
#	ENDIF(CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT)
#ENDIF(FORCE_INSTALLATION_IN_OPT)

SET(CMAKE_INSTALL_NAME_DIR "${CMAKE_INSTALL_PREFIX}/lib") #this is importat for MAC OS Dynamic Library Linking
SET(CMAKE_INSTALL_RPATH "${CMAKE_INSTALL_PREFIX}/lib")
SET(CMAKE_INSTALL_RPATH_USE_LINK_PATH TRUE)

SET(MUSCLE_LIB muscle_cpp)
SET(MUSCLE2_LIB muscle2_cpp)
SET(MUSCLE_JAVA muscle_java)

if (BUILD_JAVA)
	add_subdirectory(src/java)
endif (BUILD_JAVA)

if (BUILD_CPP)
	# muscle CPP core 
	add_subdirectory(src/cpp/muscle)
	add_subdirectory(src/cpp/muscle2)
	# Examples CPP
	add_subdirectory(src/cpp/examples/simplecpp)
	add_subdirectory(src/cpp/examples/simplecpp2)
	add_subdirectory(src/cpp/examples/mpiring)
	add_subdirectory(src/cpp/examples/transmutable)
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
	INCLUDE(cmake_scripts/CPackMuscle.cmake)
endif (BUILD_CPACK_SCRIPTS)

#INCLUDE(cmake_scripts/VariableLogging.cmake)
