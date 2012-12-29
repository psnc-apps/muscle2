cmake_minimum_required(VERSION 2.6)
PROJECT(MUSCLE Java) # The project has a name MUSCLE and utilizes CXX compiler

set(JavaMuscle_DIR "${CMAKE_SOURCE_DIR}/cmake_scripts")
find_package(JavaMuscle COMPONENTS Runtime Development REQUIRED NO_MODULE)

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    set(CMAKE_JAVA_COMPILE_FLAGS -g:none -nowarn)
endif()
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_JAVA_COMPILE_FLAGS -Xlint:all -g -Xlint:-rawtypes)
endif()

include(${CMAKE_SOURCE_DIR}/cmake_scripts/UseJavaMuscle.cmake)

FILE(GLOB THIRDPARTY_JAR ${CMAKE_SOURCE_DIR}/thirdparty/*.jar) 
FILE(GLOB CMAKE_JAVA_INCLUDE_PATH ${CMAKE_SOURCE_DIR}/thirdparty/*.jar ${CMAKE_SOURCE_DIR}/src/cpp/examples/mpiring/*.jar) 

FILE(GLOB_RECURSE ALL_SOURCES RELATIVE ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_SOURCE_DIR}/*.java)  

add_jar(muscle ${ALL_SOURCES})

install_jar(muscle share/muscle/java)
install(FILES ${THIRDPARTY_JAR} DESTINATION share/muscle/java/thirdparty)
