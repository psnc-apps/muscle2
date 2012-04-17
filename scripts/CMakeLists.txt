cmake_minimum_required(VERSION 2.6)
PROJECT(scripts CXX) # The project has a name MUSCLE and utilizes CXX compiler

set (MUSCLE_PREFIX ${CMAKE_INSTALL_PREFIX})

configure_file (
	  "${PROJECT_SOURCE_DIR}/muscle.profile.in"
	  "${PROJECT_BINARY_DIR}/muscle.profile"
      )

configure_file (
	  "${PROJECT_SOURCE_DIR}/muscle.in"
	  "${PROJECT_BINARY_DIR}/muscle"
      )

configure_file (
	  "${PROJECT_SOURCE_DIR}/muscle2.in"
	  "${PROJECT_BINARY_DIR}/muscle2"
      )

install (FILES "${PROJECT_BINARY_DIR}/muscle.profile" DESTINATION etc)
install (FILES "${PROJECT_BINARY_DIR}/muscle" DESTINATION bin PERMISSIONS OWNER_WRITE OWNER_READ OWNER_EXECUTE GROUP_READ GROUP_EXECUTE WORLD_READ WORLD_EXECUTE)
install (FILES "${PROJECT_BINARY_DIR}/muscle2" DESTINATION bin PERMISSIONS OWNER_WRITE OWNER_READ OWNER_EXECUTE GROUP_READ GROUP_EXECUTE WORLD_READ WORLD_EXECUTE)
