#include "options.h"

unsigned short localPortLow, localPortHigh;     ///< Local port range
string myName;
tcp::endpoint internalEndpoint;

string configFilePath;
string topologyFilePath;


bool loadOptions(int argc, char **argv)
{
  program_options::options_description opts("Options");
  opts.add_options()
    ("config", program_options::value<string>(), "Location of the config file (default: mto-config.cfg)")
    ("topology", program_options::value<string>(), "Location of the config file (default: mto-topology.cfg)")
    
    ("myName", program_options::value<string>(), "Name of the MTO, s specified in the topology file")
    
    ("localPortLow", program_options::value<unsigned short>(), "Low limit of the local port range")
    ("localPortHigh", program_options::value<unsigned short>(), "High limit of the local port range")
    
    ("internalPort", program_options::value<string>(), "Port to listen for conections to be transported")
    ("internalAddress", program_options::value<string>(), "Address to listen for conections to be transported")
  ;
  
  program_options::variables_map read_opts;
  
  // Reading opts, in precedence the ones from arguments
  
  program_options::store(program_options::parse_command_line(argc, argv, opts), read_opts);
  
  // Locate config
  if(read_opts.find("config")!=read_opts.end())
    configFilePath = read_opts["config"].as<string>();
  if(configFilePath.empty()) configFilePath = CONFIG_FILE_NAMEPATH;
  
  // Locate topology
  if(read_opts.find("topology")!=read_opts.end())
    topologyFilePath = read_opts["topology"].as<string>();
  if(topologyFilePath.empty()) topologyFilePath = TOPOLOGY_FILE_NAMEPATH;
  
  
  // Reading opts, complement with config file
    
  ifstream configFile(configFilePath.empty() ? CONFIG_FILE_NAMEPATH : configFilePath.c_str());
  if(configFile)
    program_options::store(program_options::parse_config_file(configFile, opts), read_opts);

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
  
  // Name
  myName =  read_opts["myName"].as<string>();
  
  // Internal endpoint (address and port)
  tcp::resolver resolver(ioService);
  error_code e;
  tcp::resolver::query indoorQuery(read_opts["internalAddress"].as<string>(),read_opts["internalPort"].as<string>());
  tcp::resolver::iterator indoor = resolver.resolve(indoorQuery,e);
  if(e || tcp::resolver::iterator() == indoor)
  {
    cerr << "unknown host/port: " << read_opts["internalAddress"].as<string>() << ":" << read_opts["internalPort"].as<string>() << endl;
    return false;
  }
  internalEndpoint = *indoor;
  
  // Port range validity check
  if(localPortLow > localPortHigh)
  {
    cerr << "Inverted limits in the port range" << endl;
    return false;
  }
  
  return true;
}
