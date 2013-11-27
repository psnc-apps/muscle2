//
//  serialization.h
//  CMuscle
//
//  Created by Joris Borgdorff on 05-08-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef CMuscle_serialization_h
#define CMuscle_serialization_h

namespace muscle {
	namespace net {
		template <typename T>
		inline static void writeToBuffer(char *&buffer, const T value)
		{
			unsigned char *buffer_ptr = (unsigned char *)buffer;
			
			for (size_t i = 0; i < sizeof(T); ++i)
				*buffer_ptr++ = (value >> 8*(sizeof(T)-i-1)) & 0xff;
			
			buffer = (char *)buffer_ptr;
		}
		
		template <typename T>
		inline static T readFromBuffer(char *&buffer)
		{
			
			unsigned char *buffer_ptr = (unsigned char *)buffer;
			T value = 0;
			
			for (size_t i = 0; i < sizeof(T); ++i)
				value |= T(*buffer_ptr++) << 8*(sizeof(T)-i-1);
			
			buffer = (char *)buffer_ptr;
			return value;
		}
	}
}

#endif
