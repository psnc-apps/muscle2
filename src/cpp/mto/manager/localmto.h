//
//  localmto.h
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__localmto__
#define __CMuscle__localmto__

#define MAX_INTERNAL_WAITING 16
#define MAX_EXTERNAL_WAITING 256

#include "peercollection.h"
#include "connectioncollection.h"
#include "options.hpp"
#include "../initiators/acceptors.h"

#include <map>

class LocalMto
{
public:
	MtoHello hello;
    PeerCollection peers;
    muscle::net::endpoint qcgEp;
    ConnectionCollection conns;
	muscle::net::SocketFactory * const intSockFactory;
    muscle::net::SocketFactory * const extSockFactory;

    void startConnectingToPeers(std::map<std::string, muscle::net::endpoint>& mtoConfigs);
    void startListeningForClients();
    void startListeningForPeers();
    void peerDied(PeerConnectionHandler *handler);
    void printDiagnostics();
    
    LocalMto(Options& opts, muscle::net::async_service *service, muscle::net::SocketFactory *intSockFactory, muscle::net::SocketFactory *extSockFactory, const muscle::net::endpoint& externalEp);
    ~LocalMto();
    
    void setSocketOptions(muscle::net::socket_opts& opts);
private:
	bool done;
	ExternalAcceptor *extAcceptor;
    InternalAcceptor *intAcceptor;
	muscle::net::async_service * const service;
    muscle::util::duration sockTimeout;
	muscle::net::endpoint internalEp;
    muscle::net::endpoint externalEp;
    const bool willDaemonize;
	
    muscle::net::socket_opts intSockOpts, intClientOpts, extSockOpts, extClientOpts;
    const std::string name;
};

#endif /* defined(__CMuscle__localmto__) */
