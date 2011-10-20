#ifndef LOGGER_H
#define LOGGER_H

#include <cstdio>

namespace Logger
{
  static const int MsgType_Config     = 1<<0;
  static const int MsgType_PeerConn   = 1<<1;
  static const int MsgType_ClientConn = 1<<2;
  
  static const int LogLevel_Error = 30;
  static const int LogLevel_Info  = 20;
  static const int LogLevel_Debug = 10;
  static const int LogLevel_Trace =  0;
  
  void setLogLevel(int logLevel);
   int getLogLevel();
  
  void setLogMsgTypes(int logMsgTypes);
  
  void log(int logLevel_, int logMsgTypes_, const char* format, ...);
  
  void trace(int logMsgTypes_, const char* format, ...);
  void info(int logMsgTypes_, const char* format, ...);
  void debug(int logMsgTypes_, const char* format, ...);
  void error(int logMsgTypes_, const char* format, ...);
  
  void setLogStream(FILE * stream);
};

#endif // LOGGER_H
