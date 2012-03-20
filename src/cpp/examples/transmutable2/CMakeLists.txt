cmake_minimum_required(VERSION 2.6)
GET_FILENAME_COMPONENT(PARENT_DIR ${CMAKE_CURRENT_LIST_FILE} PATH)

project(transmutable2 CXX)

include_directories(../../muscle2)
link_directories(../../../../build)

add_executable(transmutable2 Kernel.cpp)
target_link_libraries (transmutable2 muscle2)
  
SET(INSTALL_DIR ${INSTALL_DIR} CACHE STRING "target dir where to put compiled results")
IF(INSTALL_DIR)
install(
    TARGETS transmutable2
    DESTINATION ${INSTALL_DIR}
)
ENDIF()
