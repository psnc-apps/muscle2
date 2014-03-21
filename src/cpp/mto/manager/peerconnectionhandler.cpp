#include "peerconnectionhandler.hpp"
#include "localmto.h"

using namespace muscle;
using namespace std;
using namespace muscle::net;

PeerConnectionHandler::PeerConnectionHandler(ClientSocket * _socket, LocalMto *mto)
: socket(_socket), pendingOperations(0), closing(false), service(_socket->getServer()), mto(mto)
{
    headerBuffer = new char[Header::getSize()];
    logger::info("Established a connection with peer (receiver is %s)",
                 str().c_str());
    
    // Start normal operation
    readHeader();
}

PeerConnectionHandler::~PeerConnectionHandler()
{
    delete [] headerBuffer;
    delete socket;
}

const endpoint& PeerConnectionHandler::address() const
{
    return socket->getAddress();
}

void PeerConnectionHandler::readHeader()
{
    incrementPending();
    socket->async_recv(RECV_HEADER, headerBuffer, Header::getSize(), this);
}


bool PeerConnectionHandler::async_received(size_t code, int user_flag, void *data, void *last_data_ptr, size_t len, int is_final)
{
    if (closing || is_final == -1)
    {
        if (data && data != headerBuffer)
            delete [] (char *)data;
        return false;
    }
	if (is_final == 0) return true;
    
	bool nextPacketIsHeader = true;
	
    if (user_flag == RECV_HEADER) {
		Header h(headerBuffer);
		switch(h.type)
		{
			case Header::Data:
				handleData(h);
				nextPacketIsHeader = false;
				break;
			case Header::DataInLength:
				handleDataInLength(h, code);
				// this will be done when async_receive is called again
				nextPacketIsHeader = false;
				break;
			case Header::Connect:
				handleConnect(h);
				break;
			case Header::ConnectResponse:
				handleConnectResponse(h);
				break;
			case Header::Close:
				handleClose(h);
				break;
			case Header::PortRangeInfo:
				handleHello(h);
				break;
			default:
				errorOccurred("Unknown message: " + h.type_str() + "!");
				return false;
		}
	} else if (user_flag == RECV_DATA) {
		if (logger::isLoggable(MUSCLE_LOG_FINEST))
			logger::finest("Got data of length %u on %s from %s",
						   latestHeader.length, latestHeader.str().c_str(), str().c_str());
		
		Connection *conn = mto->conns.get(latestHeader);
		
		if(conn) {
			conn->send(data, latestHeader.length);
		} else if (!forwardToPeer(latestHeader, false, data, latestHeader.length)) {
			delete [] (char *)data;
			logger::severe("Received data for nonexistent destination");
		}
    }
	
	if (nextPacketIsHeader)
		readHeader();
	
    return true;
}

void PeerConnectionHandler::handleConnect(Header &h)
{
    PeerConnectionHandler *fwdTarget = mto->peers.get(h);
    
    if(fwdTarget)
    {
		if (logger::isLoggable(MUSCLE_LOG_FINEST))
			logger::finest("Forwarding connection %s from %s to %s",
                      h.str().c_str(), str().c_str(), fwdTarget->str().c_str());
        
        Identifier id(h);
        fwdTarget->fwdMap.insert(pair<Identifier, PeerConnectionHandler*>(id, this));
        fwdMap.insert(pair<Identifier, PeerConnectionHandler*>(id, fwdTarget));
		fwdTarget->sendHeader(h);
    }
    else if (mto->conns.isAvailable(h.dst)) {
		if (logger::isLoggable(MUSCLE_LOG_FINEST))
			logger::finest("Trying to establish connection %s (Peer MTO = %s)",
			               h.str().c_str(), str().c_str());
		
    	incrementPending();
		// Self-destruct
		new HandleConnected(h, this);
	} else {
		handleConnectFailed(h);
	}
}

void PeerConnectionHandler::handleConnectFailed(Header &h)
{
    logger::fine("Rejected connection requested from peer (%s)", h.str().c_str());
    
    h.type = Header::ConnectResponse;
    sendHeader(h, 1);
}

PeerConnectionHandler::HandleConnected::HandleConnected(Header& header, PeerConnectionHandler * thiz) : h(header), t(thiz)
{
    h.type = Header::ConnectResponse;
    t->mto->intSockFactory->async_connect(LOCAL_CONNECT, h.dst, &opts, this);
}

void PeerConnectionHandler::HandleConnected::async_accept(size_t code, int user_flag, muscle::net::ClientSocket *newSocket)
{
    if(t->closing) {
        t->errorOccurred("Can't connect to " + h.str() + "; it is closing.");
        delete newSocket;
        return;
    }
    
    t->sendHeader(h, 0);
    t->mto->conns.create(newSocket, h, t, true);
}

void PeerConnectionHandler::HandleConnected::async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex)
{
    t->handleConnectFailed(h);
}

void PeerConnectionHandler::HandleConnected::async_done(size_t code, int user_flag)
{
    t->async_done(code, user_flag);
    delete this;
}

void PeerConnectionHandler::handleConnectResponse(Header &h)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Got info about establish connection %s by %s",
                  h.str().c_str(), str().c_str());
    
    
    Connection *conn = mto->conns.get(h);
    
    if (conn) {
        conn->remoteConnected(h);
    } else if (!forwardToPeer(h, (h.length == 1))) { // Negative response,
													 //	after sending the response on
		logger::info("Got response for invalid connection");
    }
    
}

void PeerConnectionHandler::handleDataInLength(const Header &h, const size_t code)
{
	// Get the data from the last 8 bits
	unsigned char *data = new unsigned char[1];
	*data = h.length & 0xff;
	latestHeader = h;
	latestHeader.type = Header::Data;
	latestHeader.length = 1;
	async_received(code, RECV_DATA, data, data, 1, 1);
}

