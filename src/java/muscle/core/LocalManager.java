/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import muscle.core.ident.*;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.core.kernel.ThreadedInstanceController;
import muscle.net.CrossSocketFactory;

/**
 * @author Joris Borgdorff
 */
public class LocalManager implements InstanceControllerListener, ResolverFactory {
	private final List<InstanceController> controllers;
	private final static Logger logger = Logger.getLogger(LocalManager.class.getName());
	private final LocalManagerOptions opts;
	private SimpleDelegatingResolver res;
	
	private LocalManager(LocalManagerOptions opts) {
		this.opts = opts;
		controllers = new ArrayList<InstanceController>(opts.getAgents().size());
		res = null;
	}
	
	private void init() {
		Location loc = new TcpLocation(opts.getLocalSocketAddress());
		TcpIDManipulator idManipulator = new TcpIDManipulator(new CrossSocketFactory(), opts.getManagerSocketAddress(), loc);
		res = new SimpleDelegatingResolver(idManipulator);
		
		for (InstanceClass name : opts.getAgents()) {
			Identifier id = res.getIdentifier(name.getName(), IDType.instance);
			ThreadedInstanceController tc = new ThreadedInstanceController(id, name.getInstanceClass(), this, this, new Object[0]);
			controllers.add(tc);
		}
	}
	
	private void start() {
		for (InstanceController inst : controllers) {
			new Thread(inst).start();
		}
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
