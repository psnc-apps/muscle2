project(example_simplematlab)

file(GLOB MATLAB_FILES "*.m")
install(FILES ${MATLAB_FILES} DESTINATION share/muscle/examples/simplematlab)

