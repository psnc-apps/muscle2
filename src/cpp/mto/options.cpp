#include "options.h"
#include "logger.h"

#include <boost/algorithm/string.hpp>
#include <boost/foreach.hpp>
using namespace boost::algorithm;

#define foreach BOOST_FOREACH

#include <cstdio>

unsigned short localPortLow, localPortHigh;     ///< Local port range
string myName;
tcp::endpoint internalEndpoint;

string configFilePath;
string topologyFilePath;

bool daemonize = false;

bool setLogFile(string x){
  FILE * file = fopen(x.c_str(), "a");
  if(!file)
  {
    Logger::error(-1, "Failed to open file '%s' for loging!", x.c_str());
    return false;
  }
  Logger::setLogStream(file);
  return true;
}

bool setLogLvL(string f){
  to_upper(f);
  if(f=="TRACE"){
      Logger::setLogLevel(Logger::LogLevel_Trace);
    } else if(f=="INFO") {
      Logger::setLogLevel(Logger::LogLevel_Info);
    } else if(f=="DEBUG") {
      Logger::setLogLevel(Logger::LogLevel_Debug);
    } else if(f=="ERROR") {
      Logger::setLogLevel(Logger::LogLevel_Error);
    } else {
      stringstream ss(f);
      int type;
      ss >> type;
      if(!ss.eof())
        return false;
      Logger::setLogLevel(type);
    }
    return true;
}

bool setLogMsgType(string x){
  vector<string> found;
  split(found, x, is_any_of(",|"), token_compress_on);
  
  int types = 0;
  
  foreach (string f , found)
  {
    to_upper(f);
    if(f=="PEER"){
      types|=Logger::MsgType_PeerConn;
    } else if(f=="CONFIG") {
      types|=Logger::MsgType_Config;
    } else if(f=="CLIENT") {
      types|=Logger::MsgType_ClientConn;
    } else {
      stringstream ss(f);
      int type;
      ss >> type;
      if(!ss.eof())
        return false;
      types |=type;
    }
  }
  
  Logger::setLogMsgTypes(types);
  
  Logger::info(-1,"Logging events %d ( %s %s %s )", types, 
               (types&Logger::MsgType_PeerConn?"PEER":""),
               (types&Logger::MsgType_Config?"CONFIG":""),
               (types&Logger::MsgType_ClientConn?"CLIENT":"")
              );
  
  return true;
}

