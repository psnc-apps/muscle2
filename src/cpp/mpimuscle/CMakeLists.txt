cmake_minimum_required(VERSION 2.6)

PROJECT(mpimuscle  C)

add_executable(mpimuscle mpimuscle.c)
target_link_libraries(mpimuscle ${MUSCLE2_LIB})

install(TARGETS mpimuscle DESTINATION bin)
