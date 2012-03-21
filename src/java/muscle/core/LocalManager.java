/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import muscle.core.conduit.communication.TcpPortFactoryImpl;
import muscle.core.ident.*;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.core.kernel.ThreadedInstanceController;
import muscle.net.LocalSocketFactory;

/**
 * @author Joris Borgdorff
 */
public class LocalManager implements InstanceControllerListener, ResolverFactory {
	private final List<InstanceController> controllers;
	private final static Logger logger = Logger.getLogger(LocalManager.class.getName());
	private final LocalManagerOptions opts;
	private SimpleDelegatingResolver res;
	
	public static void main(String[] args) {
		LocalManagerOptions opts = new LocalManagerOptions(args);
		instance = new LocalManager(opts);
		instance.init();
		ConnectionScheme.getInstance(instance);
		instance.start();
	}
	
	private LocalManager(LocalManagerOptions opts) {
		this.opts = opts;
		controllers = new ArrayList<InstanceController>(opts.getAgents().size());
		res = null;
	}
	
	private void init() {
		TcpLocation loc = new TcpLocation(opts.getLocalSocketAddress());
		
		PortFactory.setImpl(new TcpPortFactoryImpl(new LocalSocketFactory()));
		
		TcpIDManipulator idManipulator = new TcpIDManipulator(new LocalSocketFactory(), opts.getManagerSocketAddress(), loc);
		res = new SimpleDelegatingResolver(idManipulator);
		
		for (InstanceClass name : opts.getAgents()) {
			Identifier id = res.getIdentifier(name.getName(), IDType.instance);
			ThreadedInstanceController tc = new ThreadedInstanceController(id, name.getInstanceClass(), this, this, new Object[0]);
			controllers.add(tc);
		}
	}
	
	private void start() {
		// Run the first controller in the current thread;
		for (int i = 1; i < controllers.size(); i++) {
			new Thread(controllers.get(i)).start();
		}
		controllers.get(0).run();
	}
	
	@Override
	public Resolver getResolver() {
		return this.res;
	}

	@Override
	public void isFinished(InstanceController ic) {
		System.out.println("Instance " + ic.getLocalName() + " registered as being finished.");
	}
	
	private static LocalManager instance;

	public static LocalManager getInstance() {
		return instance;
	}
}
