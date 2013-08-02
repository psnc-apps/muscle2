//
//  async_description.h
//  CMuscle
//
//  Created by Joris Borgdorff on 25-04-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__async_description__
#define __CMuscle__async_description__

#include "../../muscle2/exception.hpp"
#include "../../muscle2/logger.hpp"

namespace muscle {
    class ClientSocket;
    
    class async_listener
    {
    public:
        virtual ~async_listener() {}
        virtual void async_report_error(size_t code, int user_flag, const muscle_exception& ex) = 0;
        virtual void async_done(size_t code, int user_flag) {}
    };
    
    class async_recvlistener : public async_listener
    {
    public:
        virtual bool async_received(size_t code, int user_flag, void *data, void *data_ptr, size_t size, int is_final) = 0;
    };
    class async_sendlistener : public async_listener
    {
    public:
        virtual void async_sent(size_t code, int user_flag, void *data, size_t size_t, int is_final) = 0;
    };
    class async_sendlistener_delete : public async_sendlistener
    {
    public:
		async_sendlistener_delete() {}
        virtual void async_sent(size_t code, int user_flag, void *data, size_t sz, int is_final)
        {
            // Delete both on error and on final send
            if (is_final)
                delete [] (unsigned char *)data;
        }
        virtual void async_report_error(size_t code, int user_flag, const muscle_exception& ex)
        {
            logger::severe("Uncaught error occurred: %s", ex.what());
        }
        virtual void async_done(size_t code, int user_flag) { delete this; }
    };
    class async_sendlistener_nodelete : public async_sendlistener
    {
    public:
        virtual void async_sent(size_t code, int user_flag, void *data, size_t sz, int is_final) {} // Never delete
        virtual void async_report_error(size_t code, int user_flag, const muscle_exception& ex)
        {
            logger::severe("Uncaught error occurred: %s", ex.what());
        }
        virtual void async_done(size_t code, int user_flag) { delete this; }
    };
    
    class async_function : public async_listener
    {
    public:
        virtual void async_execute(size_t code, int user_flag, void *user_data) = 0;
    };
    class async_acceptlistener : public async_listener
    {
    public:
        virtual void async_accept(size_t code, int user_flag, ClientSocket *newSocket) = 0;
    };
    
    struct async_description
    {
        size_t code;
        int user_flag;
        void *data;
        char *data_ptr;
        size_t size;
		int opts;
        async_listener *listener;
        
        bool operator <(const async_description& s1) const { return code < s1.code; }
            bool operator ==(const async_description& s1) const { return code == s1.code; }
            inline char *data_end() const { return ((char *)data) + size;}
            
            inline char *data_ptr_advance(size_t sz) const { return data_ptr + sz;}
            inline size_t data_remain() const { return data_end() - data_ptr; }
            
            async_description(size_t code, int user_flag, void *data, size_t size, async_listener *listener) : code(code), user_flag(user_flag), data(data), data_ptr((char *)data), size(size), listener(listener), opts(-1)
            {}
            async_description() : code(0), user_flag(0), data((void *)0), size(0), listener((async_listener *)0), opts(-1)
            {}
    };
}

#endif /* defined(__CMuscle__async_description__) */
