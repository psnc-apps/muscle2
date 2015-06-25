
#include "externalconnection.hpp"
#include "peercollection.h"
#include "peerconnectionhandler.hpp"
#include "localmto.h"
#include "muscle2/util/csocket.h"

#include <cassert>

#define RECEIVE_TIMEOUT_US 500
#define RECEIVE_THRESHOLD_SZ 10240

using namespace muscle;
using namespace muscle::net;
using namespace muscle::util;

/* from remote */
ExternalConnection::ExternalConnection(ClientSocket * const s, Header h, SocketFactory * const factory)
: sock(s), header(h), pendingOperations(1), remoteSock(NULL)
{
    sockOpts = new socket_opts;
    h.dst.resolve();
    factory->async_connect(EXT_CONNECT, h.dst, sockOpts, this);
}

void ExternalConnection::async_done(size_t code, int flag)
{
    pendingOperations--;
    
    if (pendingOperations == 0)
    {
        logger::fine("External connection %s closed",
                     header.str().c_str());
        delete this;
    }
}

ExternalConnection::~ExternalConnection()
{
	logger::finer("Deleting external connection");
    delete sock;
    delete remoteSock;
}

void ExternalConnection::receive(ClientSocket *sock, int user_flag, void *buffer, size_t sz)
{
	pendingOperations++;
	sock->async_recv(user_flag, buffer, sz, this);
}

bool ExternalConnection::async_received(size_t code, int user_flag, void *buffer, void *last_data_ptr, size_t count, int is_final)
{
    if (user_flag == INT_RECEIVE) {
        return forward(sock, remoteSock, (char *)buffer, count, is_final == -1, INT_RECEIVE, EXT_SEND);
    } else {
        return forward(remoteSock, sock, (char *)buffer, count, is_final == -1, EXT_RECEIVE, INT_SEND);
    }
}

bool ExternalConnection::forward(ClientSocket * const fromSock, ClientSocket * const toSock, char * const buffer, const size_t count, const bool is_eof, const int recvFlag, const int sendFlag)
{
    if (is_eof) {
        delete [] buffer;
        return false;
    }

    // Receive some more information
    if (count == 0) return true;
    
    char *sendData = new char[count];
    memcpy(sendData, buffer, count);

    receive(fromSock, recvFlag, buffer, EXT_MTO_CONNECTION_BUFFER_SIZE);
    send(toSock, sendFlag, sendData, count);
    
    // Don't try to receive more information, we sent what we received
    return false;
}

void ExternalConnection::send(ClientSocket *sock, int user_flag, void *data, size_t length)
{
    if (logger::isLoggable(MUSCLE_LOG_FINEST)) {
        char *buffer = (char *)data;
        buffer[length] = '\0';
        logger::finest("[__W] Sending directly %zu bytes on %s", length, sock->getAddress().str().c_str());
        logger::finest("[__W] Sending %s", buffer);
    }
    
    pendingOperations++;
    sock->async_send(user_flag, data, length, this, 0);
}

void ExternalConnection::async_sent(size_t, int, void *data, size_t, int is_final)
{
    if (is_final) delete [] (char *)data;
}

void ExternalConnection::async_accept(size_t code, int user_flag, muscle::net::ClientSocket *newSocket)
{
    remoteSock = newSocket;
    remoteConnected(true);
}

void ExternalConnection::async_report_error(size_t code, int user_flag, const muscle_exception &ex)
{
    if (user_flag == EXT_CONNECT) {
        remoteConnected(false);
    } else if (user_flag == INT_SEND || user_flag == EXT_SEND) {
        sock->async_cancel();
        remoteSock->async_cancel();
    } else if (user_flag == EXT_RECEIVE) {
        sock->getServer()->erase_socket(sock->getReadSock(), -1, -1);
    } else if (user_flag == INT_RECEIVE) {
        return; // don't report
    }

    logger::severe("Error occurred in connection between %s (%s)",
                  header.str().c_str(), ex.what());
}

void ExternalConnection::remoteConnected(bool success)
{
    delete sockOpts;
    sockOpts = NULL;

    header.type = Header::ConnectResponse;
    char *packet;
    const size_t len = header.makePacket(&packet, success ? 0 : 1);
    send(sock, INT_CONNECT_RESPONSE, packet, len);
    
    if (success) {
        logger::fine("Remote connection succeeded (%s)",
                     header.str().c_str());
        receive(sock, INT_RECEIVE, new char[EXT_MTO_CONNECTION_BUFFER_SIZE], EXT_MTO_CONNECTION_BUFFER_SIZE);
        receive(remoteSock, EXT_RECEIVE, new char[EXT_MTO_CONNECTION_BUFFER_SIZE], EXT_MTO_CONNECTION_BUFFER_SIZE);
    } else {
        logger::fine("Got negative response for connection request (%s)",
                     header.str().c_str());
    }
}
