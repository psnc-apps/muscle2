/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.DataConnectionHandler;
import muscle.core.conduit.communication.Receiver;
import muscle.core.conduit.communication.TcpPortFactoryImpl;
import muscle.core.conduit.communication.TcpReceiver;
import muscle.core.ident.*;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.core.kernel.ThreadedInstanceController;
import muscle.net.LocalSocketFactory;
import muscle.net.SocketFactory;

/**
 * @author Joris Borgdorff
 */
public class LocalManager implements InstanceControllerListener, ResolverFactory {
	private final List<InstanceController> controllers;
	private final static Logger logger = Logger.getLogger(LocalManager.class.getName());
	private final LocalManagerOptions opts;
	private DataConnectionHandler connectionHandler;
	private SimpleDelegatingResolver res;
	
	public static void main(String[] args) {
		try {
			LocalManagerOptions opts = new LocalManagerOptions(args);
			instance = new LocalManager(opts);
			instance.init();
			ConnectionScheme.getInstance(instance);
			instance.start();
		} catch (IOException ex) {
			Logger.getLogger(LocalManager.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private LocalManager(LocalManagerOptions opts) {
		this.opts = opts;
		controllers = new ArrayList<InstanceController>(opts.getAgents().size());
		res = null;
		connectionHandler = null;
	}
	
	private void init() throws IOException {
		InetSocketAddress socketAddr = opts.getLocalSocketAddress();
		TcpLocation loc = new TcpLocation(socketAddr);
		
		SocketFactory sf = new LocalSocketFactory();
		
		ServerSocket ss = sf.createServerSocket(socketAddr.getPort(), 1000, socketAddr.getAddress());
		connectionHandler = new DataConnectionHandler(ss, this);
		TcpPortFactoryImpl factory = new TcpPortFactoryImpl(this,sf,connectionHandler);
		
		TcpIDManipulator idManipulator = new TcpIDManipulator(sf, opts.getManagerSocketAddress(), loc);
		res = new SimpleDelegatingResolver(idManipulator);
		
		for (InstanceClass name : opts.getAgents()) {
			Identifier id = res.getIdentifier(name.getName(), IDType.instance);
			ThreadedInstanceController tc = new ThreadedInstanceController(id, name.getInstanceClass(), this, this, new Object[0], factory);
			controllers.add(tc);
		}
	}
	
	private void start() {
		connectionHandler.start();
		
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
		logger.log(Level.INFO, "Instance {0} registered as being finished.", ic.getLocalName());
		controllers.remove(ic);
		
		if (controllers.isEmpty() && connectionHandler != null) {
			connectionHandler.dispose();
		}
	}
	
	private static LocalManager instance;

	public static LocalManager getInstance() {
		return instance;
	}
}
