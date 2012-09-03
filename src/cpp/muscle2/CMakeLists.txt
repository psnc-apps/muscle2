cmake_minimum_required(VERSION 2.6)

PROJECT(muscle2-cpp CXX C)

set(MUSCLE2CPP_VERSION "1.0")

FILE (GLOB THIS_HEADERS *.h *.hpp)
add_library(${MUSCLE2_LIB} SHARED cmuscle.cpp cppmuscle.cpp muscle_complex_data.cpp communicator.cpp logger.cpp xdr_communicator.cpp)
set_target_properties(${MUSCLE2_LIB} PROPERTIES VERSION ${MUSCLE2CPP_VERSION})
set_target_properties(${MUSCLE2_LIB} PROPERTIES OUTPUT_NAME muscle2)

install(TARGETS ${MUSCLE2_LIB} DESTINATION lib)
install(FILES ${THIS_HEADERS} DESTINATION include)
