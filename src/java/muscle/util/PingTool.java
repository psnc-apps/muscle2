/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package muscle.util;

import java.net.InetAddress;
import java.net.Socket;


/**
measures the duration to open a socket on given host and port
@author Jan Hegewald
*/
public class PingTool
{
	//
	public static String ping(InetAddress host, int port) throws java.io.IOException
	{
		long startTime = System.nanoTime();
		Socket socket = new Socket (host, port);
		long duration = System.nanoTime() - startTime;
		socket.close();

		return "time <" + duration +">ns port <" +port+ "> host_ip <"+host.getHostAddress()+"> host_name <"+host.getHostName()+">";	
	}


}
