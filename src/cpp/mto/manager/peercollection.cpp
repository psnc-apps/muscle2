//
//  PeerCollection.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 5/6/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "peercollection.h"
#include "localmto.h"

#include <set>

using namespace std;
using namespace muscle;
using namespace muscle::net;

PeerConnectionHandler *PeerCollection::get(Header header) const
{
    peers_t::const_iterator it = peers.lower_bound(header.dst.port);
    if(it == peers.end())
        return 0;
    const MtoPeer& candidate = it->second;
    if(candidate.min > header.dst.port)
        return 0;
    
    // Get any of the candidates, sampled based on the port number
    int pos = int(header.dst.port % candidate.peerConnection.size());
    
    return get(candidate.peerConnection[pos]);
}

PeerConnectionHandler *PeerCollection::get(PeerConnectionHandler *peer) const
{
    peer2peer_t::const_iterator it = connectionsIncToOut.find(peer);
    if(it != connectionsIncToOut.end())
        return it->second;
    return peer;
}

PeerConnectionHandler *PeerCollection::create(ClientSocket *sock, std::vector<MtoHello> &hellos)
{
    PeerConnectionHandler * handler = new PeerConnectionHandler(sock, mto);
    set<PeerConnectionHandler *> allPeers;

    vector<MtoHello> directNeighbours;
    for (vector<MtoHello>::iterator hello = hellos.begin(); hello != hellos.end(); hello++)
    {
        if(insert(handler, *hello) && hello->distance==0)
            directNeighbours.push_back(*hello);
    }

    // Propagate all direct neighbors to all the other peers
    if(!directNeighbours.empty())
    {
        for(peers_t::const_iterator it = peers.begin(); it!=peers.end(); ++it)
        {
            for (peerconns_t::const_iterator h = it->second.peerConnection.begin(); h != it->second.peerConnection.end(); ++h)
                allPeers.insert(*h);
        }
        
        for (set<PeerConnectionHandler *>::const_iterator peer = allPeers.begin(); peer != allPeers.end(); ++peer)
            (*peer)->propagateHellos(directNeighbours);
    }

    unsigned short hiPort = hellos.back().portHigh;
    connectionsIncomming[hiPort] = handler;
    if(connectionsOutgoing.find(hiPort)!=connectionsOutgoing.end())
        newConnectionPairFormed(hiPort);
    return handler;
}

void PeerCollection::update(PeerConnectionHandler *receiver, const Header &h)
{
    MtoHello hello(h.src.port, h.dst.port, (uint16_t)h.length);
    if(!insert(receiver, hello))
        // if hello is invalid or already known
        return;
    
    // Only update direct connections
    if(hello.distance != 0)
        return;
    
    hello.distance++;
    
    // Add peers to a set first, so they don't get double updates
    set<PeerConnectionHandler *> toUpdate;
    for(peers_t::iterator it = peers.begin(); it != peers.end(); ++it)
    {
        peerconns_t& pconns =  it->second.peerConnection;
        for (peerconns_t::const_iterator h = pconns.begin(); h != pconns.end(); ++h)
        {
            toUpdate.insert(*h);
        }
    }
    toUpdate.erase(receiver);
    
    for (set<PeerConnectionHandler *>::const_iterator handler = toUpdate.begin(); handler != toUpdate.end(); ++handler)
        (*handler)->propagateHello(hello);
}

void PeerCollection::newConnectionPairFormed(uint16_t portHigh){
    PeerConnectionHandler* inc = connectionsIncomming[portHigh];
    PeerConnectionHandler* out = connectionsOutgoing[portHigh];
    
    mto->conns.replacePeer(inc, out);
    
    set<PeerConnectionHandler*> allHandlers;
    for (peers_t::iterator it = peers.begin(); it != peers.end(); ++it)
        for (peerconns_t::iterator h = it->second.peerConnection.begin(); h != it->second.peerConnection.end(); ++h)
            allHandlers.insert(*h);
    
    allHandlers.erase(inc);
    allHandlers.erase(out);
    
    for (set<PeerConnectionHandler *>::iterator h = allHandlers.begin(); h != allHandlers.end(); ++h)
        (*h)->replacePeer(inc, out);
    
    // new connections will use proper PeerConnectionHandler
    connectionsIncToOut[inc] = out;
}

