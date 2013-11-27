project(cpp_test)

include_directories(${PROJECT_SOURCE_DIR}/../../src/cpp)
# Tester for the util libraries
set(MUSCLE2UTIL_TESTER muscle2util_tester)
add_executable(${MUSCLE2UTIL_TESTER} test_util.cpp)
target_link_libraries(${MUSCLE2UTIL_TESTER} ${MUSCLE2_LIB} )
install(TARGETS ${MUSCLE2UTIL_TESTER} DESTINATION share/muscle/test)

if (BUILD_MTO)
    set(MTO_TESTER mto_tester)
    set(MTO_SRC ${PROJECT_SOURCE_DIR}/../../src/cpp/mto)
    find_package(Threads)
    add_executable(${MTO_TESTER} test_mto.cpp ${MTO_SRC}/net/mpsocket.cpp)
    target_link_libraries(${MTO_TESTER} ${MUSCLE2_LIB} )
    target_link_libraries(${MTO_TESTER} ${MPWIDE_LIB} )
    target_link_libraries(${MTO_TESTER} ${CMAKE_THREAD_LIBS_INIT} )
    install(TARGETS ${MTO_TESTER} DESTINATION share/muscle/test)
endif()
