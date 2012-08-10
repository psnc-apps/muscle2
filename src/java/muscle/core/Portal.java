package muscle.core;

import muscle.core.model.Timestamp;

/**
 *
 * @author jborgdo1
 */
public interface Portal {
	public String getLocalName();
	public Timestamp getSITime();
	public void resetTime(Timestamp time);
}
