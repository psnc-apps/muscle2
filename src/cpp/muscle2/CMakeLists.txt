PROJECT(muscle2-cpp CXX C)

set(MUSCLE2CPP_VERSION "1.0")

FILE (GLOB THIS_HEADERS *.h *.hpp)

# exclude muscle_perf.h by default
get_filename_component(full_path_muscle_perf_h ${CMAKE_CURRENT_SOURCE_DIR}/muscle_perf.h ABSOLUTE)
list(REMOVE_ITEM THIS_HEADERS "${full_path_muscle_perf_h}")

FILE (GLOB UTIL_HEADERS util/*.h util/*.hpp)
SET(MUSCLE2_CPP_FILES cmuscle.cpp fortranmuscle.cpp cppmuscle.cpp complex_data.cpp communicator.cpp xdr_communicator.cpp custom_communicator.cpp
        util/endpoint.cpp util/async_service.cpp util/msocket.cpp util/csocket.cpp util/mtime.cpp util/option_parser.cpp util/logger.cpp
	util/custom_serializer.cpp
		util/mutex.cpp util/rwmutex.cpp util/thread.cpp util/barrier.cpp)

if (BUILD_PERF)
    LIST(APPEND MUSCLE2_CPP_FILES muscle_perf.cpp)
    LIST(APPEND THIS_HEADERS ${full_path_muscle_perf_h})
    add_definitions(-DCPPMUSCLE_PERF)
endif (BUILD_PERF)

find_package(Threads)

add_library(${MUSCLE2_LIB} SHARED ${MUSCLE2_CPP_FILES})
target_link_libraries(${MUSCLE2_LIB} ${CMAKE_THREAD_LIBS_INIT} )
set_target_properties(${MUSCLE2_LIB} PROPERTIES VERSION ${MUSCLE2CPP_VERSION})
set_target_properties(${MUSCLE2_LIB} PROPERTIES OUTPUT_NAME muscle2)

install(TARGETS ${MUSCLE2_LIB} DESTINATION lib)

if (BUILD_STATIC_LIBS)
	add_library(${MUSCLE2_LIB_STATIC} STATIC ${MUSCLE2_CPP_FILES})
	target_link_libraries(${MUSCLE2_LIB_STATIC} ${CMAKE_THREAD_LIBS_INIT} )
	set_target_properties(${MUSCLE2_LIB_STATIC} PROPERTIES VERSION ${MUSCLE2CPP_VERSION})
	set_target_properties(${MUSCLE2_LIB_STATIC} PROPERTIES OUTPUT_NAME muscle2)
	install(TARGETS ${MUSCLE2_LIB_STATIC} DESTINATION lib)
endif (BUILD_STATIC_LIBS)

install(FILES ${THIS_HEADERS} DESTINATION include/muscle2)
install(FILES ${UTIL_HEADERS} DESTINATION include/muscle2/util)
