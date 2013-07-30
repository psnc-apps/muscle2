//
//  endpoint.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "endpoint.h"
#include "../../muscle2/exception.hpp"

#include <netdb.h>
#include <sstream>
#include <stdlib.h>
#include <unistd.h>
#include <cstring>
#include <arpa/inet.h>

using namespace muscle;

endpoint::endpoint(std::string _host, const uint16_t _port) : is_ipv6(false), addr(), host(_host), port(_port)
{}

endpoint::endpoint() : is_ipv6(false), addr(), host(""), port(0)
{}

endpoint::endpoint(uint32_t host, const uint16_t _port) : is_ipv6(false), port(htons(_port)), host("")
{
	uint32_t nhost = htonl(host);
	memcpy(addr, &nhost, IPV4_SZ);
	memset(addr+IPV4_SZ, 0, sizeof(addr) - IPV4_SZ);
}

endpoint::endpoint(const char *buffer_ptr) : host("")
{
	is_ipv6 = *buffer_ptr++ == MUSCLE_ENDPOINT_IPV6;
	memcpy(addr, buffer_ptr, sizeof(addr));
	
	buffer_ptr += sizeof(addr);
	
	port = ntohs(*(const uint16_t *)buffer_ptr);
}

std::string endpoint::getHostFromAddressImpl() const
{
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
	if (host.empty())
		return getHostFromAddress();

	return host;
}

std::string endpoint::getHost()
{
	if (host.empty())
		host = getHostFromAddress();
	
	return host;
}

bool endpoint::isValid()
{
	return resolve(false);
}

bool endpoint::isValid() const
{
	return resolve(false);
}

void endpoint::getSockAddrImpl(struct sockaddr& serv_addr) const
{
	resolve(true);
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
	
bool endpoint::resolve(bool make_error)
{
	if (isResolved()) return true;
	if (host.empty())
	{
		if (make_error) throw muscle_exception("Unspecified endpoint can not be resolved.");
		else return false;
	}
	
	struct hostent *s;
	s = gethostbyname2(c_host(), AF_INET);

	if (s == NULL)
	{
		s = gethostbyname2(c_host(), AF_INET6);

		if (s == NULL)
		{
			if (make_error) throw muscle_exception("host not found: " + host);
			else return false;
		}
		else
			is_ipv6 = true;
		
	}
	else
		is_ipv6 = false;
	
	if (is_ipv6)
	{
		memcpy(addr, s->h_addr, IPV6_SZ);
	}
	else
	{
		memcpy(addr, s->h_addr, IPV4_SZ);
		memset(addr+IPV4_SZ, 0, sizeof(addr)-IPV4_SZ);
	}
	return true;
}

bool endpoint::resolve(bool make_error) const
{
	if (isResolved() || host == "")
		return true;
	else if (make_error)
		throw muscle_exception("Endpoint '" + host + "' must be resolved to get properties.");
	else
		return false;
}

int16_t endpoint::getNetworkPort() const
{
	return htons(port);
}
	
size_t endpoint::getSize()
{
	return sizeof(char) +  sizeof(uint16_t) + 16*sizeof(char); // IPv4/v6 + Port + Address
}

char *endpoint::serializeImpl(char *buffer) const
{
	*buffer++ = is_ipv6 ? MUSCLE_ENDPOINT_IPV6 : MUSCLE_ENDPOINT_IPV4;

	memcpy(buffer, addr, sizeof(addr));
	buffer += sizeof(addr);
	uint16_t *buffer_sptr = (uint16_t *)buffer;
	*buffer_sptr = htons(port);
	buffer += sizeof(port);
	return buffer;
}
   
bool endpoint::operator==(const endpoint& other) const
{
	return isIPv6() == other.isIPv6() && memcmp(&addr, &other.addr, sizeof(addr)) == 0 && port == other.port;
}
bool endpoint::operator<(const endpoint& other) const
{
	if (isIPv6() != other.isIPv6()) return isIPv6() < other.isIPv6();
	int cmp = memcmp(&addr, &other.addr, sizeof(addr));
	if (cmp == -1) return true;
	if (cmp == 1) return false;
	return port < other.port;
}
bool endpoint::operator==(endpoint& other)
{
	return isIPv6() == other.isIPv6() && memcmp(&addr, &other.addr, sizeof(addr)) == 0 && port == other.port;
}
bool endpoint::operator!=(endpoint& other)
{
	return isIPv6() != other.isIPv6() || memcmp(&addr, &other.addr, sizeof(addr)) != 0 || port != other.port;
}
bool endpoint::operator!=(const endpoint& other) const
{
	return isIPv6() != other.isIPv6() || memcmp(&addr, &other.addr, sizeof(addr)) != 0 || port != other.port;
}
bool endpoint::operator<(endpoint& other)
{
	if (isIPv6() != other.isIPv6()) return isIPv6() < other.isIPv6();
	int cmp = memcmp(&addr, &other.addr, sizeof(addr));
	if (cmp == -1) return true;
	if (cmp == 1) return false;
	return port < other.port;
}
