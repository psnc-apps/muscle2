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

/** Name for the file with options */
#define CONFIG_FILE_NAMEPATH "config.cfg"

/* defined in options.cpp */

extern short localPortLow, localPortHigh;     ///< Local port range
extern short remotePortLow, remotePortHigh;   ///< Remote port range
extern tcp::endpoint my_address;              ///< External address for this proxy
extern tcp::resolver::iterator peer_address;  ///< External address for peer proxy
extern tcp::endpoint indoorEndpoint;          ///< Internal address for this proxy

/* defined in main.cpp */

extern asio::io_service ioService;

/**
 * Loads options from file and argv
 * 
 * Returns if all aotions are provided and valid
 */
bool loadOptions(int argc, char **argv);

#endif // OPTIONS_H
