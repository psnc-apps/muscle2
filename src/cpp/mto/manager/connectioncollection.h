//
//  connectioncollection.h
//  CMuscle
//
//  Created by Joris Borgdorff on 5/7/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__connectioncollection__
#define __CMuscle__connectioncollection__

#include "connection.hpp"
#include "peerconnectionhandler.hpp"

#include <map>
#include <set>

class ConnectionCollection
{
    typedef std::map<Identifier, Connection *> conns_t;
    
    /** Open connections tunneled via proxy */
    conns_t remoteConnections;
    std::set<muscle::net::endpoint> availableConnections;
    LocalMto *mto;

public:
    ConnectionCollection(LocalMto *mto) : mto(mto) {}
    
    Connection *get(const Header& h);
    Connection *create(muscle::net::ClientSocket *sock, Header& h, PeerConnectionHandler *handler, bool remoteHasConnected);
    void erase(const Header& h);
    void replacePeer(PeerConnectionHandler *oldh, PeerConnectionHandler *newh);
    void peerDied(PeerConnectionHandler *handler);    
    
    void setAvailable(const muscle::net::endpoint& ep);
    void setUnavailable(const muscle::net::endpoint& ep);
    bool isAvailable(const muscle::net::endpoint& ep);
};

#endif /* defined(__CMuscle__connectioncollection__) */
