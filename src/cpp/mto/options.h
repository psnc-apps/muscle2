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

extern unsigned short localPortLow, localPortHigh;     ///< Local port range
extern tcp::endpoint internalEndpoint;                 ///<
extern string myName;                                  ///< 

extern string configFilePath;
extern string topologyFilePath;

/* defined in main.cpp */

extern asio::io_service ioService;

/**
 * Loads options from file and argv
 * 
 * Returns if all aotions are provided and valid
 */
bool loadOptions(int argc, char **argv);

#endif // OPTIONS_H
