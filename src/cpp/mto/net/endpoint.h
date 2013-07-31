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

namespace muscle {
    
    class endpoint
    {
    public:
        // In host order
        uint16_t port;

        endpoint();
        endpoint(const char *buffer);
        endpoint(std::string host, uint16_t port);
        endpoint(uint32_t host, uint16_t port);
        
        inline void resolve() { resolve(true); }
        inline bool isResolved() const
        {
            for (int i = 0; i < sizeof(addr); i++)
                if (addr[i]) return true;
            return false;
        }
        bool isValid();
        bool isValid() const;

        bool isIPv6() { resolve(true); return is_ipv6; }
        bool isIPv6() const { resolve(true); return is_ipv6; }
        int16_t getNetworkPort() const;
        void getSockAddr(struct sockaddr& serv_addr) {resolve(true); getSockAddrImpl(serv_addr);}
        void getSockAddr(struct sockaddr& serv_addr) const {resolve(true); getSockAddrImpl(serv_addr);}
        
        char *serialize(char *buffer) { resolve(true); return serializeImpl(buffer); }
        char *serialize(char *buffer) const { resolve(true); return serializeImpl(buffer); }
        static size_t getSize();
        
        inline const char * c_host() const { return host.c_str(); }
        inline std::string str() const { std::stringstream ss; ss << *this; return ss.str(); }
        
        std::string getHost() const;
        std::string getHost();        
        
        bool operator==(const endpoint& other) const;
        bool operator<(const endpoint& other) const;
        bool operator==(endpoint& other);
        bool operator<(endpoint& other);
        bool operator!=(endpoint& other);
        bool operator!=(const endpoint& other) const;

        std::string getHostFromAddress() { resolve(true); return getHostFromAddressImpl(); }
        std::string getHostFromAddress() const { resolve(true); return getHostFromAddressImpl(); }

	static const char IPV4_FLAG;
	static const char IPV6_FLAG;
    private:
        // Presentation
        std::string host;
        
        // In network byte order
        char addr[16];
        
        // Whether it represents an IPv6 address
        bool is_ipv6;
        
        std::string getHostFromAddressImpl() const;
        void getSockAddrImpl(struct sockaddr &serv_addr) const;
        char *serializeImpl(char *buffer) const;
        
        bool resolve(bool make_error);
        bool resolve(bool make_error) const;
		
		const static int IPV4_SZ = 4;
		const static int IPV6_SZ = 16;
			
		friend std::ostream& operator<<(std::ostream &os, const endpoint& r)
		{ return os << r.getHost() << ":" << r.port; }
    };
}

#endif /* defined(__CMuscle__endpoint__) */
