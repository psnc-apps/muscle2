project(example_simplefortran NONE)

# Work-around for CMake issue 0009220
# http://public.kitware.com/Bug/view.php?id=9220
if(DEFINED CMAKE_Fortran_COMPILER)
	if (CMAKE_Fortran_COMPILER MATCHES "^$" OR NOT EXISTS ${CMAKE_Fortran_COMPILER})
  		set(CMAKE_Fortran_COMPILER CMAKE_Fortran_COMPILER-NOTFOUND CACHE STRING "")
	endif()
endif()

enable_language(Fortran OPTIONAL)

if (CMAKE_Fortran_COMPILER-NOTFOUND OR NOT EXISTS ${CMAKE_Fortran_COMPILER})
	message( WARNING "Fortran compiler not found - Fortran example will not be built.")
else ()
	add_executable(sender sender.F90)
	target_link_libraries (sender  ${MUSCLE2_LIB})
	install(TARGETS sender DESTINATION share/muscle/examples/simplefortran)
endif()

