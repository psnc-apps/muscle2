cmake_minimum_required(VERSION 2.6)
PROJECT(MUSCLE-FULL CXX)

# muscle core
add_subdirectory( src/cpp/muscle ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/muscle )
add_subdirectory( src/cpp/muscle2 ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/muscle2 )

# examples
add_subdirectory( src/cpp/examples/simplecpp ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/simplecpp )
add_subdirectory( src/cpp/examples/simplecpp2 ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/simplecpp2 )
add_subdirectory( src/cpp/examples/mpiring   ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/mpiring )
add_subdirectory( src/cpp/examples/transmutable ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/transmutable )
add_subdirectory( src/cpp/mto ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/mto )
