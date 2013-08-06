project(mto)

set(MTO mto)

find_package(Threads)

include_directories(..)

add_executable(${MTO} 
            main.cpp
            initiators/connectors.cpp initiators/helloreader.cpp initiators/acceptors.cpp
            manager/peerconnectionhandler.cpp manager/connection.cpp manager/options.cpp manager/localmto.cpp manager/peercollection.cpp manager/connectioncollection.cpp
            manager/messages.cpp net/mpsocket.cpp
            util/mutex.cpp util/rwmutex.cpp util/thread.cpp)

target_link_libraries(${MTO} ${MPWIDE_LIB} )
target_link_libraries(${MTO} ${MUSCLE2UTIL_LIB} )
target_link_libraries(${MTO} ${CMAKE_THREAD_LIBS_INIT} )

install(TARGETS ${MTO} DESTINATION bin)
install(FILES "${PROJECT_SOURCE_DIR}/etc/mto-config.cfg.dist" DESTINATION etc)
install(FILES "${PROJECT_SOURCE_DIR}/etc/mto-topology.cfg.dist" DESTINATION etc)
install(FILES "${PROJECT_SOURCE_DIR}/etc/mto.log" DESTINATION var/log/muscle/)

