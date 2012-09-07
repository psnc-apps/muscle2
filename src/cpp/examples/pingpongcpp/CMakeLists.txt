cmake_minimum_required(VERSION 2.6)
project(example_pingpongcpp CXX)

add_executable(ping Ping.cpp)
add_executable(pong Pong.cpp)
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")
target_link_libraries (ping  ${MUSCLE2_LIB})
target_link_libraries (pong  ${MUSCLE2_LIB})
install(TARGETS ping pong DESTINATION share/muscle/examples/pingpongcpp)

