//
//  mtime.h
//  CMuscle
//
//  Created by Joris Borgdorff on 4/24/13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__mtime__
#define __CMuscle__mtime__

#include "exception.hpp"

#include <sys/time.h>
#include <stdint.h>
#include <climits>
#include <string>
#include <errno.h>

namespace muscle {
	namespace util {
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
			
			inline abstract_time()
			{
				t.tv_sec = 0;
				t.tv_usec = 0;
			}
			inline abstract_time(const struct timeval& tval) : t(tval)
			{}
			inline abstract_time(const long sec, const int usec)
			{
				t.tv_sec = sec;
				t.tv_usec = usec;
				standardize();
			}
			
			inline static struct timeval plus(const struct timeval &t1, const struct timeval &t2)
			{
				struct timeval newt = {t1.tv_sec + t2.tv_sec, t1.tv_usec + t2.tv_usec};
				if (newt.tv_usec >= USEC_IN_SEC){
					newt.tv_sec  += 1;
					newt.tv_usec -= USEC_IN_SEC;
				}
				return newt;
			}
			inline static struct timeval minus(const struct timeval &t1, const struct timeval &t2)
			{
				struct timeval newt = {t1.tv_sec - t2.tv_sec, t1.tv_usec - t2.tv_usec};
				if (newt.tv_usec < 0){
					newt.tv_sec  -= 1;
					newt.tv_usec += USEC_IN_SEC;
				}
				return newt;
			}
			static const size_t TIME_STR_MAX_LEN = 35;
			static const time_t SEC_IN_DAY = 86400;
			static const time_t SEC_IN_HOUR = 3600;
			static const time_t SEC_IN_MIN = 60;
			static const suseconds_t USEC_IN_SEC = 1000000;
			static const suseconds_t USEC_IN_MSEC = 1000;
		private:
			inline void standardize()
			{
				if (t.tv_usec < 0)
				{
					const int sec = 1 - t.tv_usec / USEC_IN_SEC;
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
		
		class mtime : public abstract_time
		{
		public:
			mtime() : abstract_time() {}
			mtime(const long sec, const int usec) : abstract_time(sec, usec) {}
			mtime(const struct timeval& tval) : abstract_time(tval) {}
			
			virtual void sleep() const;
			duration duration_until() const;
			duration duration_since() const;
			inline static mtime far_future()
			{ struct timeval t; t.tv_sec = LONG_MAX; t.tv_usec = 0; return mtime(t); }
			
			static mtime now();
			
			bool is_past() const { return !(*this > now()); }
			
			mtime operator+(const duration& other) const;
			mtime operator-(const duration& other) const;
			duration operator-(const mtime& other) const;
			
			inline bool operator<(const mtime& other) const
			{
				return t.tv_sec < other.t.tv_sec ||
				(t.tv_sec == other.t.tv_sec && t.tv_usec < other.t.tv_usec);
			}
			inline bool operator>(const mtime& other) const
			{
				return t.tv_sec > other.t.tv_sec ||
				(t.tv_sec == other.t.tv_sec && t.tv_usec > other.t.tv_usec);
			}
			inline bool operator==(const mtime& other) const
			{ return t.tv_sec == other.t.tv_usec && t.tv_usec == other.t.tv_usec; }
			inline bool operator!=(const mtime& other) const
			{ return t.tv_sec != other.t.tv_usec || t.tv_usec != other.t.tv_usec; }
		};
		
		class duration : public abstract_time
		{
		public:
			duration() : abstract_time() {}
			duration(const long sec, const int usec) : abstract_time(sec, usec)
			{
				if (t.tv_sec < 0)
				{
					t.tv_sec = 0;
					t.tv_usec = 0;
				}
			}
			duration(const struct timeval& tval) : abstract_time(tval)
			{
				if (t.tv_sec < 0)
				{
					t.tv_sec = 0;
					t.tv_usec = 0;
				}
			}
			
			virtual void sleep() const;
			inline int useconds() const
			{
				const long usec = t.tv_sec * USEC_IN_SEC + t.tv_usec;
				return (usec > INT_MAX ? INT_MAX : (int)usec);
			}
			
			inline mtime time_after() const
			{ return *this + mtime::now(); }
			
			inline mtime operator+(const mtime& other) const
			{ return mtime(plus(t, other.timeval())); }
			
			inline duration operator+(const duration& other) const
			{ return duration(plus(t, other.timeval())); }
			
			inline duration operator-(const duration& other) const
			{ return duration(minus(t, other.timeval())); }
			
			inline duration operator/(int div) const
			{ return duration(t.tv_sec/div,((t.tv_sec%div)*USEC_IN_SEC+t.tv_usec)/div); }
			
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
}

#endif /* defined(__CMuscle__mtime__) */

