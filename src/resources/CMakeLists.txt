PROJECT(resources)

FILE(GLOB LOGGING_PROP_FILES
	logging/*.properties 
	)
FILE(GLOB EXAMPLES_RESOURCE_FILES
	examples/*.dat
	)

install(FILES ${LOGGING_PROP_FILES} DESTINATION share/muscle/resources/logging)
install(FILES ${EXAMPLES_RESOURCE_FILES} DESTINATION share/muscle/examples/resources)
