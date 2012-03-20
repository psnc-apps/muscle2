SET(CPACK_PACKAGE_DESCRIPTION_SUMMARY "MUSCLE - The Multiscale Coupling Library and Environment")
SET(CPACK_PACKAGE_VENDOR "The Coast and Mapper projects")
#SET(CPACK_PACKAGE_DESCRIPTION_FILE "${CMAKE_SOURCE_DIR}/ReadMe.txt")
SET(CPACK_RESOURCE_FILE_LICENSE "${CMAKE_SOURCE_DIR}/src/gpl.txt")
SET(CPACK_PACKAGE_VERSION_MAJOR "1")
SET(CPACK_PACKAGE_VERSION_MINOR "2")
SET(CPACK_PACKAGE_VERSION_PATCH "0")

	SET(CPACK_INSTALL_PREFIX "/")
SET(CPACK_PACKAGE_DEFAULT_LOCATION ${CMAKE_INSTALL_PREFIX})
	SET(CPACK_SET_DESTDIR "ON")

INCLUDE(CPack)
