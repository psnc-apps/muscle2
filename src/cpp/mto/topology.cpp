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
#include <fstream>
#include <sstream>
#include <set>
#include <boost/regex.hpp>
#include <boost/foreach.hpp>
#include "topology.hpp"
#include "logger.hpp"

using namespace std;
using namespace boost;

#define foreach BOOST_FOREACH

string getNextLine(ifstream & file)
{
  string line;
    do
    {
      std::getline(file, line);
      // blank & empty lines
      if(regex_match(line, regex("^\\s*$")))
        continue;
      // comments
      if(regex_match(line, regex("^\\s*(//|#).*")))
        continue;
      break;
    } while( file.good() );
    return line;
}

bool loadTopology(const char * fname, map<string, mto_config> & results)
{
  ifstream file(fname);
  if(!file)
  {
    Logger::error(-1, "Opening topology file (%s) failed!", fname);
    return false;
  }
  
  string line;
  smatch matches;
  while(!(line = getNextLine(file)).empty())
  {
    if(regex_match(line, matches, regex("^\\s*(\\S+)\\s*(\\S+)\\s*(\\d*)\\s*$")))
    {
      if(results.find(matches[1])!=results.end())
      {
        Logger::error(Logger::MsgType_Config, "Redefinition of machine  %s", matches[1].str().c_str());
        return false;
      }
      
      mto_config & cfg = results[matches[1]];
      cfg.address = matches[2];
      if(!matches[3].str().empty())
        cfg.port = matches[3];
      else
        cfg.port = "";
        

      stringstream ss;
      ss << "Adding machine " << matches[1] << " as " << cfg.address;
      if(!cfg.port.empty())
        ss << ":" << cfg.port;
      else
        ss << " without open port";
      Logger::debug(Logger::MsgType_Config, ss.str().c_str());

    }
    else
    {
      Logger::error(Logger::MsgType_Config, "Syntax error in config file, in line: '%s'", line.c_str());
      return false;
    }
  }
  
  return true;
}
