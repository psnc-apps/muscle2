//
//  connectioncollection.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 5/7/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "connectioncollection.h"

using namespace std;
using namespace muscle;

Connection *ConnectionCollection::get(const Header& h)
{
    conns_t::iterator it = remoteConnections.find(Identifier(h));
    if (it == remoteConnections.end()) {
        if (logger::isLoggable(MUSCLE_LOG_FINER))
	        logger::finer("Requested connection %s does not exist",
                      h.str().c_str());
        return NULL;
    } else {
        if (logger::isLoggable(MUSCLE_LOG_FINER))
	        logger::finer("Request for existing connection %s",
                      h.str().c_str());
        return it->second;
    }
}

Connection *ConnectionCollection::create(muscle::net::ClientSocket *sock, Header& h, PeerConnectionHandler *handler, bool remoteHasConnected)
{
    logger::fine("Creating connection %s", h.str().c_str());
    Connection *conn = new Connection(h, sock, handler, mto, remoteHasConnected);
    remoteConnections[Identifier(h)] = conn;
    return conn;
}

void ConnectionCollection::erase(const Header& h)
{
    logger::fine("Removing connection %s", h.str().c_str());
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
	conns_t::iterator itTmp;
    for(conns_t::iterator it = remoteConnections.begin(); it != remoteConnections.end();)
    {
		// Ugly, but it allows for self delete of
		// the connection if there is no alternative peer
		itTmp = it;
		++it;
        itTmp->second->peerDied(handler);
    }
}

void ConnectionCollection::setAvailable(const muscle::net::endpoint& ep)
{
    availableConnections.insert(ep);
}

bool ConnectionCollection::isAvailable(const muscle::net::endpoint& ep)
{
    return availableConnections.find(ep) != availableConnections.end();
}

void ConnectionCollection::setUnavailable(const muscle::net::endpoint& ep)
{
    availableConnections.erase(ep);
}
