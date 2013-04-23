project(example_simplepython)

file(GLOB PYTHON_FILES "*.py")
install(FILES ${PYTHON_FILES} DESTINATION share/muscle/examples/simplepython)

