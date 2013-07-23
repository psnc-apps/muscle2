//
//  time.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 4/24/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "time.h"
#include "../../muscle2/exception.hpp"

#include <stdint.h>
#include <errno.h>
#include <climits>
#include <unistd.h>
#include <cstring>

#ifndef UINT32_MAX
#define UINT32_MAX 4294967295U
#endif

namespace muscle {
    time time::far_future()
    {
         return time(LONG_MAX, 0);
    }
    
    time time::now()
    {
        struct timeval now;
        if (gettimeofday(&now, NULL) == -1)
            throw muscle_exception("Could not get time for timer", errno);
        
        return time(now);
    }
    
    void time::sleep() const
    {
        duration_until().sleep();
    }

    uint32_t duration::useconds() const
    {
        if (t.tv_sec > UINT32_MAX)
            return UINT32_MAX;
        
        long usec = t.tv_sec * USEC_IN_SEC;
        if (usec > UINT32_MAX)
            return UINT32_MAX;
        
        usec += t.tv_usec;
        if (usec > UINT32_MAX)
            return UINT32_MAX;
        return uint32_t(usec);
    }
    
    void duration::sleep() const
    {
        if (t.tv_sec > 0 || t.tv_usec > 0)
            usleep(useconds());
    }
    
    duration time::duration_until() const
    {
        return *this - now();
    }
    duration time::duration_since() const
    {
        return now() - *this;
    }
    
    time time::operator+(const duration &other) const
    {
        return time(plus(t, other.timeval()));
    }

    time time::operator-(const duration &other) const
    {
        return time(minus(t, other.timeval()));
    }
    
    duration time::operator-(const time& other) const
    {
        return duration(minus(t, other.timeval()));
    }
    
    std::string abstract_time::str() const
    {
        long days = t.tv_sec / SEC_IN_DAY;
        int hours = (t.tv_sec % SEC_IN_DAY) / SEC_IN_HOUR;
        int min = (t.tv_sec % SEC_IN_HOUR) / SEC_IN_MIN;
        int sec = t.tv_sec % SEC_IN_MIN;
        int msec = t.tv_usec / USEC_IN_MSEC;
        
        char c_str[TIME_STR_MAX_LEN];
        if (days > 0)
            sprintf(c_str, "%ldd %d:%02d:%02d.%03d", days, hours, min, sec, msec);
        else
            sprintf(c_str, "%d:%02d:%02d.%03d", hours, min, sec, msec);
        return std::string(c_str);
    }
}