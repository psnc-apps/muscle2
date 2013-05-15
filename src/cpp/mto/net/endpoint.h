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
#include <sys/socket.h>

#define MUSCLE_ENDPOINT_IPV4 1
#define MUSCLE_ENDPOINT_IPV6 2

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
            return addr.l[0] || addr.l[1];
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
        std::string str() const;
        std::string str();
        
        std::string getHost() const;
        std::string getHost();        
        
        bool operator==(const endpoint& other) const;
        bool operator<(const endpoint& other) const;
        bool operator==(endpoint& other);
        bool operator<(endpoint& other);
        bool operator!=(endpoint& other);
        bool operator!=(const endpoint& other) const;

        friend std::ostream & operator<< (std::ostream &os, muscle::endpoint const &ep)
        { os << ep.str(); return os; }
        
        std::string getHostFromAddress() { resolve(true); return getHostFromAddressImpl(); }
        std::string getHostFromAddress() const { resolve(true); return getHostFromAddressImpl(); }
    private:
        // Presentation
        std::string host;
        
        // In network order
        union addr_t {
            char c[16];
            unsigned long l[2];
        } addr;
        
        std::string getHostFromAddressImpl() const;
        void getSockAddrImpl(struct sockaddr &serv_addr) const;
        char *serializeImpl(char *buffer) const;
        
        bool resolve(bool make_error);
        bool resolve(bool make_error) const;
        bool is_ipv6;
    };
}
#endif /* defined(__CMuscle__endpoint__) */
