//
//  connectioncollection.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 5/7/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "connectioncollection.h"

using namespace std;

Connection *ConnectionCollection::get(Header h)
{
    conns_t::iterator it = remoteConnections.find(Identifier(h));
    if (it == remoteConnections.end())
    {
        Logger::debug(Logger::MsgType_ClientConn,
                      "Requested connection %s does not exist",
                      h.str().c_str());
        return NULL;
    }
    else
    {
        Logger::debug(Logger::MsgType_ClientConn,
                      "Request for existing connection %s",
                      h.str().c_str());
        return it->second;
    }
}

Connection *ConnectionCollection::create(muscle::ClientSocket *sock, Header& h, PeerConnectionHandler *handler, bool remoteHasConnected)
{
    Logger::debug(Logger::MsgType_ClientConn,
                  "Creating connection %s", h.str().c_str());
    Connection *conn = new Connection(h, sock, handler, mto, remoteHasConnected);
    remoteConnections[Identifier(h)] = conn;
    return conn;
}

void ConnectionCollection::erase(Header h)
{
    Logger::debug(Logger::MsgType_ClientConn,
                  "Removing connection %s", h.str().c_str());
    remoteConnections.erase(Identifier(h));
}

void ConnectionCollection::replacePeer(PeerConnectionHandler *oldh, PeerConnectionHandler *newh)
{
    for (conns_t::iterator it = remoteConnections.begin(); it != remoteConnections.end(); ++it)
    {
        it->second->replacePeer(oldh, newh);
    }
}

void ConnectionCollection::peerDied(PeerConnectionHandler *handler)
{
    for(conns_t::iterator it = remoteConnections.begin(); it != remoteConnections.end(); ++it)
    {
        it->second->peerDied(handler);
    }
}

void ConnectionCollection::setAvailable(const muscle::endpoint& ep)
{
    availableConnections.insert(ep);
}

bool ConnectionCollection::isAvailable(const muscle::endpoint& ep)
{
    return availableConnections.find(ep) != availableConnections.end();
}

void ConnectionCollection::setUnavailable(const muscle::endpoint& ep)
{
    availableConnections.erase(ep);
}
