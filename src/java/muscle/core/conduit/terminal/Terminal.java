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
 *
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
	
	protected File getLocalFile(String infix) {
		boolean relative = hasProperty("relative") && getBooleanProperty("relative");
		
		String suffix;
		if (infix == null) {
			suffix = hasProperty("suffix") ? "." + getProperty("suffix") : "";
		} else {
			suffix = "." + infix + "." + (hasProperty("suffix") ? getProperty("suffix") : "dat");
		}
		File output;
		if (relative) {
			output = new File(this.getTmpPath(), getProperty("file") + suffix);
		} else {
			output = new File(getPathProperty("file"), suffix);
		}
		return output;
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
		this.name = id.getOwnerID().getName();
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
