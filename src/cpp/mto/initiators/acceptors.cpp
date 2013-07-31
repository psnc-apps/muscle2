//
//  acceptors.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "acceptors.h"
#include "connectors.h"
#include "../constants.hpp"
#include "../net/messages.hpp"
#include "../manager/localmto.h"
#include "../manager/connection.hpp"
#include "../../muscle2/logger.hpp"

#define INIT_CONNECTION_REJECT 61

#include <cstdlib>

using namespace muscle;

Acceptor::Acceptor(ServerSocket *sock, LocalMto *mto) : ss(sock), mto(mto)
{}

void Acceptor::async_report_error(size_t code, int flag, const muscle_exception& ex)
{
    logger::severe( "Failed to listen on incoming connections on %s. Got error: %s. Aborting.",
                  ss->str().c_str(), ex.what());
    exit(1);
}

void ExternalAcceptor::async_accept(size_t code, int flag, ClientSocket *sock)
{
    logger::finest( "Accepted peer connection %s, starting hello exchange",
                  sock->str().c_str());
    
    InitPeerConnection *init = new InitPeerConnection(sock, mto);
}


void InternalAcceptor::async_accept(size_t code, int flag, ClientSocket *sock)
{
    InitConnection *initConn = new InitConnection(sock, mto);
//
//    Connection *conn = new Connection(sock, mto);
}

InitConnection::InitConnection(ClientSocket *sock, LocalMto *mto) : sock(sock), mto(mto), refs(1)
{
    reqBuf = new char[Request::getSize()];
    sock->async_recv(MAIN_INTERNAL_ACCEPT, reqBuf, Request::getSize(), this);
}

bool InitConnection::async_received(size_t code, int user_flag, void *data, void *last_data_ptr, size_t count, int is_final)
{
    // errors are handled in async_report_error
    if (is_final != 1) return true;
    
    Request request(reqBuf);
    
    switch(request.type){
        case Request::Register:
            registerAddress(request);
            break;
        case Request::Connect:
            connect(request);
            break;
        default:
        {
            logger::severe("Client from %s sent unknown message (type %s)",
                          sock->getAddress().str().c_str(),
                          request.type_str().c_str());
        }
    }
    return true;
}

void InitConnection::async_report_error(size_t code, int flag, const muscle_exception& ex)
{
    if (flag == MAIN_INTERNAL_ACCEPT)
        logger::severe("MUSCLE did not send (connection to %s): %s",
                  sock->getAddress().str().c_str(), ex.what());
    else
        logger::severe("Could not send rejection to MUSCLE (connection to %s): %s",
                      sock->getAddress().str().c_str(), ex.what());
}

void InitConnection::connect(const Request &request)
{
    if(mto->hello.matches(request.dst))
    { // local to local
        logger::severe("Requested connection to local port range - to %s from %s",
                      request.dst.str().c_str(), sock->getAddress().str().c_str());
    }
    else
    { // local to remote
        PeerConnectionHandler *secondMto = mto->peers.get(request);
        Header h(request);
        
        if (secondMto)
        {
            mto->conns.create(sock, h, secondMto, false);
            
            sock = NULL; // Don't delete at self-delete
        }
        else
        {
            logger::severe("Requested connection to port out of range (%s) by %s",
                          request.dst.str().c_str(), request.src.str().c_str());
            refs++;
            h.type = Header::ConnectResponse;
            char *packet;
            size_t len = h.makePacket(&packet, 1); // 1 is failed
            sock->async_send(INIT_CONNECTION_REJECT, packet, len, this, 0);
        }
    }
}

void InitConnection::async_sent(size_t code, int flag, void *data, size_t len, int final)
{
    if (!final) return;
    delete [] (char *)data;
}

void InitConnection::registerAddress(const Request &request)
{
    if(mto->hello.matches(request.src))
    {
        mto->conns.setAvailable(request.src);
        
        logger::info("Listening port registered: %s",
                     request.src.str().c_str());
    }
    else
        logger::severe("Port %s out of range - registering port aborted (connection from %s)",
                      request.src.str().c_str(), sock->getAddress().str().c_str());
}

InitPeerConnection::InitPeerConnection(ClientSocket *_sock, LocalMto *mto)
: sock(_sock), mto(mto)
{
    new HelloReader(sock, this, hellos);
}

void InitPeerConnection::allHellosRead()
{
    mto->peers.introduce(sock);
    mto->peers.create(sock, hellos);
}

void InitPeerConnection::allHellosFailed(const muscle_exception& ex)
{
    logger::finest("Reading hellos from peer %s failed - occurred error: %s",
                  sock->str().c_str(), ex.what());
    sock->async_cancel();
    delete sock;
}
