
#include "connection.hpp"
#include "peercollection.h"
#include "peerconnectionhandler.hpp"
#include "localmto.h"

#include <cassert>

#define RECEIVE_TIMEOUT_US 500
#define RECEIVE_THRESHOLD_SZ 10240

using namespace muscle;

const muscle::duration Connection::recvTimeout(0l, RECEIVE_TIMEOUT_US);

/* from remote */
Connection::Connection(Header h, ClientSocket* s, PeerConnectionHandler* remoteMto, LocalMto *mto, bool remotePeerConnected)
: sock(s), header(h), closing(false), hasRemotePeer(remotePeerConnected), pendingOperations(0), remoteMto(remoteMto), mto(mto), closing_timer(0), receiving_timer(0), lastReceivedData(NULL), lastReceivedSize(0)
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
		closing_timer = sock->getServer()->timer(TIMER_CLOSE, timer, this, (void *)0);
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

	if (flag == TIMER_RECEIVE) {
		// Stop receiving
		logger::finer("Restart receive timer expired. Sending %zu bytes.", lastReceivedSize);
		server->erase_socket(sock->getReadSock(), -1, -1);
		remoteMto->send(header, lastReceivedData, lastReceivedSize);
		lastReceivedData = NULL;
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
			if (receiving_timer) {
				server->erase_timer(receiving_timer);
				receiving_timer = 0;
				delete [] (char *)lastReceivedData;
			}
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
	sock->async_recv(RECEIVE, buffer, sz, this);
}

bool Connection::async_received(size_t code, int user_flag, void *buffer, void *last_data_ptr, size_t count, int is_final)
{
    // Error occurred
    if(is_final == -1 || closing) {
		if (buffer != lastReceivedData)
			delete [] (char *)buffer;
		return false;
	}
    if (count == 0) return true;
	
	if (receiving_timer) {
		// Previously we got a large data packet and we were expecting more.
		// The expectation has now come true, so keep on with the same expectation
		// until the buffer is full, and then send the data.
		// If nothing more comes before the recvTimeout expires, in async_execute send
		// all that we got.
		if (is_final) {
			remoteMto->send(header, buffer, count);
			sock->getServer()->erase_timer(receiving_timer);
			receiving_timer = 0;
			lastReceivedData = NULL;
			receive();
		} else {
			lastReceivedSize += count;
			muscle::time t = recvTimeout.time_after();
			sock->getServer()->update_timer(receiving_timer, t, NULL);
		}
	} else if (is_final) {
		remoteMto->send(header, buffer, count);
		receive();
	} else if (count >= RECEIVE_THRESHOLD_SZ) {
		// If the received size is not too small, probably we can expect some more
		// so store the data now.
		lastReceivedSize = count;
		lastReceivedData = buffer;

		muscle::time t = recvTimeout.time_after();
		pendingOperations++;
		receiving_timer = sock->getServer()->timer(TIMER_RECEIVE, t, this, NULL);
	} else if (count > 1) {
		// Create new buffer to send, reuse the current buffer
		char *sendData = new char[count];
		// Copying is almost always slower
		memcpy(sendData, buffer, count);
		remoteMto->send(header, sendData, count);
		
		receive(buffer, MTO_CONNECTION_BUFFER_SIZE);
		// Don't try to receive more information, we sent what we received
		return false;
	} else {
		// Just send the one received byte in the header
		header.type = Header::DataInLength;
		remoteMto->sendHeader(header, *(unsigned char *)buffer);
		header.type = Header::Data;
		receive(buffer, MTO_CONNECTION_BUFFER_SIZE);
		// Don't try to receive more information, we sent what we received
		return false;
	}
	// Keep on receiving until the timer expires
    return true;
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
