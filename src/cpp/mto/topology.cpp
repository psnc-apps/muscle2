#include <fstream>
#include <sstream>
#include <set>
#include <boost/regex.hpp>
#include <boost/foreach.hpp>
#include "topology.h"
#include "logger.h"

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

bool hasLoopsInternal(map<string,set<string> > & links, set<string> & seen, const string & key)
{
  if(seen.find(key)!=seen.end())
    return true;
  seen.insert(key);
  set<string> copy(links[key]);
  links[key].empty();
  foreach(string next, copy)
  {
    links[next].erase(key);
    if( hasLoopsInternal(links, seen, next) )
      return true;
  }
  return false;
}

bool hasLoops(const map<string, mto_config> & results, map<string,set<string> > & links)
{
  set<string> seen;
  int subgraphs = 0;
  for(map<string, mto_config>::const_iterator it = results.begin(); it!=results.end(); ++it)
    if(seen.find(it->first)==seen.end())
    {
      subgraphs++;
      if(hasLoopsInternal(links, seen, it->first))
        return true;
    }
  
  if(subgraphs > 1)
    Logger::error(Logger::MsgType_Config, "Not all machines are linked together in the topology file!");
  return false;
}

bool loadTopology(const char * fname, map<string, mto_config> & results)
{
  ifstream file(fname);
  if(!file)
  {
    Logger::error(-1, "Config file open failed!");
    return false;
  }
  
  map<string,set<string> > links;
  
  string line;
  smatch matches;
  while(!(line = getNextLine(file)).empty())
  {
    if(regex_match(line, matches, regex("^\\s*(\\S+)\\s*->\\s*(\\S+)\\s*$")))
    {
      if(results.find(matches[1])==results.end() || results.find(matches[2])==results.end())
      {
        Logger::error(Logger::MsgType_Config, "Undefined machine in line: '%s'", line.c_str());
        return false;
      }
      
      if(results[matches[2]].port == "")
      {
        Logger::error(Logger::MsgType_Config, "Connecting to machine without open port in line: '%s'", line.c_str());
        return false;
      }
        
      results[matches[1]].connectsTo.push_back(matches[2]);
      
      if(    links[matches[1]].find(matches[2])!=links[matches[1]].end() 
          || links[matches[2]].find(matches[1])!=links[matches[2]].end() )
      {
        Logger::error(Logger::MsgType_Config, "Topology file contains loop from %s to %s. This is not supported!", matches[1].str().c_str(), matches[2].str().c_str());
        return false;
      }
      
      links[matches[1]].insert(matches[2]);
      links[matches[2]].insert(matches[1]);
     
      Logger::debug(Logger::MsgType_Config, "Adding connection from: %s to %s", matches[1].str().c_str(), matches[2].str().c_str());
    }
    else
    {
      if(regex_match(line, matches, regex("^\\s*(\\S+)\\s*(\\S+)\\s*(\\d*)\\s*$")))
      {
        if(results.find(matches[1])!=results.end())
        {
          Logger::error(Logger::MsgType_Config, "Redefinition of machine called %s", matches[1].str().c_str());
          return false;
        }
        
        mto_config & cfg = results[matches[1]];
        cfg.address = matches[2];
        if(!matches[3].str().empty())
          cfg.port = matches[3];
          

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
  }
  
  if(hasLoops(results, links))
  {
    Logger::error(Logger::MsgType_Config, "Topology file contains loop. This is not supported!");
    return false;
  }
  
  return true;
}
