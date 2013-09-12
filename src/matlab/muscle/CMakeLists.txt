
PROJECT(muscle2-matlab)


find_program(MEX_EXECUTABLE mex)

if (MEX_EXECUTABLE)
	file(GLOB MEX_INPUTS "*.c")
	add_custom_target( MexLibs ALL )

	foreach(mexInput ${MEX_INPUTS})
		get_filename_component(mf ${mexInput} NAME_WE)
		add_custom_command(
		    SOURCE    ${mf}.c
		    COMMAND   ${MEX_EXECUTABLE}
		    ARGS      ${CMAKE_SOURCE_DIR}/src/matlab/muscle/${mf}.c
-lmuscle2 -L${CMAKE_BINARY_DIR}/src/cpp/muscle2
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


