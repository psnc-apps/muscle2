cmake_minimum_required(VERSION 2.6)
GET_FILENAME_COMPONENT(PARENT_DIR ${CMAKE_CURRENT_LIST_FILE} PATH)

project(example_transmutable2 CXX)

add_executable(transmutable2 Kernel.cpp)
include_directories(${CMAKE_SOURCE_DIR}/src/cpp/muscle2)
target_link_libraries(transmutable2 ${MUSCLE2_LIB})
install(TARGETS transmutable2 DESTINATION share/muscle/examples/transmutable2)
