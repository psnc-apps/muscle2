//
//  custom_serializer.cpp
//  CMuscle
//
//  Created by Joris Borgdorff on 17-12-13.
//  Copyright (c) 2013 Joris Borgdorff. All rights reserved.
//

#include "custom_serializer.h"
#include <cstdlib> // free()
#include <cstring> // memcpy, strlen

#define NEGATIVE_BIT (0x80000000)

namespace muscle {
	namespace net {
		static bool isLittleEndian() {
			union {
				int32_t input;
				char output[4];
			} endianInt;
			endianInt.input = 0x01020304;
			return (endianInt.output[0] == 4 && endianInt.output[1] == 3 && endianInt.output[2] == 2 && endianInt.output[3] == 1);
		}
		
		const bool custom_serializer::needToConvert = !isLittleEndian();
		const bool custom_deserializer::needToConvert = !isLittleEndian();
		
		custom_serializer::custom_serializer(muscle::net::ClientSocket **sock, size_t bufsize) : sock(sock), bufsize(bufsize)
		{
			if (bufsize < 1024)
				throw muscle_exception("Minimum buffer size is 1024 bytes");
			
			buffer = new char[bufsize];
			buffer_end = buffer + bufsize;
			buffer_ptr = buffer + 4;
		}
		custom_serializer::~custom_serializer()
		{
			delete [] buffer;
		}
		
		void custom_serializer::flush()
		{
			size_t len = buffer_ptr - buffer - 4;
			buffer_ptr = buffer;
			writeInt((uint32_t)(len | NEGATIVE_BIT));
			(*sock)->sendAll(buffer, len + 4);
		}
		
		void custom_serializer::encodeString(const char * const value)
		{
			size_t len = strlen(value);
			encodeByteArray(value, len);
		}
		
		void custom_serializer::encodeStringSafe(const char * const value, size_t maxlen)
		{
			size_t len = strnlen(value, maxlen - 1);
			encodeByteArray(value, len);
		}

		void custom_serializer::encodeByteArray(const char * const value, const size_t len)
		{
			writeData(value, len, len);
		}
		
		void custom_serializer::encodeLongArray(const int64_t * const value, const size_t len)
		{
			if (needToConvert) {
				writeInt((uint32_t)len);
				for (size_t i = 0; i < len; ++i) {
					writeLong(value[i]);
				}
			} else {
				writeData(value, len*8, len);
			}
		}
		void custom_serializer::encodeIntArray(const int32_t * const value, const size_t len)
		{
			if (needToConvert) {
				writeInt((uint32_t)len);
				for (size_t i = 0; i < len; ++i) {
					writeInt(value[i]);
				}
			} else {
				writeData(value, len*4, len);
			}
		}
		
		void custom_serializer::encodeBooleanArray(const bool * const value, const size_t len)
		{
			writeInt((uint32_t)len);
			for (size_t i = 0; i < len; ++i) {
				writeByte(value[i] ? 1 : 0);
			}
		}
		
		void custom_serializer::encodeShortArray(const int16_t * const value, const size_t len)
		{
			if (needToConvert) {
				writeInt((uint32_t)len);
				for (size_t i = 0; i < len; ++i) {
					writeShort(value[i]);
				}
			} else {
				writeData(value, len*2, len);
			}
		}
		
		void custom_serializer::writeData(const void *data, size_t len, size_t reported_len)
		{
			// If the buffer is full, flush it first
			if (buffer_ptr + len + 4 >= buffer_end)
				flushBuffer();
			
			// If the buffer is too small in general, send the data directly
			if (buffer_ptr + len + 4 >= buffer_end) {
				// buffer was already flushed, so we can rewind
				buffer_ptr = buffer;
				// fragment size
				writeInt((uint32_t)len + 4);
				// byte array size
				writeInt((uint32_t)reported_len);
				// rewind buffer
				buffer_ptr = buffer + 4;
				
				(*sock)->sendAll(buffer, 8);
				(*sock)->sendAll(data, len);
			} else {
				writeInt((uint32_t)reported_len);
				memcpy(buffer_ptr, data, len);
				buffer_ptr += len;
			}
		}
		
		void custom_serializer::flushBuffer()
		{
			size_t len = buffer_ptr - buffer - 4;
			buffer_ptr = buffer;
			writeInt((uint32_t)len);
			(*sock)->sendAll(buffer, len + 4);
		}
		
		custom_deserializer::custom_deserializer(muscle::net::ClientSocket **sock, size_t bufsize) : sock(sock), bufsize(bufsize), fragmentRemaining(0), lastFragment(false)
		{
			if (bufsize < 1024)
				throw muscle_exception("Minimum buffer size is 1024 bytes");
			
			buffer = new char[bufsize];
			buffer_ptr = (unsigned char *)buffer;
			filledSize = 0;
		}
		
		custom_deserializer::~custom_deserializer()
		{
			delete [] buffer;
		}
		
		void custom_deserializer::fill(size_t sz)
		{
			if (fragmentRemaining == 0) {
				// In case there is no more data in the current XDR record
				// (since we already saw the last fragment), throw an exception.
				if (lastFragment) throw muscle_exception("Record empty");
				
				read(4);
				fragmentRemaining = 4;
				const int fragmentHead = readInt();
				
				// XDR header is the last one if the sign is negative
				// and the other bits are the size of the fragment
				fragmentRemaining = fragmentHead & ~NEGATIVE_BIT;
				lastFragment = (fragmentRemaining != fragmentHead);
			}
			if (fragmentRemaining < sz)
				throw muscle_exception("Record does not store data");

			read(sz);
			fragmentRemaining -= sz;
			filledSize -= sz;
		}
		
