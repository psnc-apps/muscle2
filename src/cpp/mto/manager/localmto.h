//
//  localmto.h
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__localmto__
#define __CMuscle__localmto__

#define MAX_INTERNAL_WAITING 10
#define MAX_EXTERNAL_WAITING 10

#include "peercollection.h"
#include "connectioncollection.h"
#include "options.hpp"
#include "../initiators/acceptors.h"

#include <map>

class LocalMto
{
public:
    MtoHello hello;
    muscle::socket_opts sock_opts;
    muscle::socket_opts client_opts;
    const std::string name;
    muscle::endpoint internalEp;
    muscle::endpoint externalEp;
    const bool willDaemonize;
    muscle::async_service * const service;
    muscle::SocketFactory * const intSockFactory;
    muscle::SocketFactory * const extSockFactory;
    muscle::duration sockTimeout;
    PeerCollection peers;
    ConnectionCollection conns;
    ExternalAcceptor *extAcceptor;
    InternalAcceptor *intAcceptor;
    
    void startConnectingToPeers(std::map<std::string, muscle::endpoint>& mtoConfigs);
    void startListeningForClients();
    void startListeningForPeers();
    void peerDied(PeerConnectionHandler *handler);
    void printDiagnostics();
    
    LocalMto(Options& opts, muscle::async_service *service, muscle::SocketFactory *intSockFactory, muscle::SocketFactory *extSockFactory, const muscle::endpoint& externalEp);
    ~LocalMto();
    
    void setSocketOptions(muscle::socket_opts& opts);
};

#endif /* defined(__CMuscle__localmto__) */
