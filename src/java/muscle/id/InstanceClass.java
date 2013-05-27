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

/**
 *
 * @author Joris Borgdorff
 */
public class InstanceClass implements Identifiable {
	private final Class<?> clazz;
	private final String name;
	private Identifier id;
	
	public InstanceClass(String name, Class<?> clazz) {
		if (name == null) {
			throw new IllegalArgumentException("Instance name may not be null");
		}
		if (clazz == null) {
			throw new IllegalArgumentException("Instance class may not be null");
		}
		this.name = name;
		this.clazz = clazz;
	}
	
	public void setIdentifier(Identifier id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Class<?> getInstanceClass() {
		return clazz;
	}

	@Override
	public Identifier getIdentifier() {
		return id;
	}
	
	public String toString() {
		return "Description[" + this.name + "]";
	}
}
