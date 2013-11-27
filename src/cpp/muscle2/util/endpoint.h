//
//  endpoint.h
//  CMuscle
//
//  Created by Joris Borgdorff on 4/15/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__endpoint__
#define __CMuscle__endpoint__

#include <string>
#include <ostream>
#include <sstream>
#include <sys/socket.h>
#include <stdint.h>
#include "exception.hpp"

namespace muscle {
	namespace net {
		class endpoint
		{
		public:
			// In host order
			uint16_t port;
			
			endpoint();
			endpoint(const char *buffer);
			endpoint(std::string host, uint16_t port);
			endpoint(uint16_t port);
			
			void resolve();
			inline bool isResolved() const
			{
				for (int i = 0; i < sizeof(addr); i++)
					if (addr[i]) return true;
				return false;
			}
			inline bool isWildcard() const
			{
				// If any other address part is set, it's not a wildcard
				for (int i = 1; i < sizeof(addr); i++)
					if (addr[i]) return false;
				
				return addr[0] == 1 || (addr[0] == 0 && host == "*");
			}
			bool isValid() const;
			bool isIPv6() const { assertValid(); return is_ipv6; }
			
			int16_t getNetworkPort() const;
			void getSockAddr(struct sockaddr& serv_addr) const;
			
			char *serialize(char *buffer) const;
			static size_t getSize();
			
			inline const char * c_host() const { return host.c_str(); }
			inline std::string str() const { std::stringstream ss; ss << *this; return ss.str(); }
			
			std::string getHost() const;
			
			bool operator==(const endpoint& other) const;
			bool operator<(const endpoint& other) const;
			bool operator!=(const endpoint& other) const;
			
			std::string getHostFromAddress() const;
			
			static const char IPV4_FLAG;
			static const char IPV6_FLAG;
		private:
			// Presentation
			std::string host;
			
			// In network byte order
			char addr[16];
			
			// Whether it represents an IPv6 address
			bool is_ipv6;
			
			inline void assertValid() const
			{
				if (!isResolved() && !host.empty())
					throw muscle::muscle_exception("Endpoint '" + host + "' must be resolved to get properties.");
			}
			
			const static int IPV4_SZ = 4;
			const static int IPV6_SZ = 16;
			
			friend std::ostream& operator<<(std::ostream &os, const endpoint& r)
			{ return os << r.getHost() << ":" << r.port; }
		};
	}
}

#endif /* defined(__CMuscle__endpoint__) */
