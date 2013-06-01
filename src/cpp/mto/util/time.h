//
//  time.h
//  CMuscle
//
//  Created by Joris Borgdorff on 4/24/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__time__
#define __CMuscle__time__

#include <sys/time.h>
#include <stdint.h>
#include <string>

#define TIME_STR_MAX_LEN 35
#define SEC_IN_DAY 86400l
#define SEC_IN_HOUR 3600l
#define SEC_IN_MIN 60l
#define USEC_IN_SEC 1000000l
#define USEC_IN_MSEC 1000

namespace muscle {

class time;
class duration;
    
class abstract_time {
public:
    const struct timeval& timeval() const { return t; }
    long seconds() const { return t.tv_sec; }
    
    virtual void sleep() const = 0;
    virtual std::string str() const;
protected:
    // With tv_usec > 0
    struct timeval t;

    abstract_time()
    {
        t.tv_sec = 0;
        t.tv_usec = 0;
    }
    abstract_time(struct timeval& tval) : t(tval)
    { standardize(); }
    abstract_time(long sec, int usec)
    {
        t.tv_sec = sec;
        t.tv_usec = usec;
        standardize();
    }
    
    inline static struct timeval plus(const struct timeval &t1, const struct timeval &t2)
    {
        struct timeval newt = {t1.tv_sec + t2.tv_sec, t1.tv_usec + t2.tv_usec};
        return newt;
    }
    inline static struct timeval minus(const struct timeval &t1, const struct timeval &t2)
    {
        struct timeval newt = {t1.tv_sec - t2.tv_sec, t1.tv_usec - t2.tv_usec};
        return newt;
    }
private:
    inline void standardize()
    {
        if (t.tv_usec < 0)
        {
            int sec = 1 - t.tv_usec / USEC_IN_SEC;
            t.tv_sec -= sec;
            t.tv_usec += sec * USEC_IN_SEC;
        }
        else if (t.tv_usec >= USEC_IN_SEC)
        {
            t.tv_sec += t.tv_usec / USEC_IN_SEC;
            t.tv_usec %= USEC_IN_SEC;
        }
    }
};

class time : public abstract_time
{
public:
    time() : abstract_time() {}
    time(long sec, int usec) : abstract_time(sec, usec) {}
    time(struct timeval tval) : abstract_time(tval) {}

    virtual void sleep() const;
    duration duration_until() const;
    duration duration_since() const;
    static time far_future();
    static time now();
    bool is_past() const { return !(*this > now()); }
    
    time operator+(const duration& other) const;
    time operator-(const duration& other) const;
    duration operator-(const time& other) const;
    
    inline bool operator<(const time& other) const
    {
        return t.tv_sec < other.t.tv_sec ||
              (t.tv_sec == other.t.tv_sec && t.tv_usec < other.t.tv_usec);
    }
    inline bool operator>(const time& other) const
    {
        return t.tv_sec > other.t.tv_sec ||
        (t.tv_sec == other.t.tv_sec && t.tv_usec > other.t.tv_usec);
    }
    inline bool operator==(const time& other) const
    { return t.tv_sec == other.t.tv_usec && t.tv_usec == other.t.tv_usec; }
    inline bool operator!=(const time& other) const
    { return t.tv_sec != other.t.tv_usec || t.tv_usec != other.t.tv_usec; }
};
    
class duration : public abstract_time
{
public:
    duration() : abstract_time() {}
    duration(long sec, int usec) : abstract_time(sec, usec)
    {
        if (t.tv_sec < 0)
        {
            t.tv_sec = 0;
            t.tv_usec = 0;
        }
    }
    duration(struct timeval tval) : abstract_time(tval)
    {
        if (t.tv_sec < 0)
        {
            t.tv_sec = 0;
            t.tv_usec = 0;
        }
    }
    
    virtual void sleep() const;
    uint32_t useconds() const;

    inline time time_after() const
    { return *this + time::now(); }
    
    inline time operator+(const time& other) const
    { return time(plus(t, other.timeval())); }

    inline duration operator+(const duration& other) const
    { return duration(plus(t, other.timeval())); }

    inline duration operator-(const duration& other) const
    { return duration(minus(t, other.timeval())); }

    inline bool operator<(const duration& other) const
    {
        return t.tv_sec < other.t.tv_sec ||
        (t.tv_sec == other.t.tv_sec && t.tv_usec < other.t.tv_usec);
    }
    inline bool operator>(const duration& other) const
    {
        return t.tv_sec > other.t.tv_sec ||
        (t.tv_sec == other.t.tv_sec && t.tv_usec > other.t.tv_usec);
    }
    inline bool operator==(const duration& other) const
    { return t.tv_sec == other.t.tv_usec && t.tv_usec == other.t.tv_usec; }
    inline bool operator!=(const duration& other) const
    { return t.tv_sec != other.t.tv_usec || t.tv_usec != other.t.tv_usec; }
};

}

#endif /* defined(__CMuscle__time__) */

