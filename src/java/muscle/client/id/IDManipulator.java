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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package muscle.client.id;

import muscle.id.IDType;
import muscle.id.Identifier;
import muscle.id.Location;

/**
 *
 * @author Joris Borgdorff
 */
public interface IDManipulator {
	public void propagate(Identifier id);
	public boolean register(Identifier id, Location loc);
	public void search(Identifier id);
	public boolean willActivate(Identifier id);
	public Identifier create(String name, IDType idType);
	public Location getLocation();
	public Location getManagerLocation();
	public void delete(Identifier id);
	public void deletePlatform();
}
