cmake_minimum_required(VERSION 2.6)

PROJECT(musclecpp CXX)

SET(MUSCLECPP_VERSION "1.0")

# load our includes
INCLUDE(${CMAKE_CURRENT_SOURCE_DIR}/includes.cmake)

FILE(GLOB ALL_SOURCES
	*.h *.cpp *.c *.hpp
	access/*.h access/*.cpp	
	logging/*.h logging/*.cpp	
	)

FILE (GLOB THIS_HEADERS *.h *.hpp)
FILE (GLOB ACCESS_HEADERS access/*.h)
FILE (GLOB LOGGING_HEADERS logging/*.h)

# create our library
add_library(${MUSCLE_LIB} SHARED ${ALL_SOURCES})
set_target_properties(${MUSCLE_LIB} PROPERTIES VERSION ${MUSCLECPP_VERSION})
set_target_properties(${MUSCLE_LIB} PROPERTIES OUTPUT_NAME muscle)

install(TARGETS ${MUSCLE_LIB} DESTINATION lib)
install(FILES ${THIS_HEADERS} DESTINATION include)
install(FILES ${ACCESS_HEADERS} DESTINATION include/access)
install(FILES ${LOGGING_HEADERS} DESTINATION include/logging)
