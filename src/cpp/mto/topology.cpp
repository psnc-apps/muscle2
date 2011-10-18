#include <fstream>
#include <boost/regex.hpp>
#include "topology.h"

using namespace std;
using namespace boost;

#define DEBUG_TOPOLOGY_CONFIG_LOAD

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
    cout << "Config file open failed!" << endl;
    return false;
  }
  
  string line;
  smatch matches;
  while(!(line = getNextLine(file)).empty())
  {
    if(regex_match(line, matches, regex("^\\s*(\\S+)\\s*->\\s*(\\S+)\\s*$")))
    {
      if(results.find(matches[1])==results.end() || results.find(matches[2])==results.end())
      {
        cout << "Undefined machine in line:" << endl << line << endl;
        return false;
      }
      
      if(results[matches[2]].port == "")
      {
        cout << "Connecting to machine without open port in line:" << endl << line << endl;
        return false;
      }
        
      results[matches[1]].connectsTo.push_back(matches[2]);
     
#ifdef DEBUG_TOPOLOGY_CONFIG_LOAD
        cout << "Adding connection from " << matches[1] << " to " << matches[2] << endl;
#endif      
    }
    else
    {
      if(regex_match(line, matches, regex("^\\s*(\\S+)\\s*(\\S+)\\s*(\\d*)\\s*$")))
      {
        if(results.find(matches[1])!=results.end())
        {
          cout << "Redefinition of machine called \"" << matches[1] << "\"" << endl << line << endl;
          return false;
        }
        
        mto_config & cfg = results[matches[1]];
        cfg.address = matches[2];
        if(!matches[3].str().empty())
          cfg.port = matches[3];
          
#ifdef DEBUG_TOPOLOGY_CONFIG_LOAD
        cout << "Adding machine " << matches[1] << " as " << cfg.address;
        if(!cfg.port.empty())
          cout << ":" << cfg.port;
        else
          cout << " without open port";
        cout << endl;
#endif
      }
      else
      {
        cout << "Syntax error in config file, in line:" << endl << line << endl;
        return false;
      }
    }
  }
  return true;
}


// int main(int argc, char ** argv)
// {
//   loadTopology("new_config");
//   
//   return 0;
// }