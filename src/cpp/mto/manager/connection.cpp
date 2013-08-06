
#include "connection.hpp"
#include "peercollection.h"
#include "peerconnectionhandler.hpp"
#include "localmto.h"

#include <cassert>

#define CONN_REMOTE_TO_LOCAL 1 // don't free
#define CONN_CONN_REMOTE 2 // do free
#define CONN_LOCAL_TO_REMOTE_R 3 // don't free
#define CONN_SEND_DATA 4 // don't free
#define CONN_TIMEOUT_CLOSE 5 // don't free
#define CONN_RESTART_RECEIVE 6 // don't free

using namespace muscle;

//const muscle::duration Connection::recvTimeout(0l, 1000);

/* from remote */
Connection::Connection(Header h, ClientSocket* s, PeerConnectionHandler* remoteMto, LocalMto *mto, bool remotePeerConnected)
: sock(s), header(h), closing(false), hasRemotePeer(remotePeerConnected), pendingOperations(0), remoteMto(remoteMto), mto(mto), closing_timer(0), receiving_timer(0)
{
    assert(remoteMto != NULL);
    if (hasRemotePeer)
    {
        logger::fine("Established client connection (%s), initiated by peer %s",
                      h.str().c_str(), remoteMto->str().c_str());
        header.type = Header::Data;
        
        receive();
    }
    else
    {
        remoteMto->sendHeader(header);
        
        logger::fine("Requesting connection to host %s from peer %s",
                      header.dst.str().c_str(), remoteMto->str().c_str());
    }
}

void Connection::close()
{
    if(closing)
        return;

    // Will start a timer after this, and protects from untimely deletion
	pendingOperations++;
	
    closing = true;
    logger::fine("Closing connection %s",
                  header.str().c_str());
	
	if (receiving_timer)
		sock->getServer()->erase_timer(receiving_timer);
    
    if(hasRemotePeer && remoteMto) {
        header.type=Header::Close;
		remoteMto->sendHeader(header);
    }
    
    mto->conns.erase(header);
	
	if (pendingOperations > 1) {	// We have more than the timer running
		muscle::time timer = duration(10l, 0).time_after();
		closing_timer = sock->getServer()->timer(CONN_TIMEOUT_CLOSE, timer, this, (void *)0);
	} else {
		pendingOperations--;
	}
    tryClose();
}

void Connection::tryClose()
{
    if (closing && pendingOperations == 0)
    {
        if (closing_timer)
            sock->getServer()->erase_timer(closing_timer);

        logger::fine("Connection %s closed",
                      header.str().c_str());
        delete this;
    }
}


void Connection::async_done(size_t code, int flag)
{
    pendingOperations--;
    tryClose();
}

Connection::~Connection()
{
	logger::finer("Deleting connection");
    delete sock;
}

void Connection::async_execute(size_t code, int flag, void *user_data)
{
	// If the reference count is more than one, we will cancel this timer
	// but also the socket that is still active
	async_service * const server = sock->getServer();

	if (flag == CONN_RESTART_RECEIVE) {
		receive();
		server->erase_timer(receiving_timer);
		receiving_timer = 0;
	}
	else {
		if (pendingOperations > 1) {
			logger::info("Connection %s did not close after timeout (%d connections running); forcing",
						  header.str().c_str(), pendingOperations);
			server->printDiagnostics();
			sock->async_cancel();
		}
		// Prevent double delete
		const size_t tmpTimer = closing_timer;
		closing_timer = 0;
		// Will call async_done and delete the connection
		server->erase_timer(tmpTimer);
	}
}

void Connection::receive(void *buffer, size_t sz)
{
	if (closing) {
		delete [] (char *)buffer;
		return;
	}
	
	pendingOperations++;
	sock->async_recv(CONN_LOCAL_TO_REMOTE_R, buffer, sz, this);
}

bool Connection::async_received(size_t code, int user_flag, void *buffer, void *last_data_ptr, size_t count, int is_final)
{
    // Error occurred
    if(is_final == -1 || closing) return false;
    if (count == 0) return true;
		
	// If the received size is not too small, it's faster to just allocate some new memory
	// Copying is almost always slower
	if (count >= MTO_CONNECTION_BUFFER_SIZE/100) {
		remoteMto->send(header, buffer, count);

        receive();
		// On large messages, use a pacing rate
		//muscle::time t = recvTimeout.time_after();
		//receiving_timer = sock->getServer()->timer(CONN_RESTART_RECEIVE, t, this, NULL);
	} else if (count > 1) {
		// Create new buffer to send, reuse the current buffer
		char *sendData = new char[count];
		memcpy(sendData, buffer, count);
		remoteMto->send(header, sendData, count);
		
		receive(buffer, MTO_CONNECTION_BUFFER_SIZE);
	} else {
		// Just send the one received byte in the header
		header.type = Header::DataInLength;
		remoteMto->sendHeader(header, *(unsigned char *)buffer);
		header.type = Header::Data;
		receive(buffer, MTO_CONNECTION_BUFFER_SIZE);
	}
    
	// Don't try to receive more information, we sent what we received
    return false;
}

void Connection::send(void *data, size_t length, int user_flag)
{
    if (closing) {
		logger::info("Closing connection %s, will not send %zu bytes of data.", header.str().c_str(), length);
		delete [] (char *)data;
        return;
	}
    
	if (logger::isLoggable(MUSCLE_LOG_FINEST))
		logger::finest("[__W] Sending directly %zu bytes on %s", length, sock->getAddress().str().c_str());
    
    pendingOperations++;
    sock->async_send(user_flag, data, length, this, 0);
}

void Connection::async_sent(size_t, int, void *data, size_t, int is_final)
{
    if (!is_final) return;
    
	delete [] (char *)data;
}

void Connection::async_report_error(size_t code, int user_flag, const muscle_exception &ex)
{
    if(closing)
        return;

    logger::severe("Error occurred in connection between %s (%s)",
                  header.str().c_str(), ex.what());
    close();
}


void Connection::remoteConnected(Header h)
{
    if (closing)
        return;

    const size_t response = h.length;

    h.type = Header::ConnectResponse;
    char *packet;
    const size_t len = h.makePacket(&packet, response);
	send(packet, len, CONNECT);
    
    if (response) { // Fail
        logger::fine("Got negative response for connection request (%s)",
                      header.str().c_str());
        close();
    } else { // Success
        logger::fine("Remote connection succeeded (%s)",
                      header.str().c_str());
        header.type = Header::Data;
        hasRemotePeer = true;
        receive();
    }
}

void Connection::remoteClosed()
{
    hasRemotePeer = false;
    close();
}

void Connection::peerDied(PeerConnectionHandler* handler)
{
    if(remoteMto == handler) {
        remoteMto = mto->peers.get(header);
        
        if(remoteMto == NULL)
            close();
    }
}

void Connection::replacePeer(PeerConnectionHandler* from, PeerConnectionHandler* to)
{
    if(remoteMto == from)
        remoteMto = to;
}
