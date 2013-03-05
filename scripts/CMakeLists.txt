PROJECT(scripts CXX) # The project has a name MUSCLE and utilizes CXX compiler

set (MUSCLE_PREFIX ${CMAKE_INSTALL_PREFIX})

FILE(GLOB THIRDPARTY_INSTALLED_JAR RELATIVE ${CMAKE_SOURCE_DIR}/thirdparty ${CMAKE_SOURCE_DIR}/thirdparty/*.jar)

set(MUSCLE_JAR_PREFIX "${CMAKE_INSTALL_PREFIX}/share/muscle/java")
set(MUSCLE_CLASSPATH "${MUSCLE_JAR_PREFIX}/muscle.jar")

foreach(JAR ${THIRDPARTY_INSTALLED_JAR})
	set(MUSCLE_CLASSPATH "${MUSCLE_CLASSPATH}:${MUSCLE_JAR_PREFIX}/thirdparty/${JAR}")
endforeach()

configure_file (
	  "${PROJECT_SOURCE_DIR}/muscle.profile.in"
	  "${PROJECT_BINARY_DIR}/muscle.profile"
      )

configure_file (
	  "${PROJECT_SOURCE_DIR}/muscle2.in"
	  "${PROJECT_BINARY_DIR}/muscle2"
      )

install (FILES "${PROJECT_BINARY_DIR}/muscle.profile" DESTINATION etc)
install (FILES "${PROJECT_BINARY_DIR}/muscle2" DESTINATION bin PERMISSIONS OWNER_WRITE OWNER_READ OWNER_EXECUTE GROUP_READ GROUP_EXECUTE WORLD_READ WORLD_EXECUTE)
