/*
 * 
 */
package muscle.client.instance;

import java.io.Serializable;
import muscle.core.ConduitExitController;
import muscle.util.concurrency.Disposable;

/**
 *
 * @author jborgdo1
 */
public interface ConduitExitControllerImpl<T extends Serializable> extends ConduitExitController<T>, Disposable {
	public void start();
}
