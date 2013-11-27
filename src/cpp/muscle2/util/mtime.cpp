//
//  time.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 4/24/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "mtime.h"

#include <unistd.h>
#include <cstring>

namespace muscle {
	namespace util {
		mtime mtime::now()
		{
			struct timeval now;
			if (gettimeofday(&now, NULL) == -1)
				throw muscle_exception("Could not get time for timer", errno, true);
			
			return mtime(now);
		}
		
		void mtime::sleep() const
		{
			duration_until().sleep();
		}
		
		void duration::sleep() const
		{
			if (t.tv_sec > 0 || t.tv_usec > 0)
				usleep(useconds());
		}
		
		duration mtime::duration_until() const
		{
			return *this - now();
		}
		duration mtime::duration_since() const
		{
			return now() - *this;
		}
		
		mtime mtime::operator+(const duration &other) const
		{
			return mtime(plus(t, other.timeval()));
		}
		
		mtime mtime::operator-(const duration &other) const
		{
			return mtime(minus(t, other.timeval()));
		}
		
		duration mtime::operator-(const mtime& other) const
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
}
