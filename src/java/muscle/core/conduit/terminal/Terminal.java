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
package muscle.core.conduit.terminal;

import java.io.File;
import muscle.core.Portal;
import muscle.core.kernel.Module;
import muscle.core.model.Timestamp;
import muscle.id.PortalID;
import muscle.util.concurrency.Disposable;

/**
 * A base class for Source and Sink
 * @author jborgdo1
 */
public abstract class Terminal extends Module implements Disposable, Portal {
	private volatile boolean isDone;
	private Timestamp siTime;
	private PortalID portalID;
	
	public Terminal() {
		this.isDone = false;
		this.siTime = new Timestamp(0);
	}
	
	/**
	 * Constructs a filename for the terminal.
	 * It does so by reading the "file" property. If the "relative" option is
	 * set to true, it will open the file relative to the MUSCLE temporary
	 * directory of the Terminal. If "suffix" is set, it will append that
	 * extension. If the parameter infix is not null, and "suffix" is not
	 * set, it will append the extension .dat.
	 *
	 * The filename can thus take the following forms:
	 * file
	 * or
	 * file.suffix
	 * or
	 * file.infix.dat
	 * or
	 * file.infix.suffix
	 * or
	 * tmpdir/file
	 * etc.
	 * @param infix the infix, if any to use between the filename and the suffix.
	 */
	protected File getLocalFile(String infix) {
		String suffix;
		if (infix == null) {
			suffix = hasProperty("suffix") ? "." + getProperty("suffix") : "";
		} else {
			suffix = "." + infix + "." + (hasProperty("suffix") ? getProperty("suffix") : "dat");
		}
		File output;
		if (fileIsRelative()) {
			output = new File(this.getTmpPath(), getProperty("file") + suffix);
		} else {
			output = new File(getProperty("file") + suffix);
		}
		return output;
	}
	
	protected boolean fileIsRelative() {
		return hasProperty("relative") && getBooleanProperty("relative");
	}
	
	@Override
	public Timestamp getSITime() {
		return siTime;
	}
	
	@Override
	public void resetTime(Timestamp time) {
		this.siTime = time;
	}

	public void setIdentifier(PortalID id) {
		this.setLocalName(id.getOwnerID().getName());
		this.portalID = id;
	}
	
	@Override
	public PortalID getIdentifier() {
		return this.portalID;
	}
	
	@Override
	public void dispose() {
		this.isDone = true;
	}
	
	@Override
	public boolean isDisposed() {
		return this.isDone;
	}
}
