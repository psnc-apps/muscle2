cmake_minimum_required(VERSION 2.6)

project(mto)

set(MTO mto)
set(TESTER mto_tester)

set(Boost_USE_STATIC_LIBS        OFF)
set(Boost_USE_MULTITHREADED      ON)
set(Boost_USE_STATIC_RUNTIME     ON)

#set(CMAKE_BUILD_TYPE Debug)

find_package(Threads)

set (Boost_DIR "" CACHE PATH "Location of the directory with CMake config for Boost")

if (Boost_DIR STREQUAL "") # if we don't specify Boost CMakeConfig file location we are looking for the Boost newer than 1.36
	message(STATUS "Boost search based on CMake Modules")
	set(Boost_NO_BOOST_CMAKE ON)
	find_package(Boost 1.35 COMPONENTS system program_options regex)
else (Boost_DIR STREQUAL "") # otherwise we use specific CMakeConfig file and don't worry about version
	message(STATUS "Boost search based on CMake config provided in ${Boost_DIR}")
	find_package(Boost COMPONENTS system program_options regex NO_MODULE)
endif(Boost_DIR STREQUAL "")

if(NOT Boost_FOUND)
    message(WARNING "Boost has not been found! MTO will not be built")
    set(Boost_INCLUDE_DIR "" CACHE STRING "Boost include directory" FORCE)
    #unset(Boost_INCLUDE_DIR CACHE)
else(NOT Boost_FOUND)
    include_directories(${Boost_INCLUDE_DIRS})
    add_executable(${MTO} logger.cpp peerconnectionhandler.cpp connection.cpp options.cpp messages.cpp main.cpp topology.cpp)
    target_link_libraries(${MTO} ${Boost_LIBRARIES} )
    target_link_libraries(${MTO} ${CMAKE_THREAD_LIBS_INIT} )

    # Tester for the MTO 
    add_executable(${TESTER} messages.cpp tester.cpp)
    target_link_libraries(${TESTER} ${Boost_LIBRARIES} )
    target_link_libraries(${TESTER} ${CMAKE_THREAD_LIBS_INIT} )

    install(TARGETS ${MTO} DESTINATION bin)
    install(TARGETS ${TESTER} DESTINATION share/muscle/examples/mto)
    install(FILES "${PROJECT_SOURCE_DIR}/mto-config.cfg.dist" DESTINATION etc)
    install(FILES "${PROJECT_SOURCE_DIR}/mto-topology.cfg.dist" DESTINATION etc)
    install(FILES "${PROJECT_SOURCE_DIR}/mto.log" DESTINATION log/muscle/)
endif(NOT Boost_FOUND)
