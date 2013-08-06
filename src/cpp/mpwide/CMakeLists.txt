PROJECT(mpwide CXX)

set(MPWIDE_VERSION "1.0")

add_library(${MPWIDE_LIB} SHARED MPWide.cpp Socket.cpp)
set_target_properties(${MPWIDE_LIB} PROPERTIES VERSION ${MPWIDE_VERSION})
set_target_properties(${MPWIDE_LIB} PROPERTIES OUTPUT_NAME MPW)

install(TARGETS ${MPWIDE_LIB} DESTINATION lib)
