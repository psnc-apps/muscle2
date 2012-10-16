#include "communicator.hpp"
#include "exception.hpp"
#include <strings.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <unistd.h>
#include <sys/socket.h>
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
	struct sockaddr_in serv_addr;
	struct hostent *server;
	server = gethostbyname(hostname);
	if (server == NULL) throw muscle_exception("no such host");
	if (port <= 0) throw muscle_exception("no such port");
    	
	bzero((char *) &serv_addr, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	bcopy((char *)server->h_addr, 
		(char *)&serv_addr.sin_addr.s_addr,
		server->h_length);
	serv_addr.sin_port = htons(port);

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) throw muscle_exception("can not create socket");	
	if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0)
	{
		sockfd = -1;
		throw muscle_exception("could not connect");
	}
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
