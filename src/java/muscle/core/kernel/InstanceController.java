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
package muscle.core.kernel;

import java.io.Serializable;
import java.util.Map;
import muscle.core.ConduitDescription;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.id.Identifiable;
import muscle.util.concurrency.Disposable;
import muscle.util.concurrency.NamedRunnable;

/**
 * @author Joris Borgdorff
 */
public interface InstanceController extends Identifiable, Disposable {
	public NamedRunnable getRunner();
	public String getName();
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(boolean threaded, boolean shared, String portalName, Class<T> newDataTemplate);
	public <T extends Serializable> ConduitExitController<T> createConduitExit(boolean threaded, String portalName, Class<T> newDataTemplate);
	public boolean isExecuting();
	public void fatalException(Throwable ex);
	public Map<String, ConduitDescription> getEntranceDescriptions();
	public Map<String, ConduitDescription> getExitDescriptions();
}
