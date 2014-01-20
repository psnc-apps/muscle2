//
//  custom_serializer.h
//  CMuscle
//
//  Created by Joris Borgdorff on 17-12-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#ifndef __CMuscle__custom_serializer__
#define __CMuscle__custom_serializer__

#include "msocket.h"
#include <sys/types.h>

namespace muscle {
	namespace net {
		class custom_serializer {
		public:
			custom_serializer(muscle::net::ClientSocket *sock, size_t bufsize);
			virtual ~custom_serializer();
			
			inline void encodeInt(int32_t value) { writeInt(value); }
			inline void encodeLong(int64_t value)  { writeLong(value); }
			inline void encodeBoolean(bool value) { writeByte(value ? 1 : 0); }
			inline void encodeByte(char value) { writeByte(value); }
			inline void encodeShort(int16_t value) { writeShort(value); }
			inline void encodeFloat(float value)
			{
				union {
					float input;
					uint32_t output;
				} float2int;
				float2int.input = value;
				writeInt(float2int.output);
			}
			void encodeDouble(double value)
			{
				union {
					double input;
					uint64_t output;
				} double2long;
				double2long.input = value;
				writeLong(double2long.output);
			}
			void encodeString(const char *value);

			void encodeByteArray(const char *value, size_t len);
			void encodeDoubleArray(const double *value, size_t len)
			{ encodeLongArray((const int64_t *)value, len);	}
			void encodeFloatArray(const float *value, size_t len)
			{ encodeIntArray((const int32_t *)value, len); }
			void encodeBooleanArray(const bool *value, size_t len);
			void encodeShortArray(const int16_t *value, size_t len);
			void encodeIntArray(const int32_t *value, size_t len);
			void encodeLongArray(const int64_t *value, size_t len);
			
			void flush();
		private:
			muscle::net::ClientSocket * const sock;
			char *buffer;
			const size_t bufsize;
			char *buffer_end, *buffer_ptr;
			
			inline void writeInt(uint32_t value)
			{
				if (buffer_ptr + 4 > buffer_end) flushBuffer();
				*buffer_ptr++ = (value      ) & 0xff;
				*buffer_ptr++ = (value >> 8 ) & 0xff;
				*buffer_ptr++ = (value >> 16) & 0xff;
				*buffer_ptr++ = (value >> 24) & 0xff;
			}
			inline void writeLong(uint64_t value)
			{
				if (buffer_ptr + 8 > buffer_end) flushBuffer();
				*buffer_ptr++ = (value      ) & 0xff;
				*buffer_ptr++ = (value >> 8 ) & 0xff;
				*buffer_ptr++ = (value >> 16) & 0xff;
				*buffer_ptr++ = (value >> 24) & 0xff;
				*buffer_ptr++ = (value >> 32) & 0xff;
				*buffer_ptr++ = (value >> 40) & 0xff;
				*buffer_ptr++ = (value >> 48) & 0xff;
				*buffer_ptr++ = (value >> 56) & 0xff;
			}
			
			inline void writeShort(uint16_t value)
			{
				if (buffer_ptr + 2 > buffer_end) flushBuffer();
				*buffer_ptr++ = (value      ) & 0xff;
				*buffer_ptr++ = (value >> 8 ) & 0xff;
			}
			
			inline void writeByte(char value)
			{
				if (buffer_ptr + 1 > buffer_end) flushBuffer();
				*buffer_ptr++ = value;
			}
			
			void writeData(const void *data, size_t len, size_t reported_len);
			
			void flushBuffer();
			
			static const bool needToConvert;
		};
		
		class custom_deserializer {
		public:
			custom_deserializer(muscle::net::ClientSocket *sock, size_t bufsize);
			virtual ~custom_deserializer();
			
			inline int32_t decodeInt() { return readInt(); }
			inline int64_t decodeLong()  { return readLong(); }
			inline bool decodeBoolean() { return readByte() == 1; }
			inline char decodeByte() { return readByte(); }
			inline int16_t decodeShort() { return readShort(); }
			inline float decodeFloat()
			{
				union {
					uint32_t input;
					float output;
				} float2int;
				float2int.input = readInt();
				return float2int.output;
			}
			inline double decodeDouble()
			{
				union {
					uint64_t input;
					double output;
				} long2double;
				long2double.input = readLong();
				return long2double.output;
			}
			char *decodeString(char *value, size_t *len);
			char *decodeByteArray(char *value, size_t *len);
			bool *decodeBooleanArray(bool *value, size_t *len);
			inline float *decodeFloatArray(float *value, size_t *len) {
				return (float *)decodeIntArray((int32_t *)value, len);
			}
			double *decodeDoubleArray(double *value, size_t *len) {
				return (double *)decodeLongArray((int64_t *)value, len);
			}
			int32_t *decodeIntArray(int32_t *value, size_t *len);
			int64_t *decodeLongArray(int64_t *value, size_t *len);
			int16_t *decodeShortArray(int16_t *value, size_t *len);
			
			void endDecoding();
		private:
			muscle::net::ClientSocket * const sock;
			char *buffer;
			const size_t bufsize;
			size_t filledSize;
			unsigned char *buffer_ptr;
			size_t fragmentRemaining;
			bool lastFragment;
			static const bool needToConvert;
			
			inline uint32_t readInt()
			{
				fill(4);
				uint32_t value = *buffer_ptr++;
				value |= uint32_t(*buffer_ptr++) << 8;
				value |= uint32_t(*buffer_ptr++) << 16;
				value |= uint32_t(*buffer_ptr++) << 24;
				return value;
			}

			inline uint64_t readLong()
			{
				fill(8);
				uint64_t value = *buffer_ptr++;
				value |= uint64_t(*buffer_ptr++) << 8;
				value |= uint64_t(*buffer_ptr++) << 16;
				value |= uint64_t(*buffer_ptr++) << 24;
				value |= uint64_t(*buffer_ptr++) << 32;
				value |= uint64_t(*buffer_ptr++) << 40;
				value |= uint64_t(*buffer_ptr++) << 48;
				value |= uint64_t(*buffer_ptr++) << 56;
				return value;
			}
			
			inline uint16_t readShort()
			{
				fill(2);
				uint16_t value = *buffer_ptr++;
				value |= uint16_t(*buffer_ptr++) << 8;
				return value;
			}
			
			inline char readByte()
			{
				fill(1);
				return *buffer_ptr++;
			}
			
			void readArrayLen(size_t *len, bool checklen);
			
			void fill(size_t sz);
			void read(size_t sz);
			
			void readBytes(char *value, size_t len);
		};
	}
}

#endif /* defined(__CMuscle__custom_serializer__) */
