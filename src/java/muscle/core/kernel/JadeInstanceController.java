/*
 * 
 */
package muscle.core.kernel;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.*;
import muscle.core.conduit.communication.JadePortFactoryImpl;
import muscle.core.ident.Identifier;

/**
 *
 * @author Joris Borgdorff
 */
public class JadeInstanceController extends MultiDataAgent implements InstanceController, InstanceControllerListener {
	private final static Logger logger = Logger.getLogger(JadeInstanceController.class.getName());
	private transient PortFactory factory;
	private ThreadedInstanceController realController = null;
	
	public <T extends Serializable> void addConduitEntrance(ConduitEntranceController<T> s) {
		realController.addConduitEntrance(s);
	}

	public <T extends Serializable> void addConduitExit(ConduitExitController<T> s) {
		realController.addConduitExit(s);
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

		factory = new JadePortFactoryImpl(Boot.getInstance(), messageProcessor);
		Identifier id = getIdentifier();
		
		try {
			Boot boot = Boot.getInstance();
			Class clazz = Class.forName(boot.getAgentClass(this.getLocalName()));
			realController = new ThreadedInstanceController(id, clazz, this, boot, super.getArguments(), factory);
			realController.setMainController(this);
			new Thread(realController).start();
		} catch (ClassNotFoundException ex) {
			logger.log(Level.SEVERE, "Could not find class " + Boot.getInstance().getAgentClass(this.getLocalName()) + " of instance " + getLocalName() + " in classpath.", ex);
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
