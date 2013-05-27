/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
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
/*
 * 
 */

package muscle.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalSocketFactory extends SocketFactory {
	@Override
	public Socket createSocket() {
		return new Socket();
	}

	@Override
	public ServerSocket createServerSocket(int port, int backlog, InetAddress addr) throws IOException {
		if (port == 0) {
			return createServerSocketInRange(backlog, addr);
		} else {
			return new ServerSocket(port, backlog, addr);
		}
	}
}
