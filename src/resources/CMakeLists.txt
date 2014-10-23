PROJECT(resources)

FILE(GLOB LOGGING_PROP_FILES
	logging/*.*.properties 
	)
FILE(GLOB EXAMPLES_RESOURCE_FILES
	examples/*.dat
	)

install(FILES ${LOGGING_PROP_FILES} DESTINATION share/muscle/resources/logging)
install(FILES ${EXAMPLES_RESOURCE_FILES} DESTINATION share/muscle/examples/resources)


INSTALL(CODE "
SET(CONFIG \$ENV{DESTDIR}\${CMAKE_INSTALL_PREFIX}/share/muscle/resources/logging/logging.properties)
IF(NOT EXISTS \"\${CONFIG}\")
  CONFIGURE_FILE(\"${CMAKE_CURRENT_SOURCE_DIR}/logging.properies\"
                 \"\${CONFIG}\" COPY_ONLY)
ENDIF(NOT EXISTS \"\${CONFIG}\")
")
