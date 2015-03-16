project(example_simplecpp2 CXX)

add_executable(simplecpp2 Sender.cpp)
add_executable(simplecpp2r Receiver.cpp)
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")
target_link_libraries (simplecpp2  ${MUSCLE2_LIB})
target_link_libraries (simplecpp2r  ${MUSCLE2_LIB})
install(TARGETS simplecpp2 simplecpp2r DESTINATION share/muscle/examples/simplecpp2)
