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
package muscle.id;

import java.io.Serializable;

/**
 * An identifier is a general way to identify a MUSCLE component. It always has a name and type determined by the MUSCLE component itself.
 * It caches data about whether it is located somewhere in the current MUSCLE execution, and if it is available to receive messages.
 * If it has been located, it is resolved.
 * @author jborgdo1
 */
public interface Identifier extends Comparable<Identifier>, Serializable {
	/** Get the name of the Identifier. This is not valid as a complete identification. */
	public String getName();
	/** The type of identifier. */
	public IDType getType();
	/** A string that is sufficient to apply the equality rules of identifiers. */
	public String getFullName();
	/** Whether the location of the identifier is known. */
	public boolean isResolved();
	/** Whether the MUSCLE component that the Identifier identifies is available to receive messages. */
	public boolean isAvailable();
	/** Sets the availability of the MUSCLE component. */
	public void setAvailable(boolean available);
	/** Whether the location of the identifier can be known. Results in false if the MUSCLE component has already exited. */
	public boolean canBeResolved();
	/** Signal that the MUSCLE component will never receive a location. If it already has a location, this will be unset. */
	public void willNotBeResolved();
	/** Remove the location from the component. */
	public void unResolve();
	/** Use the same information to resolve the Identifier as the given Identifier. */
	public void resolveLike(Identifier other);
	/** Get the location of the MUSCLE component that the Identifier identifies. */
	public Location getLocation();
	/** Whether this Identifier identifies given MUSCLE component. */
	public boolean identifies(Identifiable ident);
}
