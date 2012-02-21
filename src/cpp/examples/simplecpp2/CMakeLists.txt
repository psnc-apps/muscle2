cmake_minimum_required(VERSION 2.6)

project(simplecpp2 CXX)

set(CMAKE_BUILD_TYPE Debug)



include_directories(../../muscle2)
link_directories(../../../../build)

add_executable(simplecpp2 Sender.cpp)
target_link_libraries (simplecpp2 muscle2)

  
SET(INSTALL_DIR ${INSTALL_DIR} CACHE STRING "target dir where to put compiled results")
IF(INSTALL_DIR)
install(
    TARGETS simplecpp2
    DESTINATION ${INSTALL_DIR}
)
ENDIF()

