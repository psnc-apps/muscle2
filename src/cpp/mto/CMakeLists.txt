project(mto)

set(MTO mto)

include_directories(..)

set(MTO_FILES
    main.cpp
    initiators/connectors.cpp initiators/helloreader.cpp initiators/acceptors.cpp
    manager/peerconnectionhandler.cpp manager/connection.cpp manager/options.cpp manager/localmto.cpp manager/peercollection.cpp manager/connectioncollection.cpp
    manager/messages.cpp net/MPWPathSocket.cpp net/mpsocket.cpp net/DecoupledSelectSocket.cpp net/ThreadPool.cpp)

add_executable(${MTO} ${MTO_FILES})

if (BUILD_STATIC_MTO)
	target_link_libraries(${MTO} ${MPWIDE_LIB_STATIC} )
	target_link_libraries(${MTO} ${MUSCLE2_LIB_STATIC} )
else(BUILD_STATIC_MTO)
	target_link_libraries(${MTO} ${MPWIDE_LIB} )
	target_link_libraries(${MTO} ${MUSCLE2_LIB} )
endif(BUILD_STATIC_MTO)

install(TARGETS ${MTO} DESTINATION bin)
install(FILES "${PROJECT_SOURCE_DIR}/etc/mto-config.cfg.dist" DESTINATION etc)
install(FILES "${PROJECT_SOURCE_DIR}/etc/mto-topology.cfg.dist" DESTINATION etc)
install(FILES "${PROJECT_SOURCE_DIR}/etc/mto.log" DESTINATION var/log/muscle/)

