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
#include "muscle2/util/thread.h"
#include "mto/net/mpsocket.h"
#include "mto/net/MPWPathSocket.h"
#include "mto/net/ThreadPool.h"
#include "assertTemplates.h"

#include <arpa/inet.h>

class async_ss : public muscle::net::async_acceptlistener, muscle::net::async_recvlistener, muscle::net::async_sendlistener
{
public:
    muscle::net::async_service * const service;
    muscle::net::ServerSocket *ss;
    muscle::net::ClientSocket *cs;
    muscle::net::ClientSocket *as;
	int i, f3, f4;
	
    async_ss(muscle::net::async_service *s, muscle::net::ServerSocket *ssock) : service(s), ss(ssock), i(0), f3(0), f4(0) {}
    
    virtual void async_accept(size_t code, int flag, muscle::net::ClientSocket *sock)
    {
        if (code == 1) {
            cout << "accepted socket" << endl;
			ss->async_cancel();
			delete ss;
		} else {
			cout << "connected socket" << endl;
		}
		
		const char *os1 = "my string";
		const char *os2 = "my other";
		const size_t sz_large = 1024*1024*64, sz_small = 1024*4;
		if (++i == 1) {
            char *s1 = strdup(os1);
            char *s2 = strdup(os2);
			char *s3 = new char[sz_large];
			char *s4 = new char[sz_small];
			char *s5 = new char[2];
			s5[0] = 'a';
			s5[1] = 0;
            cs = sock;
            cs->async_send(3, s1, strlen(os1)+1, this, 0);
            cs->async_send(3, s2, strlen(os2)+1, this, 0);
            cs->async_recv(3, s3, sz_large, this);
            cs->async_recv(3, s4, sz_small, this);
            cs->async_send(3, s5, 2, this, 0);
        } else {
            char *s1 = new char[strlen(os1) + 1];
            char *s2 = new char[strlen(os2) + 1];
			char *s3 = new char[sz_large];
			char *s4 = new char[sz_small];
			char *s5 = new char[2];
			s3[0] = s4[0] = -1;
			s3[sz_large-1] = s4[sz_small-1] = 127;
            as = sock;
            as->async_recv(4, s1, strlen(os1)+1, this);
            as->async_recv(4, s2, strlen(os2)+1, this);
            as->async_send(4, s3, sz_large, this, 0);
            as->async_send(4, s4, sz_small, this, 0);
            as->async_recv(4, s5, 2, this);
        }
    }
    
    virtual void async_report_error(size_t,int,const muscle::muscle_exception& ex) {
		assertFalse("Had error: " + string(ex.what()));
	}
    virtual bool async_received(size_t code, int flag, void *data, void *last_data_ptr, size_t len, int final)
    {
		if (final == 0) return true;
		
        if (final == -1)
            assertFalse("Error during receiving data");
		else if (len > 0 && len < 20)
            cout << "received '" << string((char *)data) << "' (len " << len << ")" << endl;
		else if (len > 0) {
			assertEquals<char>(((char *)data)[0], -1, "Data transfer start");
			assertEquals<char>(((char *)data)[len - 1], 127, "Data transfer finish");
		}
		else
			assertFalse("received nothing...");
        
        delete [] (char *)data;
		
        return true;
    }
    virtual void async_sent(size_t code, int flag, void *data, size_t len, int final)
    {
		if (final == 0) return;
		
		if (final == -1) {
            assertFalse("Error during sending data");
		} else {
			cout << "sent '" << string((char *)data) << "' (len " << len << ")" << endl;
            free(data);
        }
    }
    virtual void async_done(size_t code, int flag)
    {
        if (flag == 3 && ++f3 == 5) delete cs;
        if (flag == 4 && ++f4 == 5) delete as;
    }
};

void testAsyncConnect(muscle::net::async_service &service, muscle::net::SocketFactory& factory, muscle::net::endpoint& ep, muscle::net::socket_opts& opts)
{
	ep.resolve();
	muscle::net::ServerSocket *ssock = factory.listen(ep, opts);
	usleep(10000);
	async_ss ass(&service, ssock);
	ssock->async_accept(1, &ass, &opts);
	factory.async_connect(2, ep, &opts, &ass);
	
	service.run();
}

struct mutexThreadData {
    muscle::util::mutex m;
    int data;
};

