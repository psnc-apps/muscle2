#include "options.hpp"

#include "muscle2/muscle_types.h"
#include "muscle2/util/logger.hpp"

#include <iostream>
#include <cstdlib>

using namespace std;
using namespace muscle;
using namespace muscle::util;
using namespace muscle::net;


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
    opts.add("help", "Print this dialog", false);
    opts.add("config", "Location of the MTO local configuration file (default: $MUSCLE_HOME/etc/mto-config.cfg)");
    opts.add("topology", "Location of the MTO global topology configuration file (default: $MUSCLE_HOME/etc/mto-topology.cfg)");
    
    opts.add("myName", "Name of the MTO, specified in the topology file");
    
    opts.add("localPortLow", "Low limit of the local port range");
    opts.add("localPortHigh", "High limit of the local port range");
    opts.add("TCPBufSize", "TCP Buffers sizes");
    
    opts.add("internalPort", "Port to listen for connections to be transported");
    opts.add("internalAddress", "Address to listen for connections to be transported (default: *)");
    opts.add("qcgCoordinator", "QCG_Host:Port to be registered automatically (used for accessing QCG-Coordinator)", false);
    
    
    opts.add("debug", "Causes the program NOT to go to background and sets logLevel to TRACE", false);
    opts.add("MPWide", "Use the MPWide library in the MTO backbone", false);
    opts.add("MPWPath", "Use the alternative MPWide Path implementation in the MTO backbone (more stable than MPWide)", false);
    opts.add("threads", "Number of threads per MPWide(Path) send or receive operation (Path default: 4, MPWide default: number of channels)");
    opts.add("channels", "Number of channels in the MPWide(Path) (default: 128)");
	opts.add("noThreadPool", "Do not use thread pool with MPWide Path", false);
    opts.add("logLevel", "Level for logging (TRACE,DEBUG,CONFIG,INFO,WARNING,ERROR, default CONFIG)");
    opts.add("logFile", "Path to the log file (default behavior - logging to standard error)");
    
    opts.add("sockAutoCloseTimeout", "Time in seconds after which idle connection is closed");
}

bool Options::setLog(const char * const path, const string strlevel){
    int level;
    string up = to_upper_ascii(strlevel);
    
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
		logger::warning("Log level '%s' not recognized.", up.c_str());
		up = "CONFIG";
		level = MUSCLE_LOG_CONFIG;
    }
    
    if (path) {
        FILE *file = fopen(path, "a");
        if(!file)
            throw muscle_exception("Failed to open log file '" + string(path) + "'", 0, true);

		logger::config("Using log file '%s'", path);
		logger::info("Will daemonize.");
		logger::initialize(NULL, file, MUSCLE_LOG_OFF, (muscle_loglevel_t)level, true);
    } else {
		logger::initialize(NULL, NULL, (muscle_loglevel_t)level, MUSCLE_LOG_OFF, true);
    }
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
	
	if (opts.has("help")) {
		opts.print();
		exit(EXIT_SUCCESS);
	}
	
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
    else {
        logger::severe("Could not open config file '%s'", configFilePath.c_str());
		return false;
	}

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
                            opts.get<string>("internalAddress", "*"),
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
    
	useMPWPath = opts.has("MPWPath");
	if (useMPWPath) {
		useThreadPool = !opts.has("noThreadPool");
		num_threads = opts.get<int>("threads", 4);
		num_channels = opts.get<int>("channels", 128);
		const char *tpstr = useThreadPool ? "with" : "without";
        logger::config("Using MPWide Path with %d threads, %d channels, %s thread pool", num_threads, num_channels, tpstr);
	} else {
		useMPWide = opts.has("MPWide");
		if (useMPWide) {
			num_channels = opts.get<int>("channels", 128);
			logger::config("Using MPWide with %d channels", num_channels);
		}
	}

    if (opts.has("qcgCoordinator")) {
        string qcgAddr = opts.get<string>("qcgCoordinator");
        vector<string> qcgSplit = split(qcgAddr, ":");
        if (qcgSplit.size() != 2) {
            logger::severe("qcgEndpoint is not configured as HOST:PORT");
            return false;
        }
        uint16_t qcgPort;
        stringstream ss(qcgSplit[1]);
        ss >> qcgPort;
        
        qcgEndpoint = endpoint(qcgSplit[0], qcgPort);
        qcgEndpoint.resolve();
    }

    tcpBufSize = opts.get<int>("TCPBufSize", 0);
    if (tcpBufSize)
        logger::config("Using custom TCP Buffer sizes: %d", tcpBufSize);
    
    return true;
}
