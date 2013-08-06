PROJECT(muscle2-cpp CXX)

set(MUSCLE2UTIL_VERSION "1.0")

FILE (GLOB THIS_HEADERS *.h *.hpp)
add_library(${MUSCLE2UTIL_LIB} SHARED logger.cpp socket.cpp csocket.cpp async_service.cpp time.cpp endpoint.cpp option_parser.cpp)
set_target_properties(${MUSCLE2UTIL_LIB} PROPERTIES VERSION ${MUSCLE2UTIL_VERSION})
set_target_properties(${MUSCLE2UTIL_LIB} PROPERTIES OUTPUT_NAME muscle2util)

install(TARGETS ${MUSCLE2UTIL_LIB} DESTINATION lib)
install(FILES ${THIS_HEADERS} DESTINATION include/muscle2/util)
