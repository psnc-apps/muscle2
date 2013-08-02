//
//  localmto.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "localmto.h"
#include "../../muscle2/logger.hpp"
#include "../initiators/connectors.h"
#include "../net/socket.h"

using namespace std;
using namespace muscle;

LocalMto::LocalMto(Options& opts, async_service *service, SocketFactory *intSockFactory, SocketFactory *extSockFactory, const endpoint& externalEp) : hello(opts.getLocalPortLow(), opts.getLocalPortHigh(), 0), name(opts.getMyName()), internalEp(opts.getInternalEndpoint()), willDaemonize(opts.getDaemonize()), service(service), intSockFactory(intSockFactory), extSockFactory(extSockFactory), sockTimeout(opts.getSockAutoCloseTimeout()), externalEp(externalEp), peers(this), conns(this), extAcceptor(NULL), intAcceptor(NULL), sock_opts(MAX_EXTERNAL_WAITING), client_opts(MAX_EXTERNAL_WAITING)
{
    logger::info("Setting custom socket options");
    
    client_opts.keep_alive = true;
    ssize_t bufsize = opts.getTCPBufSize();
    if (bufsize != 0) {
        client_opts.send_buffer_size = client_opts.recv_buffer_size = bufsize;
        // Bufsize is doubled by the system, and we divide to get kB
        ssize_t buf_kB = bufsize/512;
        logger::info("Setting custom TCP buffer sizes: %ldkB", buf_kB);
    }
}

void LocalMto::startConnectingToPeers(map<string, endpoint>& mtoConfigs)
{
    for(map<string, endpoint>::iterator it = mtoConfigs.begin(); it!= mtoConfigs.end(); ++it)
    {
        if(it->second.port != 0 && it->first != name)
        {
            try {
                endpoint& ep = it->second;
                ep.resolve();
                StubbornConnecter *sc = new StubbornConnecter(ep, service, extSockFactory, client_opts, sockTimeout, this);
            } catch (const muscle_exception& ex) {
                logger::severe("Cannot resolve %s (error %s). Ignoring MTO '%s'.",
                              it->second.str().c_str(), ex.what(), it->first.c_str());
                continue;
            }
        }
    }
}

void LocalMto::peerDied(PeerConnectionHandler *handler)
{
    conns.peerDied(handler);
    const endpoint& ep = handler->address();
    if (ep != externalEp) {
        StubbornConnecter *sc = new StubbornConnecter(ep, service, extSockFactory, client_opts, sockTimeout, this);
    }
}


void LocalMto::startListeningForClients()
{
    logger::info("Starting internal acceptor on %s", internalEp.str().c_str());
    ServerSocket *sock = intSockFactory->listen(internalEp, sock_opts);
    intAcceptor = new InternalAcceptor(sock, this, &client_opts);
}

void LocalMto::startListeningForPeers()
{
    logger::info("Starting external acceptor on %s", externalEp.str().c_str());
    
    ServerSocket *sock = extSockFactory->listen(externalEp, sock_opts);
    extAcceptor = new ExternalAcceptor(sock, this, &client_opts);
}

LocalMto::~LocalMto()
{
    if (extAcceptor) delete extAcceptor;
    if (intAcceptor) delete intAcceptor;
	peers.clear();
}

void LocalMto::printDiagnostics() {} 

