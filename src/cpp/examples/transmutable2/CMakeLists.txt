project(example_transmutable2 CXX)

add_executable(transmutable2 Kernel.cpp)
include_directories("${CMAKE_SOURCE_DIR}/src/cpp")
target_link_libraries(transmutable2 ${MUSCLE2_LIB})
install(TARGETS transmutable2 DESTINATION share/muscle/examples/transmutable2)
