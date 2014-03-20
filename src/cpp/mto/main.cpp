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
#define USE_MPWPATH 1

#include "muscle2/util/logger.hpp"
#include "muscle2/util/exception.hpp"

#include "constants.hpp"
#include "manager/localmto.h"
#include "muscle2/util/csocket.h"
#if USE_MPWPATH == 1
#include "net/MPWPathSocket.h"
#else
#include "net/mpsocket.h"
#endif

#include <iostream>
#include <map>
#include <set>
#include <algorithm>
#include <cstdlib>

#include <unistd.h>
#include <signal.h>

using namespace std;
using namespace muscle;
using namespace muscle::net;
using namespace muscle::util;

// // // // //           Variables           // // // // //
async_service *asyncService;
LocalMto *localMto;
volatile bool receivedSignal = false;

// Reaction on signal - currently QUIT, INT, and TERM
void signalReceived(int signum)
{
    if (signum == SIGQUIT)
    {
        localMto->printDiagnostics();
        asyncService->printDiagnostics();
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
			asyncService->done();
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
        
        if (opts.getDaemonize())
        {
            logger::info("Daemonizing...");
            daemon(0,1);
        }
        
        if (mtoConfigs.find(myName) == mtoConfigs.end()){
            logger::severe("The name of this MTO (%s) could not be found in the topology file", myName.c_str());
            return 1;
        }

        endpoint& externalAddress = mtoConfigs[myName];
		
		uint16_t extport = externalAddress.port;
		uint16_t intport = opts.getInternalEndpoint().port;

		if (opts.useMPWide && extport <= intport && extport + MAX_EXTERNAL_WAITING > intport) {
			logger::severe("Topology port %hu (in mto-topology.cfg), using %d MPWide streams, conflicts with internal port %hu (in mto-config.cfg)", extport, MAX_EXTERNAL_WAITING, intport);
			return 1;
		}
		if (!opts.useMPWide && extport == intport) {
			logger::severe("Topology port %hu (in mto-topology.cfg) must differ from internal port (in mto-config.cfg)", extport);
			return 1;
		}

        if (extport) {
            try {
                externalAddress.resolve();
            } catch (muscle_exception& ex) {
                logger::severe("Cannot resolve MTO external address (%s)", externalAddress.str().c_str());
                return 1;
            }
        }

		const int numPeers = int(mtoConfigs.size() > 1 ? mtoConfigs.size() - 1 : 1);
		asyncService = new async_service(size_t(6*1024*1024)*numPeers, numPeers*6);
        SocketFactory *intSockFactory = new CSocketFactory(asyncService);
        SocketFactory *extSockFactory; 
        if (opts.useMPWide) {
#if USE_MPWPATH == 1
			extSockFactory = new MPWPathSocketFactory(asyncService, 4);
#else
            extSockFactory = new MPSocketFactory(asyncService);
#endif
		} else {
            extSockFactory = new CSocketFactory(asyncService);
		}
        
        localMto = new LocalMto(opts, asyncService, intSockFactory, extSockFactory, externalAddress);
        
        if(externalAddress.port)
            localMto->startListeningForPeers();
        else
            logger::info("No external port provided, not starting external acceptor");

        localMto->startConnectingToPeers(mtoConfigs);
        localMto->startListeningForClients();
        
        signal(SIGINT, signalReceived);
        signal(SIGTERM, signalReceived);
        signal(SIGQUIT, signalReceived);
        
        asyncService->run();
        delete localMto;
		delete asyncService;
		
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
