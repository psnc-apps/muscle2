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
package muscle.client.communication;

import muscle.util.serialization.Protocol;
import muscle.util.serialization.ProtocolSerializer;

/**
 * Values that are sent over the MUSCLE data connections over TCP/IP.
 * Converts between numeric and semantic variables explicitly, with valueOf()
 * and intValue().
 * 
 * @author Joris Borgdorff
 */
public enum TcpDataProtocol implements Protocol {
	OBSERVATION(0), SIGNAL(1), KEEPALIVE(2), FINISHED(3), ERROR(-2), CLOSE(-1), MAGIC_NUMBER(134405);
	public final static ProtocolSerializer<TcpDataProtocol> handler = new ProtocolSerializer<TcpDataProtocol>(TcpDataProtocol.values());
	
	private final int num;
	
	TcpDataProtocol(int n) { num = n; }
	@Override
	public int intValue() { return num; }
}
