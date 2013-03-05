project(example_simplefortran Fortran)
enable_language (Fortran)

add_executable(sender sender.F90)
target_link_libraries (sender  ${MUSCLE2_LIB})
install(TARGETS sender DESTINATION share/muscle/examples/simplefortran)

