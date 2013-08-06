//
//  main.cpp
//  mto_tester
//
//  Created by Joris Borgdorff on 5/2/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "muscle2/util/csocket.h"
#include "muscle2/util/async_service.h"
#include "muscle2/util/option_parser.hpp"
#include "mto/util/thread.h"
#include "mto/net/mpsocket.h"
#include "assertTemplates.h"

#include <arpa/inet.h>

class async_ss : public muscle::async_acceptlistener, muscle::async_recvlistener, muscle::async_sendlistener
{
public:
    muscle::async_service * const service;
    int i;
    muscle::ServerSocket *ss;
    muscle::ClientSocket *cs;
    muscle::ClientSocket *as;
    async_ss(muscle::async_service *s, muscle::ServerSocket *ssock) : service(s), i(0), ss(ssock) {}
    
    virtual void async_accept(size_t code, int flag, muscle::ClientSocket *sock)
    {
        i++;
        if (i == 1) {
            cout << "connected socket" << endl;
            char *s = strdup("my string");
            cs = sock;
            cs->async_send(3, s, strlen(s)+1, this, 0);
        } else {
            char *s = new char[20];
            cout << "accepted socket" << endl;
            as = sock;
            as->async_recv(4, s, 20, this);
            service->erase(ss);
        }
    }
    
    virtual void async_report_error(size_t,int,const muscle::muscle_exception&) {}
    virtual bool async_received(size_t code, int flag, void *data, void *last_data_ptr, size_t len, int final)
    {
        if (final == -1)
            assertFalse("Error during receiving data");
        else if (len > 0)
            cout << "received " << string((char *)data) << endl;
        
        delete [] (char *)data;
        
        return false;
    }
    virtual void async_sent(size_t code, int flag, void *data, size_t len, int final)
    {
        if (final == 1)
        {
            cout << "sent " << string((char *)data) << endl;
            free(data);
        }
        if (final == -1)
            assertFalse("Error during sending data");
    }
    virtual void async_done(size_t code, int flag)
    {
        if (flag == 3) delete cs;
        if (flag == 4) delete as;
    }
};

void testAsyncConnect(muscle::async_service &service, muscle::SocketFactory& factory, muscle::endpoint& ep, muscle::socket_opts& opts)
{
    try
    {
        muscle::ServerSocket *ssock = factory.listen(ep, opts);
        usleep(1000000);
        async_ss ass(&service, ssock);
        ssock->async_accept(1, &ass, &opts);
        factory.async_connect(1, ep, &opts, &ass);
        
        service.run();
    } catch (const muscle::muscle_exception& ex) {
        cout << "Can not bind to port, use a different one: " << string(ex.what()) << endl;
    }
}

struct mutexThreadData {
    muscle::mutex m;
    int data;
};

void *testMutexThread(void *d)
{
    mutexThreadData *data = (mutexThreadData *)d;
    
    {
        muscle::mutex_lock lock = data->m.acquire();
        data->data++;
    }
    usleep(150);
    {
        muscle::mutex_lock lock = data->m.acquire();
        while (data->data != 2) {
            lock.wait();
        }
        data->data++;
    }
    
    return NULL;
}

void testMutex()
{
    cout << endl << "mutex" << endl << endl;

    mutexThreadData data;
    data.data = 0;
    
    pthread_t t;
    ::pthread_create(&t, NULL, &testMutexThread, &data);
    usleep(50);
    {
        muscle::mutex_lock lock = data.m.acquire();
        assertEquals(data.data, 1, "data is increased in thread");
    }
    {
        muscle::mutex_lock lock = data.m.acquire();
        data.data++;
        lock.notify();
        usleep(150);
        assertEquals(data.data, 2, "data is increased in local memory");
    }
    usleep(100);
    {
        muscle::mutex_lock lock = data.m.acquire();
        assertEquals(data.data, 3, "Notify woke up thread");
    }
    
    ::pthread_join(t, NULL);
    assertTrue("Joined thread");
}

class test_thread : public muscle::thread
{
    int *data;
    muscle::duration timeout;
public:
    test_thread(int *data, muscle::duration timeout) : data(data), timeout(timeout) { start(); }
    virtual void *run()
    {
        timeout.sleep();
        ++(*data);
        return data;
    }
};

void testThread()
{
    cout << endl << "thread" << endl << endl;
    
    int data = 0;
    muscle::duration timeout(0, 100);
    test_thread t(&data, timeout);
    assertEquals(t.isDone(), false, "Not done before execution");
    assertEquals(data, 0, "Increase only after timeout");
    int newResult = *(int *)t.getResult();
    assertEquals(t.isDone(), true, "Done after execution");
    assertEquals(data, newResult, "Result matches");
    assertEquals(data, 1, "Result is expected");
}

void testAsyncMPConnectServerSocket()
{
    cout << endl << "async_connect/accept mpsocket" << endl << endl;
    muscle::endpoint ep("127.0.0.1", 40108);
    muscle::socket_opts opts(16);
    muscle::async_service service;
    muscle::MPSocketFactory factory(&service);
    testAsyncConnect(service, factory, ep, opts);
}

int main(int argc, char * argv[])
{
    muscle::time t0 = muscle::time::now();

    try {
        testMutex();
        testThread();
//        testAsyncMPConnectServerSocket();
        cout << endl;
        assertTrue("run tests without uncaught exceptions");
    } catch (const muscle::muscle_exception& ex) {
        assertFalse("run tests without uncaught exceptions (" + string(ex.what()) + ")");
    } catch (...) {
        assertFalse("run tests without uncaught exceptions");
    }
    
    cout << endl << failed << " of " << total << " failed (using " << t0.duration_since().str() << "s)" << endl;
    
    return failed;
}
