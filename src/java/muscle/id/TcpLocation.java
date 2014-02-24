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

package muscle.id;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import muscle.util.FileTool;
import muscle.util.JVM;

/**
 * The TCP/IP location of a MUSCLE component, including a local temporary directory that results are written to.
 * @author Joris Borgdorff
 */
public class TcpLocation implements Location {
	private static final long serialVersionUID = 1L;
	private final InetAddress addr;
	private final int port;
	private final String tmpDir;
	
	public TcpLocation(InetAddress addr, int port, String tmpDir) {
		this.addr = addr;
		this.port = port;
		this.tmpDir = tmpDir;
	}

	public TcpLocation(InetSocketAddress localSocketAddress, String tmpDir) {
		this(localSocketAddress.getAddress(), localSocketAddress.getPort(), tmpDir);
	}
	
	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(addr.getHostAddress(), port);
	}
	
	public InetAddress getAddress() {
		return addr;
	}

	public int getPort() {
		return port;
	}
	
	public String getTmpDir() {
		return this.tmpDir;
	}
	
	/** Create a symlink in the current temporary directory to the temporary directory of this location. */
	public void createSymlink(String name, TcpLocation local) {
		if (!tmpDir.equals(local.tmpDir) && !tmpDir.isEmpty()) {
			FileTool.createSymlink(JVM.ONLY.tmpFile(name), new File("../" + tmpDir));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !this.getClass().equals(o.getClass())) return false;
		
		return addr.equals(((TcpLocation)o).addr) && port == ((TcpLocation)o).port && tmpDir.equals(((TcpLocation)o).tmpDir);
	}
	
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + this.addr.hashCode();
		hash = 59 * hash + this.port;
		hash = 59 * hash + this.tmpDir.hashCode();
		return hash;
	}
	
	@Override
	public String toString() {
		return "TcpLocation[" + this.addr + ":" + this.port + " <" + this.tmpDir + ">]";
	}
}
