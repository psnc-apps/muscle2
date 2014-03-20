//
//  DecoupledSelectSocket.h
//  CMuscle
//
//  Created by Joris Borgdorff on 3/18/14.
//  Copyright (c) 2014 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__DecoupledSelectSocket__
#define __CMuscle__DecoupledSelectSocket__

#include "muscle2/util/msocket.h"

namespace muscle {
	namespace net {
		class DecoupledSelectSocket : virtual public msocket
		{
		public:
			virtual void addReadReady(char thread) const;
			virtual char removeReadReady() const;
			virtual bool hasReadReady() const;
			virtual void addWriteReady(char thread) const;
			virtual char removeWriteReady() const;
			virtual bool hasWriteReady() const;
			virtual int getWriteSock() const;
			
			virtual void setBlocking(bool) {}
		protected:
			DecoupledSelectSocket();
			virtual ~DecoupledSelectSocket();
			static int select(int rs, int ws, int mask, int timeout_s, int timeout_u);
			const static int RDMASK, WRMASK;
			
			int writableReadFd, readableWriteFd, writableWriteFd;
		}; // end class DecoupledSelectSocket

	}
}

#endif /* defined(__CMuscle__DecoupledSelectSocket__) */
