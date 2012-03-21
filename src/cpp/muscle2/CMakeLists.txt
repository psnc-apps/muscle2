cmake_minimum_required(VERSION 2.6)

PROJECT(muscle2-cpp CXX C)

set(MUSCLE2CPP_VERSION "1.0")

find_package(Threads)

set(Boost_USE_STATIC_LIBS        OFF)
set(Boost_USE_MULTITHREADED      ON)
set(Boost_USE_STATIC_RUNTIME     ON)

set (Boost_DIR "" CACHE PATH "Location of the directory with CMake config for Boost")

if (Boost_DIR STREQUAL "") # if we don't specify Boost CMakeConfig file location we are looking for the Boost newer than 1.36
    message(STATUS "Boost search based on CMake Modules")
    set(Boost_NO_BOOST_CMAKE ON)
    find_package(Boost 1.36 COMPONENTS system)
else (Boost_DIR STREQUAL "") # otherwise we use specific CMakeConfig file and don't worry about version
    message(STATUS "Boost search based on CMake config provided in ${Boost_DIR}")
    find_package(Boost COMPONENTS system NO_MODULE)
endif(Boost_DIR STREQUAL "")

if(NOT Boost_FOUND)
    message(WARNING "Boost has not been found! MUSCLE C++ will not be built")
    set(Boost_INCLUDE_DIR "" CACHE STRING "Boost include directory" FORCE) 
#    unset(Boost_INCLUDE_DIR CACHE)
else (NOT Boost_FOUND)
    FILE (GLOB THIS_HEADERS *.h *.hpp)
    set(CMAKE_CXX_FLAGS " ${CMAKE_CXX_FLAGS} -g -Wall")
    add_library(${MUSCLE2_LIB} SHARED cmuscle.cpp cmuscle.h cppmuscle.cpp cppmuscle.hpp muscle_types.h)
    include_directories(${Boost_INCLUDE_DIRS})
    target_link_libraries(${MUSCLE2_LIB} ${Boost_LIBRARIES} )
    target_link_libraries(${MUSCLE2_LIB} ${CMAKE_THREAD_LIBS_INIT} )	
    set_target_properties(${MUSCLE2_LIB} PROPERTIES VERSION ${MUSCLE2CPP_VERSION})
    install(TARGETS ${MUSCLE2_LIB} DESTINATION lib)
    install(FILES ${THIS_HEADERS} DESTINATION include)
endif(NOT Boost_FOUND)
