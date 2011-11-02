cmake_minimum_required(VERSION 2.6)

project(proxy)

set(Boost_USE_STATIC_LIBS        ON)
set(Boost_USE_MULTITHREADED      ON)
set(Boost_USE_STATIC_RUNTIME     ON)

find_package(Boost REQUIRED system program_options regex )
find_package(Threads)

set(CMAKE_CXX_FLAGS "-g -Wall")

if(NOT Boost_FOUND)
    message("Boost has not been found! MTO will not be built")
else(NOT Boost_FOUND)

    include_directories(${Boost_INCLUDE_DIRS})
    add_executable(mto logger.cpp peerconnectionhandler.cpp connection.cpp options.cpp messages.cpp main.cpp topology.cpp)
    target_link_libraries(mto ${Boost_LIBRARIES} )
    target_link_libraries(mto ${CMAKE_THREAD_LIBS_INIT} )

    # Tester for the MTO 
    add_executable(tester messages.cpp tester.cpp)
    target_link_libraries(tester ${Boost_LIBRARIES} )
    target_link_libraries(tester ${CMAKE_THREAD_LIBS_INIT} )

    SET(INSTALL_DIR ${INSTALL_DIR} CACHE STRING "target dir where to put compiled results")
    IF(INSTALL_DIR)
        install(
            TARGETS mto
            DESTINATION ${INSTALL_DIR}
        )
    ENDIF()

endif(NOT Boost_FOUND)
