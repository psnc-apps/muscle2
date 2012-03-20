cmake_minimum_required(VERSION 2.6)
GET_FILENAME_COMPONENT(PARENT_DIR ${CMAKE_CURRENT_LIST_FILE} PATH)

project(example_transmutable2 CXX)

set(CMAKE_BUILD_TYPE Debug)
find_library(MUSCLE2_LIBRARY ${MUSCLE2_LIB} NO_DEFAULT_PATH)

if (NOT ${MUSCLE2_LIBRARY} STREQUAL "MUSCLE2_LIBRARY-NOTFOUND")
	add_executable(${PROJECT_NAME} Kernel.cpp)
	include_directories(${CMAKE_SOURCE_DIR}/src/cpp/muscle2)
	target_link_libraries(${PROJECT_NAME} ${MUSCLE2_LIB})

	install(TARGETS ${PROJECT_NAME} DESTINATION share/muscle/examples/transmutable2)
endif (NOT ${MUSCLE2_LIBRARY} STREQUAL "MUSCLE2_LIBRARY-NOTFOUND")
