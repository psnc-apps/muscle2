//
//  test_util.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 05-08-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "assertTemplates.h"

#include "muscle2/util/csocket.h"
#include "muscle2/util/async_service.h"
#include "muscle2/util/option_parser.hpp"

#include <arpa/inet.h> // htons

class async_ss : public muscle::net::async_acceptlistener, muscle::net::async_recvlistener, muscle::net::async_sendlistener
{
public:
    muscle::net::async_service * const service;
    int i;
    muscle::net::ServerSocket *ss;
    muscle::net::ClientSocket *cs;
    muscle::net::ClientSocket *as;
    async_ss(muscle::net::async_service *s, muscle::net::ServerSocket *ssock) : service(s), i(0), ss(ssock) {}
    
    virtual void async_accept(size_t code, int flag, muscle::net::ClientSocket *sock)
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
            ss->async_cancel();
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

class async_func_call : public muscle::net::async_function
{
public:
    muscle::net::async_service * const service;
    const bool first;
    async_func_call(muscle::net::async_service *s, bool first) : service(s), first(first) {}
    virtual void async_report_error(size_t,int,const muscle::muscle_exception&) {}
    virtual void async_execute(size_t code, int flag, void *data)
    {
        if (first)
        {
            assertEquals<size_t>(code, 1, "first call to async_service");
            assertEquals<const char *>("test string", (const char *)data, "user data passes unmodified");
            assertEquals<int>(flag, 1, "flag passes unmodified");
            service->erase_timer(code);
        }
        else
            assertEquals<size_t>(code, 2, "second call to async_service");
    }
};

void testTime()
{
    cout << endl << "time" << endl << endl;
    
    muscle::util::mtime t = muscle::util::mtime::now();
    assert(t.is_past(), "current time is past");
    assert(!muscle::util::mtime::far_future().is_past(), "far future is not past");
    muscle::util::duration d(10,0);
    assert(!d.time_after().is_past(), "duration time_after has time in the future");
    assert(t < d.time_after(), "evaluation up to now takes less than 10 seconds");
    muscle::util::duration dnine(9,1);
    assert(dnine.time_after().duration_until() < d && dnine < d.time_after().duration_until(), "Evaluation of time and duration has the right ordering and takes less than a second");
    assertEquals<long>(dnine.seconds(), 9, "seconds match given");
    assertEquals<unsigned>(dnine.useconds(), 9000001, "microseconds match given");
}

void testAsyncConnect(muscle::net::async_service &service, muscle::net::SocketFactory& factory, muscle::net::endpoint& ep, muscle::net::socket_opts& opts)
{
    try
    {
        muscle::net::ServerSocket *ssock = factory.listen(ep, opts);
        usleep(1000000);
        async_ss ass(&service, ssock);
        ssock->async_accept(1, &ass, &opts);
        factory.async_connect(1, ep, &opts, &ass);
        
        service.run();
    } catch (const muscle::muscle_exception& ex) {
        cout << "Can not bind to port, use a different one: " << string(ex.what()) << endl;
    }
}

void testAsyncConnectServerSocket()
{
    cout << endl << "async_connect/accept csocket" << endl << endl;
    muscle::net::endpoint ep("localhost", 40104);
	ep.resolve();
    muscle::net::socket_opts opts(10);
    muscle::net::async_service service;
    muscle::net::CSocketFactory factory(&service);
    testAsyncConnect(service, factory, ep, opts);
}

void testAsyncTimer()
{
    cout << endl << "async_cservice timer" << endl << endl;
	
    muscle::net::async_service service;
    muscle::util::mtime t = muscle::util::duration(0l, 100).time_after();
    const char *str = "test string";
    
    async_func_call func(&service, true);
    service.timer(1, t, &func, (void *)str);
	
    async_func_call func2(&service, false);
    muscle::util::mtime t2 = muscle::util::duration(1l, 0).time_after();
    service.timer(1, t2, &func2, (void *)str);
    
    service.run();
    
    try {
        service.update_timer(2, t2, (void *)str);
        service.run();
        assertTrue("update timer");
    } catch (...) {
        assertFalse("update timer");
    }
	
    try {
        service.update_timer(1, t2, (void *)str);
        service.run();
        assertFalse("cannot update erased timer");
    } catch (...) {
        assertTrue("cannot update erased timer");
    }
}

void testSocket()
{
    cout << endl << "csocket" << endl << endl;
    muscle::net::endpoint ep("napoli.science.uva.nl", 50022);
	ep.resolve();
    
    try {
		muscle::net::socket_opts opts;
        muscle::net::CClientSocket sock(ep, NULL, opts);
        assert(true, "Connection to existing host");
        char s[] = "some string";
		assertEquals<int>(sock.impl->select(0, sock.getWriteSock(), muscle::net::msocket_impl::RDMASK, 1, 0), muscle::net::msocket_impl::WRMASK, "Select sending");
        assert(sock.send(s, sizeof(s)) > 0, "Sending");
        ssize_t len;
		assertEquals<int>(sock.impl->select(sock.getReadSock(), 0, muscle::net::msocket_impl::WRMASK, 1, 0), muscle::net::msocket_impl::RDMASK, "Select receiving");
        assert((len = sock.recv(s, sizeof(s)-1)) > 0, "Receiving");
        s[len] = '\0';
        cout << "\t\t(received: " << string(s) << ")" << endl;
    } catch (const exception& ex) {
        assertFalse("Connection to existing host (" + string(ex.what()) + ")");
    }
    
    muscle::net::endpoint nep("XXXnapoli.science.uva.nl", 50022);
    try {
		muscle::net::socket_opts opts;
        muscle::net::CClientSocket sock(nep, NULL, opts);
        assertFalse("Do not connect to non-existing host");
    } catch (const exception& ex) {
        assertTrue("Do not connect to non-existing host (" + string(ex.what()) + ")");
    }
}

void testParsing()
{
    cout << endl << "parsing" << endl << endl;
    vector<string> v;
    v.push_back("some"); v.push_back("text");
    assertEquals(muscle::util::split("some text", " "), v, "Split string by spaces");
    assertEquals(muscle::util::split(" some text", " "), v, "Left trim string by spaces");
    assertEquals(muscle::util::split("some text ", " "), v, "Right trim string by spaces");
	assertEquals(muscle::util::split("some=text", "="), v, "Split string by equal signs");
    assertEquals(muscle::util::split("some = text ", "= "), v, "Split string by spaces and equal signs");
    assertEquals(muscle::util::split("some = 	text ", "= \t"), v, "Split string by spaces, equal and tabs");
    assertEquals<string>(muscle::util::to_upper_ascii("ascii"), "ASCII", "Upper case");
    assertEquals<string>(muscle::util::to_upper_ascii("AsCIi"), "ASCII", "Mixed to upper case");
    assertEquals<string>(muscle::util::to_upper_ascii("AsC1=*20Ii"), "ASC1=*20II", "Mixed symbols to upper case");
}

void testOptions()
{
    cout << endl << "options" << endl << endl;
    muscle::util::option_parser opt;
    opt.add("left", "left side", false);
    opt.add("optA", "opt A");
	
    const char *argv[] = {"mto_tester", "--left", "--optA", "some"};
    int argc = 4;
    opt.load(argc, (char **)argv);
    assert(opt.has("left"), "flag option");
    assertEquals<string>(opt.get<string>("left", "default"), "", "flag option value");
    assertEquals<string>(opt.get<string>("optA", "default"), "some", "flag argument value");
    assertEquals<string>(opt.get<string>("optB", "default"), "default", "flag argument default value");
    assertEquals<string>(opt.forceGet<string>("optA"), "some", "force get argument value");
    try {
        opt.forceGet<string>("NonExisting");
        assertFalse("Throw exception non-existing mandatory key");
    } catch (...) {
        assertTrue("Throw exception non-existing mandatory key");
    }
}

void testEndpoint()
{
    cout << endl << "endpoint" << endl << endl;
    muscle::net::endpoint ep("napoli.science.uva.nl", 50022);
	
    
    assertEquals(ep.c_host(), "napoli.science.uva.nl", "c-host name is preserved");
    assertEquals(ep.getHost(), string("napoli.science.uva.nl"), "host name is preserved");
    assert(!ep.isResolved(), "do not resolve existing host without calling resolve");
    ep.resolve();
    assert(ep.isResolved(), "resolve existing host");
    assert(ep.isValid(), "validate existing host");
    assert(!ep.isIPv6(), "check IPv4 host");
    assertEquals<string>(ep.getHostFromAddress(), "146.50.56.52", "formatted IPv4 address matches");
    assertEquals<uint16_t>(ep.port, 50022, "host order port number matches");
    unsigned char *buf = new unsigned char[ep.getSize()];
    ep.serialize((char *)buf);
    assertEquals<int>(buf[0], muscle::net::endpoint::IPV4_FLAG, "serialized protocol is IPv4");
    assertEquals<int>(buf[1], 146, "first IPv4 segment matches 146");
    assertEquals<int>(buf[2], 50, "second IPv4 segment matches 50");
    assertEquals<int>(buf[3], 56, "third IPv4 segment matches 56");
    assertEquals<int>(buf[4], 52, "last IPv4 segment matches 52");
    assertEquals<uint16_t>(*(uint16_t *)&buf[17],htons(50022), "port matches 50022");
    
    muscle::net::endpoint bep((char *)buf);
    assertEquals(bep, ep, "Serialized data matches original data");
    
    const muscle::net::endpoint cep(ep);
    
    assert(cep.isResolved(), "const resolve existing host without calling resolve");
    assert(cep.isValid(), "const validate existing host");
    assert(!cep.isIPv6(), "const check IPv4 host");
    
    muscle::net::endpoint invEp("XXXnapoli.science.uva.nl", 5000);
    try {
        invEp.resolve();
        assertFalse("throw exception on resolve invalid hosts: " + invEp.getHost() + "; " + invEp.getHostFromAddress());
    }
    catch (const muscle::muscle_exception& ex)
    {
        assertTrue("throw exception on resolve invalid hosts");
    }
    assert(!invEp.isResolved(), "do not resolve invalid hosts");
}

int main(int argc, char * argv[])
{
    muscle::util::mtime t0 = muscle::util::mtime::now();
	
    try {
        testEndpoint();
        testSocket();
        testTime();
        testParsing();
        testOptions();
        testAsyncTimer();
        testAsyncConnectServerSocket();
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
