/*
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "../muscle2/logger.hpp"
#include "../muscle2/exception.hpp"

#include "constants.hpp"
#include "manager/localmto.h"
#include "net/csocket.h"
#include "net/mpsocket.h"

#include <iostream>
#include <map>
#include <set>
#include <algorithm>
#include <cstdlib>

#include <unistd.h>
#include <signal.h>

using namespace std;
using namespace muscle;

// // // // //           Variables           // // // // //
async_service asyncService;
LocalMto *localMto;
volatile bool receivedSignal = false;

// Reaction on signal - currently QUIT, INT, and TERM
void signalReceived(int signum)
{
    if (signum == SIGQUIT)
    {
        localMto->printDiagnostics();
        asyncService.printDiagnostics();
    }
    else
    {
        const char *s;
        switch (signum) {
            case SIGINT:
                s = "SIGINT";
                break;
            case SIGTERM:
                s = "SIGTERM";
                break;
            default:
                s = "unknown signal(?)";
                break;
        }

		if (!receivedSignal) {
			receivedSignal = true;
			logger::warning("Received %s, exiting...", s);
			asyncService.done();
		} else {
			logger::severe("Received %s again, forcing exit.", s);
			logger::finalize();
			
			exit(0);
		}
    }
}

int main(int argc, char **argv)
{
    try {
        Options opts(argc, argv);
        
        map<string, endpoint> mtoConfigs;

        if(!loadTopology(opts.getTopologyFilePath(), mtoConfigs))
            return 1;
        
        string myName = opts.getMyName();
        
        if(opts.getDaemonize())
        {
            logger::info("Daemonizing...");
            daemon(0,1);
        }
        
        if(mtoConfigs.find(myName) == mtoConfigs.end()){
            logger::severe("The name of this MTO (%s) could not be found in the topology file", myName.c_str());
            return 1;
        }

        endpoint& externalAddress = mtoConfigs[myName];

        if (externalAddress.port)
        {
            try {
                externalAddress.resolve();
            }
            catch (muscle_exception& ex)
            {
                logger::severe("Cannot resolve MTO external address (%s)!", externalAddress.str().c_str());
                return 1;
            }
        }

        SocketFactory *intSockFactory = new CSocketFactory(&asyncService);
        SocketFactory *extSockFactory;
        if (opts.useMPWide)
            extSockFactory = new MPSocketFactory(&asyncService);
        else
            extSockFactory = new CSocketFactory(&asyncService);
        
        localMto = new LocalMto(opts, &asyncService, intSockFactory, extSockFactory, externalAddress);
        
        if(externalAddress.port)
            localMto->startListeningForPeers();
        else
            logger::info("No external port provided, not starting external acceptor");

        localMto->startConnectingToPeers(mtoConfigs);
        localMto->startListeningForClients();
        
        signal(SIGINT, signalReceived);
        signal(SIGTERM, signalReceived);
        signal(SIGQUIT, signalReceived);
        
        asyncService.run();
        delete localMto;
		
        return 0;
    }
    catch (const muscle::muscle_exception& ex)
    {
        cerr << "Exited with exception: " << ex.what() << endl;
        return (ex.error_code ? ex.error_code : 1);
    }
    catch (int i)
    {
        return i;
    }
}
