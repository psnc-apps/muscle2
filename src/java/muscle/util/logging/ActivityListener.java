package muscle.util.logging;

import muscle.id.Identifier;
import muscle.util.concurrency.Disposable;

/**
 *
 * @author joris
 */
public interface ActivityListener extends Disposable {
	public void init();
	public void activity(ActivityProtocol protocol, Identifier id);
	public void activity(ActivityProtocol protocol, String id);
}
