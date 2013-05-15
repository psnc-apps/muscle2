project(mto)

set(MTO mto)
set(TESTER mto_tester)

#set(Boost_USE_STATIC_LIBS        ON)
#set(Boost_USE_MULTITHREADED      OFF)
#set(Boost_USE_STATIC_RUNTIME     ON)

#set(CMAKE_BUILD_TYPE Debug)

#find_package(Threads)

#set (Boost_DIR "" CACHE PATH "Location of the directory with CMake config for Boost")

#set (Boost_COMPILER "clang")

#if (Boost_DIR STREQUAL "") # if we don't specify Boost CMakeConfig file location we are looking for the Boost newer than 1.36
	#message(STATUS "Boost search based on CMake Modules")
	#set(Boost_NO_BOOST_CMAKE ON)
	#find_package(Boost 1.35 COMPONENTS system program_options)
#else (Boost_DIR STREQUAL "") # otherwise we use specific CMakeConfig file and don't worry about version
#	message(STATUS "Boost search based on CMake config provided in ${Boost_DIR}")
#	find_package(Boost COMPONENTS system program_options NO_MODULE)
#endif(Boost_DIR STREQUAL "")

#if(NOT Boost_FOUND)
#    message(WARNING "Boost has not been found! MTO will not be built")
#   set(Boost_INCLUDE_DIR "" CACHE STRING "Boost include directory" FORCE)
    #unset(Boost_INCLUDE_DIR CACHE)
#else(NOT Boost_FOUND)
#include_directories(${Boost_INCLUDE_DIR})
#    link_directories(${Boost_LIBRARY_DIRS})
    add_executable(${MTO} 
            main.cpp
            initiators/connectors.cpp initiators/helloreader.cpp initiators/acceptors.cpp
            manager/peerconnectionhandler.cpp manager/connection.cpp manager/options.cpp manager/localmto.cpp manager/peercollection.cpp manager/connectioncollection.cpp
            net/messages.cpp net/endpoint.cpp net/socket.cpp net/csocket.cpp net/async_service.cpp net/async_cservice.cpp
            util/logger.cpp util/option_parser.cpp util/time.cpp)
#target_link_libraries(${MTO} ${Boost_LIBRARIES} )
#    target_link_libraries(${MTO} ${CMAKE_THREAD_LIBS_INIT} )

    # Tester for the MTO 
    add_executable(${TESTER} test/test_suite.cpp util/option_parser.cpp net/csocket.cpp net/socket.cpp util/time.cpp util/logger.cpp net/endpoint.cpp net/async_service.cpp net/async_cservice.cpp)
#    target_link_libraries(${TESTER} ${Boost_LIBRARIES} )
#    target_link_libraries(${TESTER} ${CMAKE_THREAD_LIBS_INIT} )

    install(TARGETS ${MTO} DESTINATION bin)
    install(TARGETS ${TESTER} DESTINATION share/muscle/examples/mto)
    install(FILES "${PROJECT_SOURCE_DIR}/etc/mto-config.cfg.dist" DESTINATION etc)
    install(FILES "${PROJECT_SOURCE_DIR}/etc/mto-topology.cfg.dist" DESTINATION etc)
    install(FILES "${PROJECT_SOURCE_DIR}/etc/mto.log" DESTINATION var/log/muscle/)
#endif(NOT Boost_FOUND)
