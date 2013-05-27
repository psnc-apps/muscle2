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
  
  void closeLogFile();
};

#endif // LOGGER_H
