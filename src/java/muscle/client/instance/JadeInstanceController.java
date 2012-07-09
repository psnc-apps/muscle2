/*
 * 
 */
package muscle.client.instance;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.JadeBoot;
import muscle.client.communication.JadePortFactoryImpl;
import muscle.client.communication.PortFactory;
import muscle.core.ConduitEntranceController;
import muscle.core.ConduitExitController;
import muscle.core.DataTemplate;
import muscle.id.Identifier;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeInstanceController extends MultiDataAgent implements InstanceController, InstanceControllerListener {
	private final static Logger logger = Logger.getLogger(JadeInstanceController.class.getName());
	private transient PortFactory factory;
	private ThreadedInstanceController realController = null;
	
	
	@Override
	public <T extends Serializable> ConduitEntranceController<T> createConduitEntrance(String portalName, DataTemplate newDataTemplate) {
		return realController.createConduitEntrance(portalName, newDataTemplate);
	}

	@Override
	public <T extends Serializable> ConduitExitController<T> createConduitExit(String portalName, DataTemplate newDataTemplate) {
		return realController.createConduitExit(portalName, newDataTemplate);
	}
	
	@Override
	public void takeDown() {
		super.takeDown();
		
		if (this.realController != null)
			this.realController.dispose();
		
		doDelete();
	}
	
	@Override
	final protected void setup() {
		super.setup();

		factory = new JadePortFactoryImpl(JadeBoot.getInstance(), messageProcessor);
		Identifier id = getIdentifier();
		
		try {
			JadeBoot boot = JadeBoot.getInstance();
			Class clazz = Class.forName(boot.getAgentClass(this.getLocalName()));
			realController = new ThreadedInstanceController(id, clazz, this, boot, super.getArguments(), factory);
			realController.setMainController(this);
			new Thread(realController).start();
		} catch (ClassNotFoundException ex) {
			logger.log(Level.SEVERE, "Could not find class " + JadeBoot.getInstance().getAgentClass(this.getLocalName()) + " of instance " + getLocalName() + " in classpath.", ex);
		}
	}

	public String toString() {
		return getClass().getSimpleName() + "[" + getIdentifier() + "]";
	}

	public void dispose() {
		takeDown();
	}
	
	@Override
	public void isFinished(InstanceController ic) {
		takeDown();
	}

	@Override
	public boolean isExecuting() {
		return this.realController.isExecuting();
	}
}
