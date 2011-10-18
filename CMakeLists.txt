cmake_minimum_required(VERSION 2.6)
PROJECT(MUSCLE-FULL CXX)

# muscle core
add_subdirectory( src/cpp/muscle ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/muscle )

# examples
add_subdirectory( src/cpp/examples/simplecpp ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/simplecpp )
add_subdirectory( src/cpp/examples/simplempi ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/simplempi )
add_subdirectory( src/cpp/examples/transmutable ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/transmutable )
add_subdirectory( src/cpp/mto ${${CMAKE_PROJECT_NAME}_BINARY_DIR}/mto )