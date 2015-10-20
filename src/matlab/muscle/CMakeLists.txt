
PROJECT(muscle2-matlab)


find_program(MEX_EXECUTABLE mex)

if(MEX_EXECUTABLE)
	execute_process(COMMAND ${MEX_EXECUTABLE} -help OUTPUT_VARIABLE MEX_OUT)
endif()

SET(MATLAB_STRING ".*MATLAB.*")
# exclude mex that works with pdftex
if("${MEX_OUT}" MATCHES ${MATLAB_STRING}) 
	
	file(GLOB MEX_INPUTS "*.c")
	add_custom_target( MexLibs ALL )

	target_link_libraries(MexLibs ${MUSCLE2_LIB})

	foreach(mexInput ${MEX_INPUTS})
		get_filename_component(mf ${mexInput} NAME_WE)
		add_custom_command(
		    SOURCE    ${mf}.c
		    COMMAND   ${MEX_EXECUTABLE}
		    ARGS      ${CMAKE_SOURCE_DIR}/src/matlab/muscle/${mf}.c
-lmuscle2 -L${CMAKE_BINARY_DIR}/src/cpp/muscle2
-I${CMAKE_SOURCE_DIR}/src/cpp/muscle2
		    TARGET    MexLibs
		    OUTPUTS   ${mf}.mexa64
		)

		add_custom_command(
		    SOURCE    MexLibs
		    TARGET    MexLibs
		    DEPENDS   ${mf}.mexa64
		)
	
		install(FILES "${CMAKE_BINARY_DIR}/src/matlab/muscle/${mf}.mexa64" DESTINATION share/muscle/matlab/modules)
	endforeach()

else()
    message( WARNING "Matlab binary 'mex' not found. Skipping Matlab extension" )
endif()


