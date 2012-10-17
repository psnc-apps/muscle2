#include "communicator.hpp"
#include "exception.hpp"
#include <strings.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <unistd.h>
#include <errno.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>

extern "C" int communicator_read_from_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
	cout << "xdr_read_from_socket:" << buf_len << endl;
#endif
	return read(*(int *)socket_handle, buf, buf_len);
}

extern "C" int communicator_write_to_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
	cout << "xdr_write_to_socket:" << buf_len << endl;
#endif
	return write(*(int *)socket_handle, buf, buf_len);
}

namespace muscle {

void Communicator::connect_socket(const char *hostname, int port)
{
	struct hostent *server;
	int res;
	
	if (port <= 0) throw muscle_exception("no such port");
	uint16_t hport = htons(port);
	
	server = gethostbyname(hostname);
	if (server == NULL)
	{
		server = gethostbyname2(hostname, AF_INET6);
		if (server == NULL) throw muscle_exception("no such host");
		res = connect_socket_ipv6(server, hport);
	}
	else res = connect_socket_ipv4(server, hport);		
    	if (res < 0)
	{
		sockfd = -1;
		logger::severe("could not connect: %s", strerror(errno));
		throw muscle_exception("could not connect");
	}
}

int Communicator::connect_socket_ipv4(struct hostent *server, uint16_t port)
{
	struct sockaddr_in serv_addr;

	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	bcopy((char *)server->h_addr,
		(char *)&serv_addr.sin_addr.s_addr,
		server->h_length);
	serv_addr.sin_port = port;

	sockfd = socket(PF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) throw muscle_exception("can not create socket");	
	return connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr));
}

int Communicator::connect_socket_ipv6(struct hostent *server, uint16_t port)
{
	struct sockaddr_in6 serv_addr;

	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin6_family = AF_INET6;
	bcopy((char *)server->h_addr,
		(char *)&serv_addr.sin6_addr.s6_addr,
		server->h_length);
	serv_addr.sin6_port = port;

	sockfd = socket(PF_INET6, SOCK_STREAM, 0);
	if (sockfd < 0) throw muscle_exception("can not create socket");	
	
	return connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr));
}

std::string Communicator::retrieve_string(muscle_protocol_t opcode, std::string *name) {
	char *str = (char *)0;
	size_t len = 65536;
	execute_protocol(opcode, name, MUSCLE_STRING, NULL, 0, &str, &len);
	std::string str_out(str);
	free_data(str, MUSCLE_STRING);
	return str_out;
}

}
