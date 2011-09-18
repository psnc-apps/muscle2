#include "options.h"

unsigned short localPortLow, localPortHigh;
unsigned short remotePortLow, remotePortHigh;
tcp::endpoint my_address;
tcp::endpoint indoorEndpoint;
tcp::resolver::iterator peer_address;

bool loadOptions(int argc, char **argv)
{
  program_options::options_description opts("Options");
  opts.add_options()
    ("localPortLow", program_options::value<unsigned short>(), "Low limit of the local port range")
    ("localPortHigh", program_options::value<unsigned short>(), "High limit of the local port range")
    ("remotePortLow", program_options::value<unsigned short>(), "Low limit of the remote port range")
    ("remotePortHigh", program_options::value<unsigned short>(), "High limit of the remote port range")
    ("peerAddress", program_options::value<string>(), "Address of the remote MTO")
    ("peerPort", program_options::value<string>(), "Port of the remote MTO")
    ("myAddress", program_options::value<string>(), "Address to listen for the other MTO")
    ("myPort", program_options::value<string>(), "Port to listen for the other MTO")
    ("indoorPort", program_options::value<string>(), "Port to listen for conections to be transported")
    ("indoorAddress", program_options::value<string>(), "Address to listen for conections to be transported")
  ;
  
  program_options::variables_map read_opts;
  
  // Reading opts, in precedence the ones from arguments
  
  program_options::store(program_options::parse_command_line(argc, argv, opts), read_opts);
  
  ifstream configFile(CONFIG_FILE_NAMEPATH);
  if(configFile)
    program_options::store(program_options::parse_config_file(configFile, opts), read_opts);
  
  // Count check
  
  if (read_opts.size()!=10)
  {
    cerr << "Some option not specified!" << endl;
    opts.print(cout);
    return false;
  }
  
  
  // Port range
  localPortLow   = read_opts["localPortLow"].as<unsigned short>();
  localPortHigh  = read_opts["localPortHigh"].as<unsigned short>();
  remotePortLow  = read_opts["remotePortLow"].as<unsigned short>();
  remotePortHigh = read_opts["remotePortHigh"].as<unsigned short>();
  
  // Adresses
  
  tcp::resolver resolver(ioService);
  error_code e;
  
  // Mine external
  tcp::resolver::query myQuery(read_opts["myAddress"].as<string>(),read_opts["myPort"].as<string>());
  tcp::resolver::iterator me = resolver.resolve(myQuery,e);
  if(e || tcp::resolver::iterator() == me)
  {
    cerr << "unknown host/port: " << read_opts["myAddress"].as<string>() << ":" << read_opts["myPort"].as<string>() << endl;
    return false;
  }
  my_address = *me;
  
  // Remote
  tcp::resolver::query peerQuery(read_opts["peerAddress"].as<string>(),read_opts["peerPort"].as<string>());
  peer_address = resolver.resolve(peerQuery,e);
  if(e || tcp::resolver::iterator() == peer_address)
  {
    cerr << "unknown host/port: " << read_opts["peerAddress"].as<string>() << ":" << read_opts["peerPort"].as<string>() << endl;
    return false;
  }
  
  // Mine internal
  tcp::resolver::query indoorQuery(read_opts["indoorAddress"].as<string>(),read_opts["indoorPort"].as<string>());
  tcp::resolver::iterator indoor = resolver.resolve(indoorQuery,e);
  if(e || tcp::resolver::iterator() == indoor)
  {
    cerr << "unknown host/port: " << read_opts["indoorAddress"].as<string>() << ":" << read_opts["indoorPort"].as<string>() << endl;
    return false;
  }
  indoorEndpoint = *indoor;
  
  
  // Port range validity check
  if(localPortLow > localPortHigh || remotePortLow > remotePortHigh)
  {
    cerr << "Inverted limits in some port range" << endl;
    return false;
  }
  
  if(localPortLow > localPortHigh || remotePortLow > remotePortHigh)
  {
    cerr << "Inverted limits in some port range" << endl;
    return false;
  }
  
  if(localPortLow>=remotePortLow? localPortLow <= remotePortHigh : remotePortLow <= localPortHigh)
  {
     cerr << "Port ranges overlap" << endl;
     return false;
  }
  
  return true;
}
