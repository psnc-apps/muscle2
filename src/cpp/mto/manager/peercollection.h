//
//  PeerCollection.h
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__PeerCollection__
#define __CMuscle__PeerCollection__

#include "peerconnectionhandler.hpp"
#include "connectioncollection.h"

#include <map>
#include <vector>

class Header;

typedef std::vector<PeerConnectionHandler *> peerconns_t;

struct MtoPeer
{
    uint16_t min;               ///< Lower bound for the port range
    peerconns_t peerConnection; ///< Where to send messages to the MTO
    MtoHello bestHello;         ///< The Hello for this MTO

    MtoPeer() : min(0) {}
    MtoPeer(uint16_t min, const MtoHello& hello, PeerConnectionHandler *h)
    : min(min), bestHello(hello) { peerConnection.push_back(h); }
};


class PeerCollection
{
private:
    // Keys are the high end of the port range
    typedef std::map<uint16_t,MtoPeer> peers_t;
    typedef std::map<PeerConnectionHandler *,PeerConnectionHandler *> peer2peer_t;

    LocalMto *mto;
    peers_t peers;
    
    // Keys are the high end of the port range
    /** Direct connections from other MTO's to this MTO */
    std::map<uint16_t,PeerConnectionHandler *> connectionsIncomming;
    
    /** Direct connections to other MTO's from this MTO */
    std::map<uint16_t,PeerConnectionHandler *> connectionsOutgoing;
    
    /** Keeps pairs of connections from and to MTO for fast loopup of proper connection to be used */
    peer2peer_t connectionsIncToOut;

    /** Once the second connection ouf of incomming/outgoing is formed, this method is called */
    void newConnectionPairFormed(uint16_t portHigh);
    
    /** Parses the Hello and returns if this hello should be forwarded to others */
    bool insert(PeerConnectionHandler *handler, const MtoHello& hello);
public:
    PeerCollection(LocalMto *mto) : mto(mto) {}
    
    ~PeerCollection() {}

    /** Get the PeerConnectionHandler that matches the destination of the given header */
    PeerConnectionHandler *get(Header h) const;

    /** Get the standard PeerConnectionHandler instead of the given PeerConnectionHandler */
    PeerConnectionHandler *get(PeerConnectionHandler *h) const;
    
    /** Create a new PeerConnectionHandler from given socket */
    PeerConnectionHandler *create(muscle::ClientSocket *sock, std::vector<MtoHello>& hellos);
    
    /** Update the header information of the given handler */
    void update(PeerConnectionHandler *handler, const Header& h);
    /** Erase given handler from possible peers */
    void erase(PeerConnectionHandler *h);

    /** Introduce all peers to a given socket. To get feedback, the sendlistener can
     listen for succesful sends, and it should delete the char pointer of data once
     it is completely sent. The errorlistener should clean up the socket, since it will
     not be able to make a full introduction. */
    void introduce(muscle::ClientSocket *sock);
	
	void clear();
};

#endif /* defined(__CMuscle__PeerCollection__) */
