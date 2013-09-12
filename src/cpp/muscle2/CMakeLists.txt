PROJECT(muscle2-cpp CXX C)

set(MUSCLE2CPP_VERSION "1.0")

FILE (GLOB THIS_HEADERS *.h *.hpp)
FILE (GLOB UTIL_HEADERS util/*.h util/*.hpp)
add_library(${MUSCLE2_LIB} SHARED cmuscle.cpp fortranmuscle.cpp cppmuscle.cpp complex_data.cpp communicator.cpp xdr_communicator.cpp
        util/endpoint.cpp util/async_service.cpp util/msocket.cpp util/csocket.cpp util/mtime.cpp util/option_parser.cpp util/logger.cpp)
set_target_properties(${MUSCLE2_LIB} PROPERTIES VERSION ${MUSCLE2CPP_VERSION})
set_target_properties(${MUSCLE2_LIB} PROPERTIES OUTPUT_NAME muscle2)

install(TARGETS ${MUSCLE2_LIB} DESTINATION lib)
install(FILES ${THIS_HEADERS} DESTINATION include/muscle2)
install(FILES ${UTIL_HEADERS} DESTINATION include/muscle2/util)
