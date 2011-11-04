#ifndef OPTIONS_H
#define OPTIONS_H

#include <iostream>
#include <fstream>
#include <boost/asio.hpp>
#include <boost/program_options.hpp>

using namespace std;
using namespace boost;
using namespace boost::asio::ip;
using namespace boost::system;

/** Default name for the file with options */
#define CONFIG_FILE_NAMEPATH "mto-config.cfg"

/** Default name for the file with topology */
#define TOPOLOGY_FILE_NAMEPATH "mto-topology.cfg"

/* defined in main.cpp */
extern asio::io_service ioService;


class Options
{
private:
  unsigned short localPortLow, localPortHigh; ///< Local port range
  tcp::endpoint internalEndpoint;             ///< Address and port for listening to clients
  string myName;                              ///< Name as in config file
  bool daemonize;                             ///< If the MTO should go to background

  string topologyFilePath;                    ///< Location of the topology
  
  static Options * instance;
  
  Options() : daemonize(false){}
  
public:
  static Options & getInstance(){if(instance) return *instance; instance = new Options; return *instance;}
  
  /**
   * Loads options from file and argv
   * 
   * Returns if all options are provided and valid
   */
  bool load(int argc, char **argv);

  /* Getters */
  
  unsigned short getLocalPortLow() const {return localPortLow;}
  unsigned short getLocalPortHigh() const {return localPortHigh;}
  tcp::endpoint getInternalEndpoint() const {return internalEndpoint;}
  string getMyName() const {return myName;}
  bool getDaemonize() const {return daemonize;}
  string getTopologyFilePath() const {return topologyFilePath;}
  
private:
  bool setLogFile(string path);
  bool setLogLvL(string f);
  bool setLogMsgType(string x);
};

#endif // OPTIONS_H
