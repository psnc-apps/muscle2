/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#ifndef MAIN_H
#define MAIN_H

#include <boost/asio/ip/tcp.hpp>

#include "logger.hpp"

struct Header;
class PeerConnectionHandler;

/** Gets MTO that handles this port. Returns 0 if no such MTO exists. */
PeerConnectionHandler * getPeer(Header header);

/** If the argument is an incomming connection to some MTO and also outgoing connection exists, returns outgoing one. Otherwise returns argument */
PeerConnectionHandler * getPeer(PeerConnectionHandler * peer);

/** Handles properly information about new MTO available via the receiver */
void helloReceived(Header h, PeerConnectionHandler * receiver);

/** Informs the MTO that connection to anther MTO has been lost */
void peerDied(PeerConnectionHandler *, bool reconnect);

#endif // MAIN_H

