PROJECT(MUSCLE Java) # The project has a name MUSCLE and utilizes CXX compiler

find_package(Java COMPONENTS Runtime Development REQUIRED)

if (CMAKE_BUILD_TYPE STREQUAL "Release")
    set(CMAKE_JAVA_COMPILE_FLAGS -g:none -nowarn)
endif()
if (CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_JAVA_COMPILE_FLAGS -Xlint:all -g -Xlint:-rawtypes)
endif()

include(UseJava)

FILE(GLOB THIRDPARTY_JAR ${CMAKE_SOURCE_DIR}/thirdparty/*.jar) 
FILE(GLOB CMAKE_JAVA_INCLUDE_PATH ${CMAKE_SOURCE_DIR}/thirdparty/*.jar ${CMAKE_SOURCE_DIR}/src/cpp/examples/mpiring/*.jar) 

FILE(GLOB_RECURSE ALL_SOURCES RELATIVE ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_SOURCE_DIR}/*.java)  
FILE(GLOB_RECURSE ALL_RESOURCES RELATIVE ${CMAKE_CURRENT_SOURCE_DIR} ${CMAKE_CURRENT_SOURCE_DIR}/*.properties)  

add_jar(muscle ${ALL_SOURCES} ${ALL_RESOURCES})

install_jar(muscle share/muscle/java)
install(FILES ${THIRDPARTY_JAR} DESTINATION share/muscle/java/thirdparty)
