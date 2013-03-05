project(example_simplecpp2 CXX)

add_executable(simplecpp2 Sender.cpp)
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")
target_link_libraries (simplecpp2  ${MUSCLE2_LIB})
install(TARGETS simplecpp2 DESTINATION share/muscle/examples/simplecpp2)
