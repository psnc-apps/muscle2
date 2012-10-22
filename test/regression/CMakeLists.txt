
enable_testing()

add_test(MuscleVersion muscle2 --version)

macro (do_test2 cxa result)
  set (CXA_FILE "${CMAKE_INSTALL_PREFIX}/share/muscle/examples/cxa/${cxa}")
  add_test (Muscle2Test-${cxa} muscle2 --cxa ${CXA_FILE} --main --allkernels)
  set_tests_properties (Muscle2Test-${cxa} PROPERTIES PASS_REGULAR_EXPRESSION ${result})
  set_tests_properties (Muscle2Test-${cxa} PROPERTIES TIMEOUT 30)
endmacro (do_test2)

macro (do_test2_exec exec cxa result)
  set (CXA_FILE "${CMAKE_INSTALL_PREFIX}/share/muscle/examples/cxa/${cxa}")
  set (CXA_EXEC "${CMAKE_INSTALL_PREFIX}/share/muscle/examples/${exec}/${exec}")
  add_test (Muscle2ExecTest-${cxa} ${CXA_EXEC} -- --cxa ${CXA_FILE} --main --allkernels)
  set_tests_properties (Muscle2ExecTest-${cxa} PROPERTIES PASS_REGULAR_EXPRESSION ${result})
  set_tests_properties (Muscle2ExecTest-${cxa} PROPERTIES TIMEOUT 30)
endmacro (do_test2_exec)

do_test2("SimpleExample.cxa.rb" ] "got: 2.0")
do_test2("Triangle.cxa.rb"  "Got from Three: 3.0")
do_test2("PingPong.cxa.rb"  "All local submodels have finished")
do_test2("SimpleSubmodelExample.cxa.rb"  "(1.0\t2.0\t)")
do_test2("Terminals.cxa.rb"  "boolMask.3.dat")
if (BUILD_CPP)
    do_test2("NativeExample2.cxa.rb"  "got: 2.0")
    do_test2("TransmutableExample2.cxa.rb"  "t: 2")
    if (BUILD_MPI_EXAMPLES)
        do_test2("LHC2.cxa.rb"  "LHC:Final proton energy: 12")
    endif (BUILD_MPI_EXAMPLES)
    do_test2("PingPongCpp.cxa.rb"  "         16")
    do_test2("DataIntegrity.cxa.rb"  "tests passed")
    do_test2_exec("simplecpp2" "NativeExample2.cxa.rb"  "got: 2.0")
    do_test2_exec("transmutable2" "TransmutableExample2.cxa.rb"  "t: 2")
	if (BUILD_FORTRAN)
		do_test2("SimpleFortranExample.cxa.rb" "got: 2.0")
	endif (BUILD_FORTRAN)
endif (BUILD_CPP)



#legacy examples
#do_test("NativeExample.cxa.rb" "r w" "got: 2.0")


