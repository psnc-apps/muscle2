//
//  meta_socket.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 04-05-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "meta_socket.h"
#include "muscle2/util/exception.hpp"

namespace muscle {

using namespace net;
using namespace util;

meta_socket::meta_socket(endpoint& ep, async_service *service, duration& timeout) : socket(ep, service), locked(false), timeout(timeout)
{
    sock = NULL;
}

const ClientSocket *meta_socket::lock()
{
    if (locked)
        throw muscle_exception("Can not lock already locked socket");

    locked = true;
    
    if (sock == NULL)
    {
        recreate();
    }
    
    return sock;
}
    
void meta_socket::unlock()
{
    if (!locked)
        throw muscle_exception("Can not unlock already socket that was not locked");
    
    locked = false;
}


void meta_socket::enableAutoClose(bool on)
{
    assert( reconnect );
    
    logger::finest("PeerConnHandler to %s set to auto close mode", socketEndpt.str().c_str());
    
    autoClose = on;
    updateLastOperationTimer();
    
    mtime triggerTime = lastOperation + Options::getInstance().getSockAutoCloseTimeout();
    iddleTimer = asyncService->timer(PCH_IDLE, triggerTime, this, this, NULL);
    //    iddleTimer.async_wait(bind(&PeerConnectionHandler::iddleTimerFired, this, _1));
}

void meta_socket::updateLastOperationTimer()
{
    lastOperation = mtime();
}

void meta_socket::async_execute(size_t code, int user_flag, void *user_data)
{
    switch (user_flag) {
        case PCH_IDLE:
        {
            muscle::time timeout = lastOperation + Options::getInstance().getSockAutoCloseTimeout();
            
            logger::finest("Checking if connection to %s is iddle, last access at %s",
                          socketEndpt.str().c_str(),
                          lastOperation.str().c_str()
                          );
            
            if(timeout.is_past())
            {
                logger::info("Connection to %s is idle, closing socket",
                             socketEndpt.str().c_str()
                             );
                
                Header h;
                h.type=Header::PeerClose;
                char *buf;
                size_t len = h.makePacket(&buf);
                // To do: There is a slight chance that between the line below and iddleSocketClose something bad will happen
                send(buf, len);
                //bind(&PeerConnectionHandler::iddleSocketClose, this));
            }
            else
            {
                asyncService->update_timer(iddleTimer, timeout, NULL);
            }
            break;
        }
        default:
            break;
    }
}

void meta_socket::iddleSocketClose()
{
    ready = false;
    delete socket;
    socket = 0;
    
    logger::finest("Connection to %s closed",
                  socketEndpt.str().c_str()
                  );
}


void meta_socket::recreateSocket(muscle::net::async_function *func)
{
    recreateSocketPending.push_back(func);
    
    // already recreating socket
    if(socket)
        return;
    
    logger::severe("Recreating socket to %s",
                 socketEndpt.str().c_str());
    
    ClientSocket::async_connect(asyncService, PCH_RECREATE, socketEndpt, this, this);
    muscle::time timeout = muscle::util::duration(10,0).time_after();
    asyncService->update_timer(recreateSocketTimer, timeout, NULL);
}

void meta_socket::recreateSocketFired()
{
    asyncService->erase_timer(recreateSocketTimer);
    
    //        // be unhappy and kill peerconnectionhandler
    //        logger::severe("Failed to recreating socket to %s - connection refused",
    //                      socketEndpt.str().c_str());
    
    logger::info("Connection successfuly recreated to %s",
                 socketEndpt.str().c_str()
                 );
    
    // hello xchange....
    
    char * data = new char[MtoHello::getSize()];
    MtoHello myHello(Options::getInstance().getLocalPortLow(), Options::getInstance().getLocalPortHigh(), 0, true);
    myHello.serialize(data);
    socket->async_send(PCH_MAKE_CONNECT, data, MtoHello::getSize(), this, this);
    
    //bind(&PeerConnectionHandler::recreateSocketSentHello, this, _1, data));
}

void meta_socket::recreateSocketSentHello(char* data)
{
    socket->async_recv(PCH_MAKE_CONNECT, data, MtoHello::getSize(), this, this);
    //bind(&PeerConnectionHandler::recreateSocketReadHello, this, _1, data));
}

void meta_socket::recreateSocketReadHello(char* data)
{
    MtoHello hello(data);
    
    if(!hello.isLastMtoHello) {
        socket->async_recv(PCH_MAKE_CONNECT, data, MtoHello::getSize(), this, this);
        //        async_read(*socket, buffer(data, MtoHello::getSize()), bind(&PeerConnectionHandler::recreateSocketReadHello, this, _1, data));
        return;
    }
    
    delete [] data;
    
    ready = true;
    startReadHeader();
    
    std::vector<muscle::net::async_function *> ops(recreateSocketPending);
    recreateSocketPending.clear();
    
    for (int i = 0; i < ops.size(); ++i) {
        ops[i]->async_execute(0, PCH_RECREATE, NULL);
    }
    
    updateLastOperationTimer();
    muscle::time timeout = lastOperation + Options::getInstance().getSockAutoCloseTimeout();
    asyncService->update_timer(iddleTimer, timeout, NULL);
}


void meta_socket::recreateSocketTimedOut(size_t timer, int user_flag, void *user_data)
{
    int *retryCount = (int *)user_data;
    
    if(*retryCount>2){
        // be unhappy and kill peerconnectionhandler
        errorOccurred("Failed to recreating socket to " + socketEndpt.str() + " - timed out");
        return;
    }
    (*retryCount)++;
    
    logger::finest("Recreating socket to %s timed out, retrying...",
                  socketEndpt.str().c_str());
    
    delete socket;
    
    muscle::time timeout = muscle::util::duration(20,0).time_after();
    asyncService->update_timer(recreateSocketTimer, timeout, retryCount);
    muscle::net::ClientSocket::async_connect(asyncService, PCH_SOCKET_TIMER, socketEndpt, this, this);
}

}
