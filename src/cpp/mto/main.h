#ifndef MAIN_H
#define MAIN_H

#include <boost/asio/ip/tcp.hpp>

#include "logger.h"

struct Header;
class PeerConnectionHandler;

/** Gets MTO that handles this port. Returns 0 if no such MTO exists. */
PeerConnectionHandler * getPeer(unsigned short port);

/** Gets MTO that handles this port. Returns 0 if no such MTO exists. */
boost::asio::ip::tcp::socket * getPeerSocket(unsigned short port);

/** Handles properly information about new MTO available via the receiver */
void helloReceived(Header h, PeerConnectionHandler * receiver);

/** Informs the MTO that connection to anther MTO has been lost */
void peerDied(PeerConnectionHandler *, bool reconnect);

#endif // LOGGER_H