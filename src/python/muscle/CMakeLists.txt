PROJECT(muscle2-python CXX)

set(MUSCLE2PYTHON_VERSION "1.0")

#set (PYTHON_BUILD_ARGS -L${CMAKE_BINARY_DIR}/src/cpp/muscle2 -I${CMAKE_SOURCE_DIR}/src/cpp/muscle2 -R${MUSCLE_INSTALL_PREFIX}/lib -b${CMAKE_CURRENT_BINARY_DIR} -t${CMAKE_CURRENT_BINARY_DIR})

#if (PYTHON_COMPILER)
#    set (PYTHON_BUILD_ARGS ${PYTHON_BUILD_ARGS} -c${PYTHON_COMPILER})
#endif()

find_package(PythonLibsMuscle)
if (PYTHONLIBS_FOUND)
    add_library(${MUSCLE2_PYTHON} SHARED pythonmuscle.cpp)
    set_target_properties(${MUSCLE2_PYTHON} PROPERTIES OUTPUT_NAME muscle)
    set_target_properties(${MUSCLE2_PYTHON} PROPERTIES PREFIX "")
    set_target_properties(${MUSCLE2_PYTHON} PROPERTIES SUFFIX ".so")
    
    include_directories(${PYTHON_INCLUDE_DIRS} ${CMAKE_SOURCE_DIR}/src/cpp/muscle2)
    target_link_libraries(${MUSCLE2_PYTHON} ${PYTHON_LIBRARIES} ${MUSCLE2_LIB} )

    if (CMAKE_BUILD_TYPE STREQUAL "Debug")
        target_link_libraries(${MUSCLE2_PYTHON} ${PYTHON_DEBUG_LIBRARIES})
    endif()
    
#add_custom_command(
#             OUTPUT ${PYTHON_MODULE}
#             COMMAND ${PYTHON_INTERPRETER} setup.py build_ext ${PYTHON_BUILD_ARGS}
#             WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
#             DEPENDS ${MUSCLE2_LIB} ${PYTHON_HEADERS} ${PYTHON_CPP}
#             )
    
#    add_custom_target(${MUSCLE2_PYTHON} ALL DEPENDS ${PYTHON_MODULE})
    
#    install(FILES ${PYTHON_MODULE} DESTINATION share/muscle/python/modules)
    install(TARGETS ${MUSCLE2_PYTHON} DESTINATION share/muscle/python/modules)
    install(FILES pythonmuscle.hpp DESTINATION include/muscle2)
else(PYTHONLIBS_FOUND)
    message( SEND_ERROR "Python libraries not found. Skipping Python extension" )
endif(PYTHONLIBS_FOUND)
