//
//  endpoint.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "endpoint.h"

#include <netdb.h>
#include <sstream>
#include <stdlib.h>
#include <unistd.h>
#include <cstring>
#include <arpa/inet.h>

using namespace muscle;

const char endpoint::IPV4_FLAG = 1;
const char endpoint::IPV6_FLAG = 2;

endpoint::endpoint(std::string _host, const uint16_t _port) : is_ipv6(false), addr(), host(_host), port(_port)
{}

endpoint::endpoint() : is_ipv6(false), addr(), host(""), port(0)
{}

endpoint::endpoint(const char *buffer_ptr) : host("")
{
	is_ipv6 = *buffer_ptr++ == IPV6_FLAG;
	memcpy(addr, buffer_ptr, sizeof(addr));
	
	buffer_ptr += sizeof(addr);
	
	port = ntohs(*(const uint16_t *)buffer_ptr);
}

endpoint::endpoint(uint16_t port_) : addr()
{
	port = port_;
	const size_t max_hostname = 256;
	char hostname[max_hostname];
	hostname[max_hostname - 1] = '\0';
	if (gethostname(hostname, max_hostname) == -1) {
		throw muscle_exception("Could not find hostname");
	}
	host = std::string(hostname);
}

std::string endpoint::getHostFromAddress() const
{
	assertValid();
	if (is_ipv6)
	{
		char hostname[INET6_ADDRSTRLEN];
		inet_ntop(AF_INET6, addr, hostname, INET6_ADDRSTRLEN);
		return std::string(hostname);
	}
	else
	{
		char hostname[INET_ADDRSTRLEN];
		inet_ntop(AF_INET, addr, hostname, INET_ADDRSTRLEN);
		return std::string(hostname);
	}
}

std::string endpoint::getHost() const
{
	if (!host.empty())
		return host;
	
	assertValid();
	return host;
}

bool endpoint::isValid() const
{
	return isResolved() != host.empty();
}

void endpoint::getSockAddr(struct sockaddr& serv_addr) const
{
	assertValid();
	memset(&serv_addr, 0, sizeof(struct sockaddr));
	if (is_ipv6)
	{
		struct sockaddr_in6 *saddr = (struct sockaddr_in6 *)&serv_addr;
		saddr->sin6_family = AF_INET6;
		memcpy(saddr->sin6_addr.s6_addr, addr, IPV6_SZ);
		saddr->sin6_port = htons(port);
	}
	else
	{
		struct sockaddr_in *saddr = (struct sockaddr_in *)&serv_addr;
		saddr->sin_family = AF_INET;
		memcpy(&saddr->sin_addr.s_addr, addr, IPV4_SZ);
		saddr->sin_port = htons(port);
	}
}
	
void endpoint::resolve()
{
	if (isResolved()) {
		if (host.empty())
			host = getHostFromAddress();
		return;
	}
	// No use resolving empty host
	if (host.empty()) return;
	
	struct hostent *s;
	s = gethostbyname2(c_host(), AF_INET);

	if (s == NULL) {
		s = gethostbyname2(c_host(), AF_INET6);

		if (s == NULL)
			throw muscle_exception("host " + host + " not found");
		else
			is_ipv6 = true;
		
	} else {
		is_ipv6 = false;
	}
	
	if (is_ipv6) {
		memcpy(addr, s->h_addr, IPV6_SZ);
	} else {
		memcpy(addr, s->h_addr, IPV4_SZ);
		memset(addr+IPV4_SZ, 0, sizeof(addr)-IPV4_SZ);
	}
}

int16_t endpoint::getNetworkPort() const
{
	return htons(port);
}
	
size_t endpoint::getSize()
{
	return sizeof(char) +  sizeof(uint16_t) + 16*sizeof(char); // IPv4/v6 + Port + Address
}

char *endpoint::serialize(char *buffer) const
{
	assertValid();
	*buffer++ = is_ipv6 ? IPV6_FLAG : IPV4_FLAG;

	memcpy(buffer, addr, sizeof(addr));
	buffer += sizeof(addr);
	uint16_t *buffer_sptr = (uint16_t *)buffer;
	*buffer_sptr = htons(port);
	buffer += sizeof(port);
	return buffer;
}
   
bool endpoint::operator<(const endpoint& other) const
{
	assertValid(); other.assertValid();

	if (is_ipv6 != other.is_ipv6) return is_ipv6 < other.is_ipv6;
	if (port    != other.port   ) return port    < other.port;
	
	return memcmp(&addr, &other.addr, sizeof(addr)) == -1;
}
bool endpoint::operator==(const endpoint& other) const
{
	assertValid(); other.assertValid();
	return is_ipv6 == other.is_ipv6 && port == other.port && memcmp(&addr, &other.addr, sizeof(addr)) == 0;
}
bool endpoint::operator!=(const endpoint& other) const
{
	assertValid(); other.assertValid();
	return is_ipv6 != other.is_ipv6 || port != other.port || memcmp(&addr, &other.addr, sizeof(addr)) != 0;
}
