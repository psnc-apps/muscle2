/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "logger.hpp"
#include <string.h>
#include <cstdarg>

#ifdef USE_BOOST_FOR_GETTING_TIME
  #include <boost/date_time.hpp>
  using namespace boost::posix_time;
#else
  #include <sys/time.h>
  #include <ctime>
#endif

namespace Logger
{
  
FILE * stream  = stderr;
int logMsgTypes = 0xffffffff;
int logLevel = LogLevel_Info;

void setLogStream(FILE * stream_)
{
  stream = stream_;
}

void setLogLevel(int logLevel_)
{
  logLevel = logLevel_;
}

int getLogLevel()
{
  return logLevel;
}


void setLogMsgTypes(int logMsgTypes_)
{
  logMsgTypes = logMsgTypes_;
}

char * getPrefix(int logLevel_, int logMsgTypes_, const char* format)
{
#ifdef USE_BOOST_FOR_GETTING_TIME
  char * fmt = new char[ /* name */ 8 + /* date */ 15 + strlen(format) + /* "\n\0" */ 2];
  time_duration time = microsec_clock::local_time() - from_time_t(0);
  sprintf(fmt, "        %14.3f %s\n", time.total_milliseconds()/1000.0, format);
#else
  char * fmt = new char[ /* name */ 8 + /* date */ 24 + strlen(format) + /* "\n\0" */ 2];
  timeval now; gettimeofday(&now,0);
  sprintf(fmt, "                           .%03d %s\n", (int) now.tv_usec/1000, format);
  tm * timeTm = localtime(&now.tv_sec);
  strftime(fmt+8, 20, "%Y.%m.%d %H:%M:%S ", timeTm);
#endif
  
  if(logLevel_ == LogLevel_Error) {
    strncpy(fmt, "[ERROR]", 7);
  } else if(logLevel_ == LogLevel_Debug) {
    strncpy(fmt, "[DEBUG]", 7);
  } else if(logLevel_ == LogLevel_Info) {
    strncpy(fmt, "[ INFO]", 7);
  } else if(logLevel_ == LogLevel_Trace) {
    strncpy(fmt, "[TRACE]", 7);
  } else {
    char unknown[11];
    sprintf(unknown, "[%-5d]", logLevel_);
    strncpy(fmt, unknown, 7);
  }
  
  return fmt;
}


void log(int logLevel_, int logMsgTypes_, const char* format, va_list ap )
{
  if(logLevel_ < logLevel)
    return;
  
  if(!(logMsgTypes_ & logMsgTypes))
    return;
  
  char * extendedFormat = getPrefix(logLevel_, logMsgTypes_, format);
  vfprintf(stream, extendedFormat, ap);
  fflush(stream);
  delete [] extendedFormat;
}

void log(int logLevel_, int logMsgTypes_, const char* format, ... )
{
  va_list ap;
  va_start(ap, format);
  log(logLevel_, logMsgTypes_, format, ap);
  va_end(ap);
}

void trace(int logMsgTypes_, const char* format, ...) { va_list ap; va_start(ap, format); log(LogLevel_Trace, logMsgTypes_, format, ap); va_end(ap);}
void  info(int logMsgTypes_, const char* format, ...) { va_list ap; va_start(ap, format); log(LogLevel_Info,  logMsgTypes_, format, ap); va_end(ap);}
void debug(int logMsgTypes_, const char* format, ...) { va_list ap; va_start(ap, format); log(LogLevel_Debug, logMsgTypes_, format, ap); va_end(ap);}
void error(int logMsgTypes_, const char* format, ...) { va_list ap; va_start(ap, format); log(LogLevel_Error, logMsgTypes_, format, ap); va_end(ap);}

void closeLogFile()
{
  fclose(stream);
}

}
