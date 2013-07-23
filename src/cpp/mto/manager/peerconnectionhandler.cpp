#include "peerconnectionhandler.hpp"
#include "localmto.h"

#define PCH_HEADER 1
#define PCH_MAKE_CONNECT 2
#define PCH_RECV_DATA 3
#define PCH_SEND_DATA 4

using namespace muscle;

PeerConnectionHandler::PeerConnectionHandler(ClientSocket * _socket, LocalMto *mto)
: socket(_socket), pendingOperatons(0), closing(false), service(_socket->getServer()), mto(mto)
{
    dataBufffer = new char[Header::getSize()];
    logger::info("Established a connection with peer (receiver is %s)",
                 str().c_str());
    
    // Start normal operation
    readHeader();
}

PeerConnectionHandler::~PeerConnectionHandler()
{
    delete [] dataBufffer;
    delete socket;
}

const endpoint& PeerConnectionHandler::remoteEndpoint() const
{
    return socket->getAddress();
}

void PeerConnectionHandler::readHeader()
{
    incrementPending();
    socket->async_recv(PCH_HEADER, dataBufffer, Header::getSize(), this);
}


bool PeerConnectionHandler::async_received(size_t code, int user_flag, void *data, size_t len, int is_final)
{
    if (closing || is_final == -1)
    {
        if (data && data != dataBufffer)
            delete [] (char *)data;
        return false;
    }
    else if (is_final == 0) return true;
    
    switch(user_flag) {
        case PCH_HEADER:
        {
            Header h(dataBufffer);
            switch(h.type)
            {
                case Header::Connect:
                    handleConnect(h);
                    break;
                case Header::ConnectResponse:
                    handleConnectResponse(h);
                    break;
                case Header::Data:
                    handleData(h);
                    break;
                case Header::Close:
                    handleClose(h);
                    break;
                case Header::PortRangeInfo:
                    handleHello(h);
                    break;
                case Header::PeerClose:
                    errorOccurred("PeerClose");
                    break;
                default:
                    errorOccurred("Unknown message: " + h.type_str() + "!");
                    return false;
            }
            break;
        }
        case PCH_RECV_DATA:
        {
			if (logger::isLoggable(MUSCLE_LOG_FINEST))
				logger::finest("Got data of length %u on %s from %s",
				               latestHeader.length, latestHeader.str().c_str(), str().c_str());
            
            Connection *conn = mto->conns.get(latestHeader);
            
            if(conn)
                conn->send(data, latestHeader.length);
            else
            {
                Identifier id(latestHeader);
                
                if(fwdMap.find(id) != fwdMap.end())
                    fwdMap[id]->forward(latestHeader, latestHeader.length, data);
                else
                    logger::severe("Received data for nonexistent destination");
                
                delete [] (char *)data;
            }
            
            readHeader();
            break;
        }
    }
    return true;
}

void PeerConnectionHandler::handleConnect(Header h)
{
    readHeader();
    
    PeerConnectionHandler *fwdTarget = mto->peers.get(h);
    
    if(fwdTarget)
    {
		if (logger::isLoggable(MUSCLE_LOG_FINEST))
			logger::finest("Forwarding connection %s from %s to %s",
                      h.str().c_str(), str().c_str(), fwdTarget->str().c_str());
        
        
        Identifier id(h);
        fwdTarget->fwdMap.insert(std::pair<Identifier, PeerConnectionHandler*>(id, this));
        fwdTarget->forward(h);
        fwdMap.insert(std::pair<Identifier, PeerConnectionHandler*>(id, fwdTarget));
    }
    else
    {
        if (mto->conns.isAvailable(h.dst))
        {
			if (logger::isLoggable(MUSCLE_LOG_FINEST))
				logger::finest("Trying to establish connection %s (Peer MTO = %s)",
                          h.str().c_str(), str().c_str());
            
            HandleConnected *hc = new HandleConnected(h, this);
        }
        else
            handleConnectFailed(h);
    }
}

void PeerConnectionHandler::handleConnectFailed(Header h)
{
    logger::fine("Rejected connection requested from peer (%s)", h.str().c_str());
    
    h.type = Header::ConnectResponse;
    send(h, 1);
}

PeerConnectionHandler::HandleConnected::HandleConnected(Header& header, PeerConnectionHandler * thiz) : h(header), t(thiz)
{
    h.type = Header::ConnectResponse;
    t->incrementPending();
    t->mto->intSockFactory->async_connect(PCH_MAKE_CONNECT, h.dst, &opts, this);
}

void PeerConnectionHandler::HandleConnected::async_accept(size_t code, int user_flag, muscle::ClientSocket *newSocket)
{
    if(t->closing)
    {
        t->errorOccurred("Can't connect to " + h.str() + "; it is closing.");
        delete newSocket;
        return;
    }
    
    t->send(h, (size_t)0);
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

void PeerConnectionHandler::handleConnectResponse(Header h)
{
    readHeader();
    
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Got info about establish connection %s by %s",
                  h.str().c_str(), str().c_str());
    
    
    Connection *conn = mto->conns.get(h);
    
    if (conn)
        conn->remoteConnected(h);
    else
    {
        Identifier id(h);
        if(fwdMap.find(id)!=fwdMap.end())
        {
            fwdMap[id]->forward(h);
            // Negative response, after sending the response on
            if(h.length)
                fwdMap.erase(id);
        }
        else
            logger::info("Got response for invalid connection");
    }
    
}

void PeerConnectionHandler::handleData(Header h)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Starting data transfer of length %u on %s from %s",
                  h.length, h.str().c_str(), str().c_str());
    
    latestHeader = h;
    char *data = new char[h.length];

    incrementPending();
    socket->async_recv(PCH_RECV_DATA, data, h.length, this);
}