bool loadOptions(int argc, char **argv)
{
  program_options::options_description opts("Options");
  opts.add_options()
    ("config", program_options::value<string>(), "Location of the config file (default: $MUSCLE_HOME/etc/mto-config.cfg)")
    ("topology", program_options::value<string>(), "Location of the config file (default: $MUSCLE_HOME/etc/mto-topology.cfg)")
    
    ("myName", program_options::value<string>(), "Name of the MTO, s specified in the topology file")
    
    ("localPortLow", program_options::value<unsigned short>(), "Low limit of the local port range")
    ("localPortHigh", program_options::value<unsigned short>(), "High limit of the local port range")
    
    ("internalPort", program_options::value<string>(), "Port to listen for conections to be transported")
    ("internalAddress", program_options::value<string>(), "Address to listen for conections to be transported")
    
    ("daemon", "Causes the program to go to background immediatly after start")
    ("logLevel", program_options::value<string>(), "Level for logging (TRACE,DEBUG,INFO,ERROR, default INFO)")
    ("logMsgTypes", program_options::value<string>(), "Allows filtering log msgs (PEER,CONFIG,CLIENT, default: PEER|CONFIG|CLIENT)")
    ("logFile", program_options::value<string>(), "Path to log file (default behaviour - logging to stderr)")
  ;
  
  program_options::variables_map read_opts;
  
  bool logFileChanged = false;
  string muscle_home = string(getenv("MUSCLE_HOME") ? getenv("MUSCLE_HOME") : ".");
  
  // Reading opts, in precedence the ones from arguments
  try
  {
    program_options::store(program_options::parse_command_line(argc, argv, opts), read_opts);
    if(read_opts.find("logFile")!=read_opts.end())
    {
      if( ! setLogFile(read_opts["logFile"].as<string>()))
        return false;
      logFileChanged = true;
    }
    read_opts.erase("logFile");
  
  }
  catch(program_options::unknown_option)
  {
    opts.print(cout);
    return false;
  }
  
  // Locate config
  if(read_opts.find("config")!=read_opts.end())
    configFilePath = read_opts["config"].as<string>();
  if(configFilePath.empty()) configFilePath = muscle_home + "/etc/" + CONFIG_FILE_NAMEPATH;
  
  // Locate topology
  if(read_opts.find("topology")!=read_opts.end())
    topologyFilePath = read_opts["topology"].as<string>();
  if(topologyFilePath.empty()) topologyFilePath = muscle_home + "/etc/" + TOPOLOGY_FILE_NAMEPATH;
  
  // Reading opts, complement with config file
    
  ifstream configFile(configFilePath.c_str());
  if(configFile)
  {
    Logger::info(Logger::MsgType_Config, "Config file '%s'", configFilePath.c_str());
    
    try
    {
      program_options::store(program_options::parse_config_file(configFile, opts), read_opts);
      if(!logFileChanged && read_opts.find("logFile")!=read_opts.end())
        if( ! setLogFile(read_opts["logFile"].as<string>()))
          return false;
      read_opts.erase("logFile");
  
    }
    catch(program_options::unknown_option)
    {
      opts.print(cout);
      return false;
    }
  }
  else
  {
    Logger::info(Logger::MsgType_Config, "Could not open config file '%s'", configFilePath.c_str());
  }

  Logger::info(Logger::MsgType_Config, "Topology file '%s'", topologyFilePath.c_str());

  // Logging
    
  if(read_opts.find("logMsgTypes")!=read_opts.end())
    if(!setLogMsgType(read_opts["logMsgTypes"].as<string>()))
      return false;
  read_opts.erase("logMsgTypes");
  
  if(read_opts.find("logLevel")!=read_opts.end())
    if(!setLogLvL(read_opts["logLevel"].as<string>()))
      return false;
  read_opts.erase("logLevel");
  
  int l = Logger::getLogLevel();
  Logger::info(-1, "Logging level %d (%s)", l, 
	( l == Logger::LogLevel_Trace ? "TRACE":
	( l == Logger::LogLevel_Debug ? "DEBUG":
	( l == Logger::LogLevel_Info ? "INFO":
	( l == Logger::LogLevel_Error ? "ERROR": 
	  "?")))));
  
  // Daemon
  
  if(read_opts.find("daemon")!=read_opts.end()){
    Logger::debug(Logger::MsgType_Config, "Will daemonize");
    daemonize = true;
  }
  read_opts.erase("daemon");
  
  // Remove optional opts
  read_opts.erase("config");
  read_opts.erase("topology");
  
  // Count check
  
  if (read_opts.size() != 5)
  {
    cerr << "Some option not specified!" << endl;
    opts.print(cout);
    return false;
  }
  
  
  // Port range
  localPortLow   = read_opts["localPortLow"].as<unsigned short>();
  localPortHigh  = read_opts["localPortHigh"].as<unsigned short>();
  
  Logger::info(Logger::MsgType_Config, "My port range: %hu - %hu", localPortLow, localPortHigh);
  
  // Name
  myName =  read_opts["myName"].as<string>();
  
  Logger::info(Logger::MsgType_Config, "My name in topology file: %s", myName.c_str());
  Logger::info(Logger::MsgType_Config, "Internal address: %s:%s", read_opts["internalAddress"].as<string>().c_str(), read_opts["internalPort"].as<string>().c_str());
  
  // Internal endpoint (address and port)
  tcp::resolver resolver(ioService);
  error_code e;
  tcp::resolver::query indoorQuery(read_opts["internalAddress"].as<string>(),read_opts["internalPort"].as<string>());
  tcp::resolver::iterator indoor = resolver.resolve(indoorQuery,e);
  if(e || tcp::resolver::iterator() == indoor)
  {
    Logger::error(Logger::MsgType_Config, "unknown host/port for internal socket: %s:%s (error: %s)",
                  read_opts["internalAddress"].as<string>().c_str(),
                  read_opts["internalPort"].as<string>().c_str(),
                  e.message().c_str()
            );
    return false;
  }
  internalEndpoint = *indoor;
  
  // Port range validity check
  if(localPortLow > localPortHigh)
  {
    Logger::error(Logger::MsgType_Config, "Inverted limits in the port range");
    return false;
  }
  
  return true;
}
