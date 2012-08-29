cmake_minimum_required(VERSION 2.6)

PROJECT(resources)

FILE(GLOB LOGGING_PROP_FILES
	logging/*.properties 
	)
FILE(GLOB SANDBOX_PROP_FILES
	sandbox_template/*.cxa.rb	
	)
FILE(GLOB EXAMPLES_RESOURCE_FILES
	examples/*.dat
	)

install(FILES ${LOGGING_PROP_FILES} DESTINATION share/muscle/resources/logging)
install(FILES ${SANDBOX_PROP_FILES} DESTINATION share/muscle/resources/sandbox_template)
install(FILES ${EXAMPLES_RESOURCE_FILES} DESTINATION share/muscle/examples/resources)
