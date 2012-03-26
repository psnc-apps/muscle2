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
      getline(file, line);
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
