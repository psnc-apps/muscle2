#include "communicator.hpp"
#include <boost/asio.hpp>

extern "C" int communicator_read_from_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
	cout << "xdr_read_from_socket:" << buf_len << endl;
#endif
	return ((tcp::socket *)socket_handle)->read_some(boost::asio::buffer(buf, buf_len));
}

extern "C" int communicator_write_to_socket(void *socket_handle, void *buf, int buf_len)
{
#ifdef CPPMUSCLE_TRACE
	cout << "xdr_write_to_socket:" << buf_len << endl;
#endif
	return boost::asio::write(*((tcp::socket *)socket_handle), boost::asio::buffer(buf, buf_len));
}

namespace muscle {

void Communicator::connect_socket(boost::asio::ip::address_v4 host, int port)
{
	boost::asio::io_service io_service;

	s = new tcp::socket(io_service);

	s->connect(tcp::endpoint(host, port));
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
