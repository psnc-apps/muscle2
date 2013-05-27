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

import muscle.core.kernel.InstanceController;
import muscle.util.concurrency.Disposable;

/**
 *
 * @author jborgdo1
 */
public interface Resolver extends Disposable {
	/** Whether given ID is local to the current execution. */
	public boolean isLocal(Identifier id);
	/** Get an identifier non-blocking, unresolved. */
	public Identifier getIdentifier(String name, IDType type);
	/** removes blocks */
	public void addAvailableIdentifier(Identifier id);
	/** removes blocks */
	public void canNotResolveIdentifier(Identifier id);
	/** removes blocks */
	public void removeIdentifier(String name, IDType type);
	/** blocking */
	public boolean resolveIdentifier(Identifier id) throws InterruptedException;
	/** non-blocking */
	public boolean identifierMayActivate(Identifier id);
	
	/** At current location */
	public boolean register(InstanceController id);
	/** At current location */
	public void makeAvailable(InstanceController id);
	/** At current location */
	public void deregister(InstanceController id);
}
