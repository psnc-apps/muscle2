cmake_minimum_required(VERSION 2.6)

project(proxy)

set(Boost_USE_STATIC_LIBS       OFF)
set(Boost_USE_MULTITHREADED      ON)
set(Boost_USE_STATIC_RUNTIME    OFF)

find_package(Boost REQUIRED system program_options )

if(NOT Boost_FOUND)
	message(FATAL_ERROR "Boost has not been found")
endif(NOT Boost_FOUND)

include_directories(${Boost_INCLUDE_DIRS})
add_executable(proxy connection.cpp options.cpp messages.cpp main.cpp)
add_executable(tester messages.cpp tester.cpp)
target_link_libraries(proxy ${Boost_LIBRARIES} )
target_link_libraries(tester ${Boost_LIBRARIES} )