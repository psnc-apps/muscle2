PROJECT(muscle2-cpp CXX C)

set(MUSCLE2CPP_VERSION "1.0")

FILE (GLOB THIS_HEADERS *.h *.hpp)
FILE (GLOB UTIL_HEADERS util/*.h util/*.hpp)
SET(MUSCLE2_CPP_FILES cmuscle.cpp fortranmuscle.cpp cppmuscle.cpp complex_data.cpp communicator.cpp xdr_communicator.cpp
        util/endpoint.cpp util/async_service.cpp util/msocket.cpp util/csocket.cpp util/mtime.cpp util/option_parser.cpp util/logger.cpp
		util/mutex.cpp util/rwmutex.cpp util/thread.cpp util/mbarrier.cpp)

add_library(${MUSCLE2_LIB} SHARED ${MUSCLE2_CPP_FILES})
set_target_properties(${MUSCLE2_LIB} PROPERTIES VERSION ${MUSCLE2CPP_VERSION})
set_target_properties(${MUSCLE2_LIB} PROPERTIES OUTPUT_NAME muscle2)

install(TARGETS ${MUSCLE2_LIB} DESTINATION lib)

if (BUILD_STATIC_LIBS)
	add_library(${MUSCLE2_LIB_STATIC} STATIC ${MUSCLE2_CPP_FILES})
	set_target_properties(${MUSCLE2_LIB_STATIC} PROPERTIES VERSION ${MUSCLE2CPP_VERSION})
	set_target_properties(${MUSCLE2_LIB_STATIC} PROPERTIES OUTPUT_NAME muscle2)
	install(TARGETS ${MUSCLE2_LIB_STATIC} DESTINATION lib)
endif (BUILD_STATIC_LIBS)

install(FILES ${THIS_HEADERS} DESTINATION include/muscle2)
install(FILES ${UTIL_HEADERS} DESTINATION include/muscle2/util)
