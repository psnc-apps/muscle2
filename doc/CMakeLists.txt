cmake_minimum_required(VERSION 2.6)
PROJECT(MUSCLE-Documentation) 

option(BUILD_API_DOC CACHE "Build API documentation using Doxygen")

if (BUILD_API_DOC)
    find_package(Doxygen REQUIRED)

    if(DOXYGEN_FOUND)
	set(MUSCLE_CPP_API cpp)
	set(MUSCLE_JAVA_API java)

	configure_file(${PROJECT_SOURCE_DIR}/DoxyfileCPP.in ${PROJECT_BINARY_DIR}/DoxyfileCPP @ONLY)
	configure_file(${PROJECT_SOURCE_DIR}/DoxyfileJava.in ${PROJECT_BINARY_DIR}/DoxyfileJava @ONLY)
	add_custom_target(doc ALL
		${DOXYGEN_EXECUTABLE} ${PROJECT_BINARY_DIR}/DoxyfileCPP
		COMMAND	${DOXYGEN_EXECUTABLE} ${PROJECT_BINARY_DIR}/DoxyfileJava
		WORKING_DIRECTORY ${PROJECT_BINARY_DIR}
		COMMENT "Generating API documentation with Doxygen" VERBATIM
		)
	install(DIRECTORY ${PROJECT_BINARY_DIR}/${MUSCLE_CPP_API} DESTINATION doc/muscle)
	install(DIRECTORY ${PROJECT_BINARY_DIR}/${MUSCLE_JAVA_API} DESTINATION doc/muscle)
	
	install(FILES ${PROJECT_SOURCE_DIR}/index.html DESTINATION doc/muscle)
    endif(DOXYGEN_FOUND)
endif (BUILD_API_DOC)

