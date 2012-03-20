cmake_minimum_required(VERSION 2.6)

PROJECT(cxa)

# load our includes

FILE (GLOB ALL_CXA *.cxa.rb)

# create our library
install(FILES ${ALL_CXA} DESTINATION share/muscle/examples/cxa)
