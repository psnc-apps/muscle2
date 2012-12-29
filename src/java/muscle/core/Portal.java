package muscle.core;

import muscle.core.model.Timestamp;
import muscle.id.Identifiable;
import muscle.id.PortalID;
import muscle.util.concurrency.Disposable;

/**
 *
 * @author jborgdo1
 */
public interface Portal extends Identifiable<PortalID>, Disposable {
	public String getLocalName();
	public Timestamp getSITime();
	public void resetTime(Timestamp time);
}