void PeerConnectionHandler::handleClose(Header h)
{
    readHeader();
    
    logger::finest("Got connection close request for %s from %s",
                  h.str().c_str(), str().c_str());
    
    Connection *conn = mto->conns.get(h);
    
    if (conn)
        conn->remoteClosed();
    else
    {
        Identifier id(h);
        if(fwdMap.find(id) != fwdMap.end())
        {
            fwdMap[id]->forward(h);
            fwdMap[id]->fwdMap.erase(id);
            fwdMap.erase(id);
        }
        else
            logger::severe("Closing not established connection");
    }
}

/** 'data' is NOT deleted in this function */
void PeerConnectionHandler::forward(Header & h, size_t dataLen, void *data)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Forwarding '%s' to %s",
                  h.type_str().c_str(), str().c_str());
    
    send(h, data, dataLen);
}

void PeerConnectionHandler::propagateHello(const MtoHello& hello)
{
    endpoint src("", hello.portLow);
    endpoint dst("", hello.portHigh);

    Header h(Header::PortRangeInfo, src, dst, hello.distance + 1);
    
    send(h);
}

void PeerConnectionHandler::propagateHellos(std::vector< MtoHello >& hellos)
{
    for (std::vector<MtoHello>::const_iterator it = hellos.begin(); it != hellos.end(); it++)
        propagateHello(*it);
}

void PeerConnectionHandler::handleHello(Header h)
{
    readHeader();
    
    logger::finest("Got hello from %s for %d-%d",
                  str().c_str(),h.src.port,h.dst.port);
    
    mto->peers.update(this, h);
}

void PeerConnectionHandler::done()
{
    if (closing)
    {
        tryClean();
        return;
    }
    incrementPending();
    
    for(std::map<Identifier, PeerConnectionHandler*>::const_iterator it =  fwdMap.begin(); it!=fwdMap.end(); ++it)
    {
        Header h(Header::Close, it->first);
        it->second->forward(h);
        it->second->fwdMap.erase(it->first);
    }
    
    closing = true;
    decrementPending();
}

void PeerConnectionHandler::errorOccurred(std::string message)
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

bool PeerConnectionHandler::tryClean()
{
    if (closing)
    {
        if (pendingOperatons == 0)
        {
            mto->peers.erase(this);
            return true;
        }
        else
            service->erase(socket);
    }
    return false;
}

void PeerConnectionHandler::send(Header& h, size_t value, muscle::async_sendlistener *_sender)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Writing '%s' to %s",
                  h.type_str().c_str(), str().c_str());

    char *packet;
    size_t sz = h.makePacket(&packet, value);
    send(packet, sz, _sender);
}

void PeerConnectionHandler::send(Header& h, void *data, size_t len, muscle::async_sendlistener *_sender)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Writing '%s' to %s",
                  h.type_str().c_str(), str().c_str());
    char *packet;
    size_t sz = h.makePacket(&packet, len);
    send(packet, sz, _sender);
	if (data)
		send(data, len, _sender);
}

void PeerConnectionHandler::send(void *data, size_t len, muscle::async_sendlistener *_sender)
{
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("Sending directly %u bytes on %s", len, str().c_str());
    
    Sender *s = new Sender(_sender, this, data, len);
}

PeerConnectionHandler::Sender::Sender(muscle::async_sendlistener *listener, PeerConnectionHandler *t, void *data, size_t len) : listener(listener), t(t)
{
    t->incrementPending();
    t->socket->async_send(PCH_SEND_DATA, data, len, this);
}

void PeerConnectionHandler::Sender::async_sent(size_t code, int user_flag, void *data, size_t len, int is_final)
{
    if (listener)
        listener->async_sent(code, user_flag, data, len, is_final);
    
    if (is_final == 0)
        return;
}

void PeerConnectionHandler::Sender::async_report_error(size_t code, int flag, const muscle::muscle_exception& ex)
{
    t->async_report_error(code, flag, ex);
    if (listener)
        listener->async_report_error(code, flag, ex);
}

void PeerConnectionHandler::Sender::async_done(size_t code, int user_flag)
{
    if (listener)
        listener->async_done(code, user_flag);
    t->decrementPending();
    delete this;
}

void PeerConnectionHandler::async_report_error(size_t code, int user_flag, const muscle::muscle_exception& ex)
{
    switch (user_flag) {
        case PCH_HEADER:
            errorOccurred("Could not read header: " + std::string(ex.what()));
            break;
        case PCH_RECV_DATA:
            errorOccurred("Could not read data: " + std::string(ex.what()));
            break;
        case PCH_SEND_DATA:
            errorOccurred("Could not send data: " + std::string(ex.what()));
            break;
        default:
            break;
    }
}

void PeerConnectionHandler::replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to)
{
    for (std::map<Identifier, PeerConnectionHandler*>::iterator it = fwdMap.begin(); it != fwdMap.end(); ++it) {
        if(it->second == from)
            it->second = to;
    }
}

void PeerConnectionHandler::peerDied(PeerConnectionHandler* handler)
{
    std::map<Identifier, PeerConnectionHandler*>::iterator it = fwdMap.begin();
    
    // Need a while loop to be able to call erase
    while ( it != fwdMap.end() ) {
        if(it->second == handler){
            Header h(0, it->first);
            PeerConnectionHandler *newHandler = mto->peers.get(h);
            if (newHandler)
                it->second = newHandler;
            else
            {
                fwdMap.erase(it++);
                // Don't increase iterator twice
                continue;
            }
        }
        ++it;
    }
}

