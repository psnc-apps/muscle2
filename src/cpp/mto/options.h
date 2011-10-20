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

/* defined in options.cpp */

extern unsigned short localPortLow, localPortHigh; ///< Local port range
extern tcp::endpoint internalEndpoint;             ///< Address and port for listening to clients
extern string myName;                              ///< Name as in config file
extern bool daemonize;                             ///< If the MTO should go to background

extern string topologyFilePath;                    ///< Location of the topology

/* defined in main.cpp */

extern asio::io_service ioService;

/**
 * Loads options from file and argv
 * 
 * Returns if all options are provided and valid
 */
bool loadOptions(int argc, char **argv);

#endif // OPTIONS_H