template <typename M, typename V> void removeFirstKeyFromMap (M& m, V v)
{
    for(typename M::iterator it = m.begin(); it != m.end(); ++it)
    {
        if(v == it->second) {
			m.erase(it);
			break;
        }
    }
}

void PeerCollection::erase(PeerConnectionHandler *handler)
{
    peers_t::iterator it = peers.begin();
    while ( it != peers.end() )
    {
        peerconns_t& pconns = it->second.peerConnection;
        
        for (peerconns_t::iterator pconn = pconns.begin(); pconn != pconns.end(); ++pconn)
        {
            if (*pconn == handler)
            {
                pconns.erase(pconn);
                break;
            }
        }
        
        if (pconns.empty())
            peers.erase(it++);
        else
            ++it;
    }
    
	removeFirstKeyFromMap(connectionsIncomming, handler);
	removeFirstKeyFromMap(connectionsOutgoing, handler);
    
    mto->peerDied(handler);
    
    connectionsIncToOut.erase(handler);
}


bool PeerCollection::insert(PeerConnectionHandler *handler, const MtoHello & hello)
{
    logger::fine("Parsing hello: %s", hello.str().c_str());
    
    if(peers.find(hello.portHigh)!=peers.end())
    { // already known
        MtoPeer & peer = peers[hello.portHigh];
        if(peer.min!=hello.portLow)
        {
            logger::severe("Port ranges %s and %s overlap. Ignoring second.",
                          peer.bestHello.str().c_str(), hello.str().c_str()
                          );
            return false;
        }
        if(peer.bestHello.distance > hello.distance)
        {
            peer.peerConnection.clear();
            peer.bestHello=hello;
            peer.peerConnection.push_back(handler);
            return true;
        }
        if(peer.bestHello.distance == hello.distance) {
            peer.peerConnection.push_back(handler);
        }
        return false;
    }

    // rebounce of my hello?
    if(hello.matches(mto->hello))
        return false;
    
    // overlapping with myself?
    if (hello.overlaps(mto->hello))
    {
        logger::severe("My port range and %s overlap. Ignoring second.",
                      hello.str().c_str()
                      );
        return false;
    }
    
    // overlapping with my peers?
    peers_t::iterator itH = peers.lower_bound(hello.portHigh), itL = peers.lower_bound(hello.portLow);
    if((itH != peers.end() && itH->second.min < hello.portLow) || itL != itH)
    {
        logger::severe("Port ranges %s and %s overlap. Ignoring second.",
                      itH->second.bestHello.str().c_str(), hello.str().c_str()
                      );
        return false;
    }
    
    peers[hello.portHigh] = MtoPeer(hello.portLow, hello, handler);
    return true;
}

void PeerCollection::introduce(ClientSocket *sock)
{
    for (peers_t::iterator peer = peers.begin(); peer != peers.end(); peer++)
    {
        MtoHello hello = peer->second.bestHello;
        hello.distance++;
        hello.isLastMtoHello=false;
        
        char * data = new char[MtoHello::getSize()];
        hello.serialize(data);

        sock->async_send(MAIN_WRITE_HELLO, data, MtoHello::getSize(), new async_sendlistener_delete, async_service::PLUG_CORK);
    }
    
    mto->hello.isLastMtoHello=true;
    
    char * data = new char[MtoHello::getSize()];
    mto->hello.serialize(data);

    sock->async_send(MAIN_WRITE_HELLO, data, MtoHello::getSize(), new async_sendlistener_delete, async_service::UNPLUG_CORK);
}

void PeerCollection::clear()
{
	size_t sz = connectionsIncomming.size();
	if (sz > 0) logger::fine("Clearing %zu incoming connection(s)...", sz);
	while (!connectionsIncomming.empty()) {
		connectionsIncomming.begin()->second->done();
    }
	
	sz = connectionsIncomming.size();
	if (sz > 0) logger::fine("Clearing %zu outgoing connection(s)...", sz);
	while (!connectionsOutgoing.empty()) {
		connectionsOutgoing.begin()->second->done();
    }
}