void *testMutexThread(void *d)
{
    mutexThreadData *data = (mutexThreadData *)d;
    
    {
        muscle::util::mutex_lock lock = data->m.acquire();
        data->data++;
    }
    usleep(150);
    {
        muscle::util::mutex_lock lock = data->m.acquire();
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
        muscle::util::mutex_lock lock = data.m.acquire();
        assertEquals(data.data, 1, "data is increased in thread");
    }
    {
        muscle::util::mutex_lock lock = data.m.acquire();
        data.data++;
        lock.notify();
        usleep(150);
        assertEquals(data.data, 2, "data is increased in local memory");
    }
    usleep(100);
    {
        muscle::util::mutex_lock lock = data.m.acquire();
        assertEquals(data.data, 3, "Notify woke up thread");
    }
    
    ::pthread_join(t, NULL);
    assertTrue("Joined thread");
}

class test_thread : public muscle::util::thread
{
    int * const data;
    const muscle::util::duration timeout;
public:
    test_thread(int *data, const muscle::util::duration& timeout) : data(data), timeout(timeout) {}
    virtual void *run()
    {
        timeout.sleep();
        ++(*data);
        return data;
    }
    virtual void deleteResult(void *) {}
};

void testThread()
{
    cout << endl << "thread" << endl << endl;
    
    int data = 0;
    muscle::util::duration timeout(0, 100);
    test_thread t(&data, timeout);
	t.start();
    assertEquals(t.isDone(), false, "Not done before execution");
    assertEquals(data, 0, "Increase only after timeout");
    int newResult = *(int *)t.getResult();
    assertEquals(t.isDone(), true, "Done after execution");
    assertEquals(data, newResult, "Result matches");
    assertEquals(data, 1, "Result is expected");
}

void testThreadPool()
{
    cout << endl << "threadpool" << endl << endl;
    
    int d1 = 0, d2 = 0;
    const muscle::util::duration timeout(0, 100), timeout2(0, 150);
	muscle::ThreadPool tp(1);
    test_thread t1(&d1, timeout), t2(&d2, timeout2);
	tp.execute(&t1);
	tp.execute(&t2);

    assertEquals(t1.isDone(), false, "1: Not done before execution");
    assertEquals(t2.isDone(), false, "2: Not done before execution");
    assertEquals(d1, 0, "1: Increase only after timeout");
    assertEquals(d2, 0, "2: Increase only after timeout");
    int newResult1 = *(int *)t1.getResult();
    assertEquals(t1.isDone(), true, "1: Done after execution");
    assertEquals(d1, newResult1, "1: Result matches");
    assertEquals(d1, 1, "1: Result is expected");
	if (!t2.isDone()) {
		cout << "very shortly sleeping" << endl;
		muscle::util::duration(0, 50).sleep();
	}
    assertEquals(t2.isDone(), true, "2: Done after execution");
    int newResult2 = *(int *)t2.getResult();
    assertEquals(d2, newResult2, "2: Result matches");
    assertEquals(d2, 1, "2: Result is expected");
}

void testAsyncMPConnectServerSocket()
{
    cout << endl << "async_connect/accept mpsocket" << endl << endl;
    muscle::net::endpoint ep("127.0.0.1", 40108);
    muscle::net::socket_opts opts(16);
    muscle::net::async_service service(size_t(1024*1024*1024), 20);
    muscle::MPSocketFactory factory(&service);
    testAsyncConnect(service, factory, ep, opts);
}

void testAsyncMPWPathConnectServerSocket()
{
    cout << endl << "async_connect/accept mpwpathsocket" << endl << endl;
    muscle::net::endpoint ep("127.0.0.1", 40108);
    muscle::net::socket_opts opts(16);
    muscle::net::async_service service(size_t(1024*1024*1024), 20);
    muscle::MPWPathSocketFactory factory(&service, 16, true);
    testAsyncConnect(service, factory, ep, opts);
}


int main(int argc, char * argv[])
{
    muscle::util::mtime t0 = muscle::util::mtime::now();
	muscle::logger::initialize("mto_tester", NULL, MUSCLE_LOG_SEVERE, MUSCLE_LOG_OFF, true);
    try {
        testMutex();
		testThread();
        testThreadPool();
//        testAsyncMPConnectServerSocket();
        testAsyncMPWPathConnectServerSocket();
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
