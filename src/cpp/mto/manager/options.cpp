#include "options.hpp"

#include <iostream>
#include <cstdlib>

using namespace std;

Options::Options(int argc, char **argv)
{
    setOptions(opts);
    try {
        load(argc, argv);
    }
    catch (const muscle::muscle_exception& ex)
    {
        cerr << "Could not load options: " << ex.what() << endl;
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
    opts.add("logMsgTypes", "Allows filtering log messages context (PEER,CONFIG,CLIENT, default: PEER|CONFIG|CLIENT)");
    opts.add("logFile", "Path to the log file (default behavior - logging to standard error)");
    
    opts.add("sockAutoCloseTimeout", "Time in seconds after which idle connection is closed");
}

bool Options::setLogFile(string x){
    FILE * file = fopen(x.c_str(), "a");
    if(!file)
    {
        Logger::error(-1, "Failed to open log file '%s'", x.c_str());
        return false;
    }
    Logger::setLogStream(file);
    return true;
}

bool Options::setLogLvL(string f){
    string up = to_upper_ascii(f);
    if(up=="TRACE"){
        Logger::setLogLevel(Logger::LogLevel_Trace);
    } else if(up=="INFO") {
        Logger::setLogLevel(Logger::LogLevel_Info);
    } else if(up=="DEBUG") {
        Logger::setLogLevel(Logger::LogLevel_Debug);
    } else if(up=="ERROR") {
        Logger::setLogLevel(Logger::LogLevel_Error);
    } else {
        stringstream ss(up);
        int type;
        ss >> type;
        if(!ss.eof())
            return false;
        Logger::setLogLevel(type);
    }
    return true;
}

bool Options::setLogMsgType(string x){
    vector<string> found = split(x, ",|");
    
    int types = 0;
    
    for (vector<string>::iterator it = found.begin(); it != found.end(); it++)
    {
        string up = to_upper_ascii(*it);
        if(up=="PEER"){
            types|=Logger::MsgType_PeerConn;
        } else if(up=="CONFIG") {
            types|=Logger::MsgType_Config;
        } else if(up=="CLIENT") {
            types|=Logger::MsgType_ClientConn;
        } else {
            stringstream ss(up);
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

void Options::print()
{
    opts.print();
}

bool Options::load(int argc, char **argv)
{
    opts.load(argc, argv);
    bool logFileSet = false;
    string muscle_home = string(getenv("MUSCLE_HOME") ? getenv("MUSCLE_HOME") : ".");
    
    // Reading opts, in precedence the ones from arguments
    if(opts.has("logFile"))
    {
        if(!setLogFile(opts.forceGet<string>("logFile")))
            return false;
        logFileSet = true;
    }
    
    string configFilePath;
    
    // Locate config
    configFilePath = opts.get<string>("config",
                        muscle_home + "/etc/" + CONFIG_FILE_NAMEPATH);
    
    // Complement with options with config file
    if (opts.load(configFilePath))
    {
        Logger::info(Logger::MsgType_Config, "Config file '%s'", configFilePath.c_str());
        if(!logFileSet && opts.has("logFile"))
        {
            if(!setLogFile(opts.forceGet<string>("logFile")))
                return false;
            logFileSet = true;
        }
    }
    else
        Logger::error(Logger::MsgType_Config, "Could not open config file '%s'", configFilePath.c_str());

    // Daemon
    if(opts.has("debug"))
    {
        setLogLvL("TRACE");
        Logger::debug(Logger::MsgType_Config, "Will not daemonize. Debug mode");
        daemonize = false;
    }
    else
    {
        daemonize = true;
        if (!logFileSet) /*log file is not set explicitly */
            setLogFile(muscle_home + "/var/log/muscle/mto.log");
    }

    // Locate topology
    topologyFilePath = opts.get<string>("topology",
                        muscle_home + "/etc/" + TOPOLOGY_FILE_NAMEPATH);

    Logger::info(Logger::MsgType_Config, "Using topology file '%s'", topologyFilePath.c_str());
    
    // Port range
    localPortLow   = opts.forceGet<uint16_t>("localPortLow");
    localPortHigh  = opts.forceGet<uint16_t>("localPortHigh");
    
    Logger::info(Logger::MsgType_Config, "My port range: %hu - %hu", localPortLow, localPortHigh);
    
    // Name
    myName = opts.forceGet<string>("myName");
    
    Logger::info(Logger::MsgType_Config, "My name in topology file: %s", myName.c_str());
    internalEndpoint = muscle::endpoint(
                            opts.forceGet<string>("internalAddress"),
                            opts.forceGet<uint16_t>("internalPort"));
    internalEndpoint.resolve();

    Logger::info(Logger::MsgType_Config, "My internal address: %s", internalEndpoint.str().c_str());
    
    // Port range validity check
    if(localPortLow > localPortHigh)
    {
        Logger::error(Logger::MsgType_Config, "Invalid port range");
        return false;
    }
    
    // Logging
    if(opts.has("logMsgTypes"))
        if(!setLogMsgType(opts.forceGet<string>("logMsgTypes")))
            return false;
    
    if(opts.has("logLevel"))
        if(!setLogLvL(opts.forceGet<string>("logLevel")))
            return false;
    
    int l = Logger::getLogLevel();
    Logger::info(-1, "Logging level %d (%s)", l,
                 ( l == Logger::LogLevel_Trace ? "TRACE":
                  ( l == Logger::LogLevel_Debug ? "DEBUG":
                   ( l == Logger::LogLevel_Info ? "INFO":
                    ( l == Logger::LogLevel_Error ? "ERROR":
                     "?")))));
    
    // Other options
    long timeoutSec = opts.get<long>("sockAutoCloseTimeout", 10l);
    sockAutoCloseTimeout = muscle::duration(timeoutSec, 0);
    Logger::debug(Logger::MsgType_Config, "Auto close timeout: %s", sockAutoCloseTimeout.str().c_str());
    
    useMPWide = opts.has("MPWide");
    if (useMPWide)
        Logger::debug(Logger::MsgType_Config, "Using MPWide");
    
    tcpBufSize = opts.get<int>("TCPBufSize", 0);
    if (tcpBufSize)
        Logger::debug(Logger::MsgType_Config, "Using custom TCP Buffer sizes: %d", tcpBufSize);
    
    return true;
}