		void custom_deserializer::read(size_t minimal)
		{
			if (filledSize < minimal) {
				// move data to the start of the buffer
				if (buffer_ptr > (unsigned char *)buffer) {
					if (filledSize > 0) {
						memmove(buffer, buffer_ptr, filledSize);
					}
					buffer_ptr = (unsigned char *)buffer;
				}
				filledSize += (*sock)->recvAll(buffer + filledSize, minimal - filledSize, bufsize - filledSize);;
			}
		}
		
		void custom_deserializer::endDecoding()
		{
			// Clear all buffers that are still remaining
			do {
				if (fragmentRemaining > 0) {
					if (fragmentRemaining <= filledSize) {
						buffer_ptr += fragmentRemaining;
						filledSize -= fragmentRemaining;
						fragmentRemaining = 0;
					} else {
						fragmentRemaining -= filledSize;
						buffer_ptr = (unsigned char *)buffer;
						filledSize = 0;
						ssize_t recvd = (*sock)->recv(buffer, fragmentRemaining < bufsize ? fragmentRemaining : bufsize);
						if (recvd == -1)
							throw muscle_exception("Stream does not do full seek");
						
						fragmentRemaining -= recvd;
					}
				}
				
				if (lastFragment && fragmentRemaining == 0) {
					// restart
					lastFragment = false;
					break;
				} else {
					fill(0);
				}
			} while (true);
		}
		
		char *custom_deserializer::decodeString(char *value, size_t * const len)
		{
			if (len != NULL) (*len)++;
			readArrayLen(len, value != NULL);

			(*len)++;
			if (value == NULL)
				value = (char *)malloc(*len);

			value[*len - 1] = '\0';
			
			readBytes(value, *len - 1);
			
			return value;
		}
		
		char *custom_deserializer::decodeByteArray(char *value, size_t * const len)
		{
			readArrayLen(len, value != NULL);

			if (value == NULL)
				value = (char *)malloc(*len);
			
			readBytes(value, *len);
			
			return value;
		}
		
		void custom_deserializer::readBytes(char *value, const size_t len)
		{
			size_t recvd = 0;
			while (len > recvd) {
				fill(0);

				const size_t toFill = fragmentRemaining <= len - recvd ? fragmentRemaining : len - recvd;
				
				size_t newBytes;
				if (filledSize > 0) {
					newBytes = toFill <= filledSize ? toFill : filledSize;
					memcpy(value + recvd, buffer_ptr, newBytes);
					if (newBytes == filledSize) {
						buffer_ptr = (unsigned char *)buffer;
						filledSize = 0;
					} else {
						buffer_ptr += newBytes;
						filledSize -= newBytes;
					}
				} else if (toFill > 1024) {
					newBytes = (*sock)->recv(value + recvd, toFill);
					if (newBytes == -1)
						throw muscle_exception("Stream does not do full seek");
				} else {
					newBytes = 0;
					read(1);
				}
				
				recvd += newBytes;
				fragmentRemaining -= newBytes;
			}
		}
		
		bool *custom_deserializer::decodeBooleanArray(bool *value, size_t *len)
		{
			readArrayLen(len, value != NULL);
			if (value == NULL)
				value = (bool *)malloc(*len * sizeof(bool));
			for (size_t i = 0; i < *len; ++i) {
				value[i] = decodeBoolean();
			}
			return value;
		}
		int32_t *custom_deserializer::decodeIntArray(int32_t *value, size_t *len)
		{
			readArrayLen(len, value != NULL);
			if (value == NULL)
				value = (int32_t *)malloc(*len * sizeof(int32_t));
			if (needToConvert) {
				for (size_t i = 0; i < *len; ++i) {
					value[i] = readInt();
				}
			} else {
				readBytes((char *)value, *len*4);
			}
			return value;
		}
		int64_t *custom_deserializer::decodeLongArray(int64_t *value, size_t *len)
		{
			readArrayLen(len, value != NULL);
			if (value == NULL)
				value = (int64_t *)malloc(*len * sizeof(int64_t));
			if (needToConvert) {
				for (size_t i = 0; i < *len; ++i) {
					value[i] = readLong();
				}
			} else {
				readBytes((char *)value, *len*8);
			}
			return value;
		}
		int16_t *custom_deserializer::decodeShortArray(int16_t *value, size_t *len)
		{
			readArrayLen(len, value != NULL);
			if (value == NULL)
				value = (int16_t *)malloc(*len * sizeof(int16_t));
			if (needToConvert) {
				for (size_t i = 0; i < *len; ++i) {
					value[i] = readShort();
				}
			} else {
				readBytes((char *)value, *len*2);
			}
			return value;
		}
		
		void custom_deserializer::readArrayLen(size_t *const len, const bool checklen)
		{
			if (len == NULL)
				throw muscle_exception("Length argument is mandatory");
			
			const size_t recvLen = decodeInt();
			if (checklen && *len < recvLen) {
				logger::severe("Maximum array length %zu provided (%zu received)", *len, recvLen);
				throw muscle_exception("Provided maximum array length is exceeded");
			}
			
			*len = recvLen;
		}
	}
}

