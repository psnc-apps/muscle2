//
//  helloreader.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 04-05-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "helloreader.h"

using namespace std;

HelloReader::HelloReader(const muscle::ClientSocket* _sock, Initiator *init, vector< MtoHello >& hellos_)
: sock(_sock), initiator(init), hellos(hellos_), refs(1)
{
    buf = new char[MtoHello::getSize()];
    sock->async_recv(0, buf, MtoHello::getSize(), this);
}

bool HelloReader::async_received(size_t code, int flag, void *data, size_t len, int final)
{
    if (final != 1) return true;
    
    MtoHello hello(buf);
    hellos.push_back(hello);
    if(hello.isLastMtoHello)
    {
        initiator->allHellosRead();
    }
    else
    {
        refs++;
        sock->async_recv(0, buf, MtoHello::getSize(), this);
    }
    
    return true;
}

void HelloReader::async_report_error(size_t code, int flag, const muscle::muscle_exception& ex)
{
    initiator->allHellosFailed(ex);
}

HelloReader::~HelloReader()
{
    delete [] buf;
}
