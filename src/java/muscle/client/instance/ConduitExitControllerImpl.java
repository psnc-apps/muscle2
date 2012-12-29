/*
 * 
 */
package muscle.client.instance;

import java.io.Serializable;
import muscle.core.ConduitExitController;

/**
 *
 * @author jborgdo1
 */
public interface ConduitExitControllerImpl<T extends Serializable> extends ConduitExitController<T> {
	public void start();
}
