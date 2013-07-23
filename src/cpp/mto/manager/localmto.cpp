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

LocalMto::LocalMto(Options& opts, async_service *service, SocketFactory *intSockFactory, SocketFactory *extSockFactory, const endpoint& externalEp) : hello(opts.getLocalPortLow(), opts.getLocalPortHigh(), 0), name(opts.getMyName()), internalEp(opts.getInternalEndpoint()), willDaemonize(opts.getDaemonize()), service(service), intSockFactory(intSockFactory), extSockFactory(extSockFactory), sockTimeout(opts.getSockAutoCloseTimeout()), externalEp(externalEp), peers(this), conns(this), extAcceptor(NULL), intAcceptor(NULL), sock_opts(MAX_EXTERNAL_WAITING)
{
    logger::info("Setting custom socket options");
    
    sock_opts.keep_alive = true;
    ssize_t bufsize = opts.getTCPBufSize();
    if (bufsize != 0)
    {
        sock_opts.send_buffer_size = sock_opts.recv_buffer_size = bufsize;
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
            try
            {
                endpoint& ep = it->second;
                ep.resolve();
                StubbornConnecter *sc = new StubbornConnecter(ep, service, extSockFactory, sock_opts, sockTimeout, this);
            }
            catch (const muscle_exception& ex)
            {
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
    endpoint ep = handler->remoteEndpoint();
    if (ep != externalEp)
    {
        StubbornConnecter *sc = new StubbornConnecter(ep, service, extSockFactory, sock_opts, sockTimeout, this);
    }
}


void LocalMto::startListeningForClients()
{
    logger::info("Starting internal acceptor on %s", internalEp.str().c_str());
    socket_opts opts(MAX_INTERNAL_WAITING);
    ServerSocket *sock = intSockFactory->listen(internalEp, opts);
    
    intAcceptor = new InternalAcceptor(sock, this);
}

void LocalMto::startListeningForPeers()
{
    logger::info("Starting external acceptor on %s", externalEp.str().c_str());
    
    socket_opts opts(MAX_EXTERNAL_WAITING);
    ServerSocket *sock = extSockFactory->listen(externalEp, opts);
    
    extAcceptor = new ExternalAcceptor(sock, this);
}

LocalMto::~LocalMto()
{
    if (extAcceptor) delete extAcceptor;
    if (intAcceptor) delete intAcceptor;
}

void LocalMto::printDiagnostics() {} 
