//
//  helloreader.h
//  CMuscle
//
//  Created by Joris Borgdorff on 04-05-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__helloreader__
#define __CMuscle__helloreader__

#include "../net/messages.hpp"
#include "../net/socket.h"

#include <exception>
#include <vector>

class Initiator
{
public:
    virtual ~Initiator() {}
    virtual void allHellosRead() = 0;
    virtual void allHellosFailed(const muscle::muscle_exception& ex) = 0;
    virtual void done() { delete this; }
};

/**
 * Once a connection between MTO's is established, this class is ued to read all Hellos
 * before starting normal communication.
 */
struct HelloReader : public muscle::async_recvlistener
{
private:
    muscle::ClientSocket * sock;
    char * buf;
    int refs;
    std::vector<MtoHello>& hellos;
    Initiator *initiator;
    
public:
    HelloReader(muscle::ClientSocket * _sock, Initiator *init, std::vector<MtoHello>& hellos_);
    
    virtual ~HelloReader();
    /** Triggered on accept; reads the header and constructs a connection */
    virtual bool async_received(size_t code, int flag, void *data, void *last_data_ptr, size_t len, int final);
    virtual void async_report_error(size_t code, int flag, const muscle::muscle_exception& ex);
    
    virtual void async_done(size_t code, int flag) { if (--refs == 0) delete this; }
};


#endif /* defined(__CMuscle__helloreader__) */
