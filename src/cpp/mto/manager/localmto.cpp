//
//  localmto.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "localmto.h"
#include "../util/logger.hpp"
#include "../initiators/connectors.h"
#include "../net/csocket.h"

using namespace std;

LocalMto::LocalMto(Options& opts, muscle::async_service *service, const muscle::endpoint& externalEp) : hello(opts.getLocalPortLow(), opts.getLocalPortHigh(), 0), name(opts.getMyName()), internalEp(opts.getInternalEndpoint()), willDaemonize(opts.getDaemonize()), service(service), sockTimeout(opts.getSockAutoCloseTimeout()), externalEp(externalEp), peers(this), conns(this), extAcceptor(NULL), intAcceptor(NULL)
{
    Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn,"Setting custom socket options");
    
    sock_opts.keep_alive = true;
    ssize_t bufsize = opts.getTCPBufSize();
    if (bufsize != 0)
    {
        sock_opts.send_buffer_size = sock_opts.recv_buffer_size = bufsize;
        // Bufsize is doubled by the system, and we divide to get kB
        ssize_t buf_kB = bufsize/512;
        Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn,"Setting custom TCP buffer sizes: %ldkB", buf_kB);
    }
}

void LocalMto::startConnectingToPeers(map<string, muscle::endpoint>& mtoConfigs)
{
    for(map<string, muscle::endpoint>::iterator it = mtoConfigs.begin(); it!= mtoConfigs.end(); ++it)
    {
        if(it->second.port != 0 && it->first != name)
        {
            try
            {
                muscle::endpoint& ep = it->second;
                ep.resolve();
                StubbornConnecter *sc = new StubbornConnecter(ep, service, sock_opts, sockTimeout, this);
            }
            catch (const muscle::muscle_exception& ex)
            {
                Logger::error(Logger::MsgType_PeerConn|Logger::MsgType_Config,
                              "Cannot resolve %s (error %s). Ignoring MTO '%s'.",
                              it->second.str().c_str(), ex.what(), it->first.c_str());
                continue;
            }
        }
    }
}

void LocalMto::peerDied(PeerConnectionHandler *handler)
{
    conns.peerDied(handler);
    muscle::endpoint ep = handler->remoteEndpoint();
    if (ep != externalEp)
    {
        StubbornConnecter *sc = new StubbornConnecter(ep, service, sock_opts, sockTimeout, this);
    }
}


void LocalMto::startListeningForClients()
{
    Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn, "Starting internal acceptor on %s", internalEp.str().c_str());
    muscle::socket_opts opts(MAX_INTERNAL_WAITING);
    muscle::ServerSocket *servSock = new muscle::CServerSocket(internalEp, service, opts);
    
    intAcceptor = new InternalAcceptor(servSock, this);
}

void LocalMto::startListeningForPeers()
{
    Logger::info(Logger::MsgType_Config|Logger::MsgType_PeerConn, "Starting external acceptor on %s", externalEp.str().c_str());
    
    muscle::socket_opts opts(MAX_EXTERNAL_WAITING);
    muscle::CServerSocket *sock = new muscle::CServerSocket(externalEp, service, opts);
    
    extAcceptor = new ExternalAcceptor(sock, this);
}

LocalMto::~LocalMto()
{
    if (extAcceptor) delete extAcceptor;
    if (intAcceptor) delete intAcceptor;
}

void LocalMto::printDiagnostics() {} 

