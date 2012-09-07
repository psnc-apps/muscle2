cmake_minimum_required(VERSION 2.6)
project(example_dataintegrity CXX)

add_executable(check Check.cpp)
add_executable(bounce Bounce.cpp)
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")
target_link_libraries (check  ${MUSCLE2_LIB})
target_link_libraries (bounce  ${MUSCLE2_LIB})
install(TARGETS check bounce DESTINATION share/muscle/examples/dataintegrity)

