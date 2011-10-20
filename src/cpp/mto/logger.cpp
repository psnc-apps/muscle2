#include "logger.h"
#include <string.h>
#include <cstdarg>

#include <boost/date_time.hpp>

using namespace boost::posix_time;

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
  char * fmt = new char[ /* name */ 6 + /* date */ 15 + strlen(format) + /* "\n\0" */ 2];
  time_duration time = microsec_clock::local_time() - from_time_t(0);
  sprintf(fmt, "      %14.3f %s\n", time.total_milliseconds()/1000.0, format);
  
  if(logLevel_ == LogLevel_Error) {
    strncpy(fmt, "ERROR", 5);
  } else if(logLevel_ == LogLevel_Debug) {
    strncpy(fmt, "DEBUG", 5);
  } else if(logLevel_ == LogLevel_Info) {
    strncpy(fmt, " INFO", 5);
  } else if(logLevel_ == LogLevel_Trace) {
    strncpy(fmt, "TRACE", 5);
  } else {
    char unknown[11];
    sprintf(unknown, "%-5d", logLevel_);
    strncpy(fmt, unknown, 5);
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
  delete extendedFormat;
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

}