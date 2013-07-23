#include "options.hpp"

#include "../../muscle2/muscle_types.h"
#include "../../muscle2/logger.hpp"

#include <iostream>
#include <cstdlib>

using namespace std;
using namespace muscle;

Options::Options(int argc, char **argv)
{
    setOptions(opts);
    try {
        load(argc, argv);
    }
    catch (const muscle_exception& ex)
    {
        cout << "Could not load options: " << ex.what() << endl;
        opts.print();
        throw 1;
    }
}

void Options::setOptions(option_parser& opts)
{
    opts.add("config", "Location of the MTO local configuration file (default: $MUSCLE_HOME/etc/mto-config.cfg)");
    opts.add("topology", "Location of the MTO global topology configuration file (default: $MUSCLE_HOME/etc/mto-topology.cfg)");
    
    opts.add("myName", "Name of the MTO, specified in the topology file");
    
    opts.add("localPortLow", "Low limit of the local port range");
    opts.add("localPortHigh", "High limit of the local port range");
    opts.add("TCPBufSize", "TCP Buffers sizes");
    
    opts.add("internalPort", "Port to listen for connections to be transported");
    opts.add("internalAddress", "Address to listen for connections to be transported");
    opts.add("autoRegister", "Address:Port to be registered automatically (used for accessing QCG-Coordinator)", false);
    
    
    opts.add("debug", "Causes the program NOT to go to background and sets logLevel to TRACE", false);
    opts.add("MPWide", "Use the MPWide library in the MTO backbone", false);
    opts.add("logLevel", "Level for logging (TRACE,DEBUG,INFO,ERROR, default INFO)");
    opts.add("logFile", "Path to the log file (default behavior - logging to standard error)");
    
    opts.add("sockAutoCloseTimeout", "Time in seconds after which idle connection is closed");
}

bool Options::setLog(const char * const path, const string strlevel){
    int level;
    const string up = to_upper_ascii(strlevel);
    
    if(up=="TRACE"){
        level = MUSCLE_LOG_FINEST;
    } else if(up=="DEBUG") {
        level = MUSCLE_LOG_FINE;
    } else if(up=="CONFIG") {
        level = MUSCLE_LOG_CONFIG;
    } else if(up=="INFO") {
        level = MUSCLE_LOG_INFO;
    } else if(up=="WARNING") {
        level = MUSCLE_LOG_WARNING;
    } else if(up=="ERROR") {
        level = MUSCLE_LOG_SEVERE;
    } else {
        stringstream ss(up);
        ss >> level;
        if(!ss.eof())
            return false;
    }
    
    FILE * file;
    if (path) {
        file = fopen(path, "a");
        if(!file)
            throw muscle_exception("Failed to open log file <" + string(path) + ">", 0, true);
    } else {
        file = NULL;
    }
    logger::initialize(NULL, file, (muscle_loglevel_t)level, (muscle_loglevel_t)level, true);
    logger::config("Log level %s", up.c_str());

    return true;
}

void Options::print()
{
    opts.print();
}

bool Options::load(int argc, char **argv)
{
    opts.load(argc, argv);
    const char * const c_muscle_home = getenv("MUSCLE_HOME");
    if (c_muscle_home == NULL) {
        logger::severe("Environment variable MUSCLE_HOME not set. Can not load configuration.");
        return false;
    }
    string muscle_home = string(c_muscle_home);
    
    // Locate config
    string configFilePath = opts.get<string>("config", muscle_home + OPTIONS_CONFIG_FILE_NAMEPATH);
    
    // Complement with options with config file
    if (opts.load(configFilePath))
        logger::config("Config file '%s'", configFilePath.c_str());
    else
        logger::severe("Could not open config file '%s'", configFilePath.c_str());

    // Daemon
    if(opts.has("debug"))
    {
        string logLevel = opts.get<string>("logLevel", "TRACE");
        if (!setLog((const char *)0, logLevel))
            return false;
        logger::config("Will not daemonize. Debug mode");
        daemonize = false;
    }
    else
    {
        string logFile = opts.get<string>("logFile", muscle_home + OPTIONS_DEFAULT_LOG_FILE);
        string logLevel = opts.get<string>("logLevel", "CONFIG");

        if (!setLog(logFile.c_str(), logLevel))
            return false;
        
        daemonize = true;
    }

    // Locate topology
    topologyFilePath = opts.get<string>("topology", muscle_home + OPTIONS_TOPOLOGY_FILE_NAMEPATH);

    logger::config("Using topology file '%s'", topologyFilePath.c_str());
    
    // Port range
    localPortLow   = opts.forceGet<uint16_t>("localPortLow");
    localPortHigh  = opts.forceGet<uint16_t>("localPortHigh");
    
    logger::config("My port range: %hu - %hu", localPortLow, localPortHigh);
    
    // Name
    myName = opts.forceGet<string>("myName");
    
    logger::config("My name in topology file: %s", myName.c_str());
    internalEndpoint = endpoint(
                            opts.forceGet<string>("internalAddress"),
                            opts.forceGet<uint16_t>("internalPort"));
    internalEndpoint.resolve();

    logger::config("My internal address: %s", internalEndpoint.str().c_str());
    
    // Port range validity check
    if(localPortLow > localPortHigh)
    {
        logger::severe("Invalid port range");
        return false;
    }
    
    // Other options
    long timeoutSec = opts.get<long>("sockAutoCloseTimeout", 10l);
    sockAutoCloseTimeout = duration(timeoutSec, 0);
    logger::fine("Auto close timeout: %s", sockAutoCloseTimeout.str().c_str());
    
    useMPWide = opts.has("MPWide");
    if (useMPWide)
        logger::config("Using MPWide");
    
    tcpBufSize = opts.get<int>("TCPBufSize", 0);
    if (tcpBufSize)
        logger::config("Using custom TCP Buffer sizes: %d", tcpBufSize);
    
    return true;
}
