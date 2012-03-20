cmake_minimum_required(VERSION 2.6)

PROJECT(muscle2)

set(Boost_USE_STATIC_LIBS        ON)
set(Boost_USE_MULTITHREADED      ON)
set(Boost_USE_STATIC_RUNTIME     ON)

find_package(Boost COMPONENTS system)
find_package(Threads)

if(NOT Boost_FOUND)
    message("Boost has not been found! MUSCLE C++ will not be built")
elseif (${Boost_MINOR_VERSION} LESS 34)
    message("Boost version ${Boost_VERSION} found is too old! MTO will not be built")
else(NOT Boost_FOUND)
	message("Building MUSCLE C/C++ API")
	set(CMAKE_CXX_FLAGS " ${CMAKE_CXX_FLAGS} -g -Wall -fPIC")
	add_library(${PROJECT_NAME} SHARED cmuscle.cpp cmuscle.h cppmuscle.cpp cppmuscle.hpp muscle_types.h)
	include_directories(${Boost_INCLUDE_DIRS})
	target_link_libraries(${PROJECT_NAME} ${Boost_LIBRARIES} )
	target_link_libraries(${PROJECT_NAME} ${CMAKE_THREAD_LIBS_INIT} )
	
	# install
	GET_FILENAME_COMPONENT(PARENT_DIR ${CMAKE_CURRENT_LIST_FILE} PATH)

	FIND_FILE(INSTALL_DIR build
		${PARENT_DIR}/../../../
	)

	SET(INSTALL_DIR ${INSTALL_DIR} CACHE STRING "target dir where to put compiled results")
	IF(INSTALL_DIR)
		install(
			TARGETS ${PROJECT_NAME}
			DESTINATION ${INSTALL_DIR})
	ENDIF(INSTALL_DIR)

endif(NOT Boost_FOUND)