void PeerConnectionHandler::handleData(const Header &h)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Starting data transfer of length %u on %s from %s",
                  h.length, h.str().c_str(), str().c_str());
    
    latestHeader = h;
    char *data = new char[h.length];

    incrementPending();
    socket->async_recv(RECV_DATA, data, h.length, this);
}

void PeerConnectionHandler::handleClose(Header &h)
{
    logger::finest("Got connection close request for %s from %s",
                  h.str().c_str(), str().c_str());
    
    Connection *conn = mto->conns.get(h);
    
    if (conn) {
        conn->remoteClosed();
	} else if (!forwardToPeer(h, true)) {
		logger::severe("Closing not established connection");
    }
}

void PeerConnectionHandler::handleHello(Header &h)
{
    logger::finest("Got hello from %s for %d-%d",
				   str().c_str(),h.src.port,h.dst.port);
    
    mto->peers.update(this, h);
}

bool PeerConnectionHandler::forwardToPeer(Header &h, bool erase, void *data, size_t dataLen)
{
	Identifier id(h);
	map<Identifier, PeerConnectionHandler*>::iterator it = fwdMap.find(id);
	if (it == fwdMap.end())
		return false;

	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Forwarding '%s' to %s",
					   h.type_str().c_str(), it->second->str().c_str());

	it->second->send(h, data, dataLen);
	if (erase) {
		it->second->fwdMap.erase(id);
		fwdMap.erase(it);
	}
	return true;
}

void PeerConnectionHandler::propagateHello(const MtoHello& hello)
{
    endpoint src("", hello.portLow);
    endpoint dst("", hello.portHigh);

    Header h(Header::PortRangeInfo, src, dst, hello.distance + 1);
    
    sendHeader(h);
}

void PeerConnectionHandler::propagateHellos(vector< MtoHello >& hellos)
{
    for (vector<MtoHello>::const_iterator it = hellos.begin(); it != hellos.end(); it++)
        propagateHello(*it);
}

void PeerConnectionHandler::done()
{
    if (closing) {
        tryClose();
        return;
    }
    incrementPending();
    
    while (!fwdMap.empty()) {
		logger::fine("Closing forward connection to %s", fwdMap.begin()->first.str().c_str());
		Header h(Header::Close, fwdMap.begin()->first);
        forwardToPeer(h, true);
    }
    
    closing = true;
    decrementPending();
}

void PeerConnectionHandler::errorOccurred(string message)
{
    if (!closing)
    {
        if(message=="PeerClose")
            logger::severe("Peer %s closed it's idle connection", str().c_str());
        else
            logger::severe("Peer connection error: '%s'. Closing peer connection to %s", message.c_str(), str().c_str());
    }

    done();
}

void PeerConnectionHandler::tryClose()
{
    assert(closing);

	if (pendingOperations == 0) {
		--pendingOperations; // Go into a negative regime; do not erase again.
		mto->peers.erase(this);
		delete this;
	} else {
		socket->async_cancel();
	}
}

void PeerConnectionHandler::sendHeader(Header& h, size_t value)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Writing '%s' to %s",
                  h.type_str().c_str(), str().c_str());

    char *packet;
    size_t sz = h.makePacket(&packet, value);

    incrementPending();
    socket->async_send(SEND_HEADER, packet, sz, this, 0);
}

void PeerConnectionHandler::send(Header& h, void *data, const size_t len)
{
	// If the data fits into the length variable, do it,
	// it might well save a round trip.
	if (len == 1 && h.type == Header::Data) {
		h.type = Header::DataInLength;
		sendHeader(h, *(unsigned char *)data);
		delete [] (char *)data;
		return;
	}

	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Writing '%s' to %s",
                  h.type_str().c_str(), str().c_str());
	
	char *packet;
	const size_t sz = h.makePacket(&packet, len);
	
	incrementPending();
	incrementPending();
	socket->async_send(SEND_HEADER, packet, sz, this, async_service::PLUG_CORK);
	socket->async_send(SEND_DATA, data, len, this, async_service::UNPLUG_CORK);
}

void PeerConnectionHandler::async_sent(size_t code, int user_flag, void *data, size_t len, int is_final)
{
	if (is_final) delete [] (char *)data;
}

void PeerConnectionHandler::async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex)
{
    switch (user_flag) {
        case RECV_HEADER:
            errorOccurred("Could not read header: " + string(ex.what()));
            break;
        case RECV_DATA:
            errorOccurred("Could not read data: " + string(ex.what()));
            break;
        case SEND_DATA:
            errorOccurred("Could not send data: " + string(ex.what()));
            break;
        case SEND_HEADER:
            errorOccurred("Could not send header: " + string(ex.what()));
            break;
        default:
            break;
    }
}

void PeerConnectionHandler::replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to)
{
    for (map<Identifier, PeerConnectionHandler*>::iterator it = fwdMap.begin(); it != fwdMap.end(); ++it) {
        if(it->second == from)
            it->second = to;
    }
}

void PeerConnectionHandler::peerDied(PeerConnectionHandler* handler)
{
    map<Identifier, PeerConnectionHandler*>::iterator it = fwdMap.begin();
    
    // Need a while loop to be able to call erase
    while (it != fwdMap.end() ) {
        if(it->second == handler){
            Header h(0, it->first);

            PeerConnectionHandler *newHandler = mto->peers.get(h);
            if (newHandler) {
                it->second = newHandler;
				++it;
			} else {
                fwdMap.erase(it++);
            }
        }
    }
}

