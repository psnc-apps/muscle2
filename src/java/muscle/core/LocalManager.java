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
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.conduit.communication.DataConnectionHandler;
import muscle.core.conduit.communication.TcpPortFactoryImpl;
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
	private TcpIDManipulator idManipulator;
	private PortFactory factory;
	
	public static void main(String[] args) {
		try {
			LocalManagerOptions opts = new LocalManagerOptions(args);
			instance = new LocalManager(opts);
			instance.init();
			ConnectionScheme.getInstance(instance);
			instance.start();
		} catch (IOException ex) {
			Logger.getLogger(LocalManager.class.getName()).log(Level.SEVERE, "Could not start listening for data connections.", ex);
		}
	}
	
	private LocalManager(LocalManagerOptions opts) {
		this.opts = opts;
		controllers = new ArrayList<InstanceController>(opts.getAgents().size());
		res = null;
		connectionHandler = null;
		factory = null;
		idManipulator = null;
	}
	
	private void init() throws IOException {
		SocketFactory sf = new LocalSocketFactory();
		
		// Local address, accepting data connections
		InetSocketAddress socketAddr = opts.getLocalSocketAddress();
		ServerSocket ss = sf.createServerSocket(socketAddr.getPort(), 1000, socketAddr.getAddress());
		TcpLocation loc = new TcpLocation(new InetSocketAddress(ss.getInetAddress(), ss.getLocalPort()));
		connectionHandler = new DataConnectionHandler(ss, this);
		
		// Create new conduit exits/entrances using this location.
		factory = new TcpPortFactoryImpl(this, sf, connectionHandler);
		
		// Create a local resolver
		idManipulator = new TcpIDManipulator(sf, opts.getManagerSocketAddress(), loc);
		res = new SimpleDelegatingResolver(idManipulator);
		idManipulator.setResolver(res);
		
		// Initialize the InstanceControllers
		for (InstanceClass name : opts.getAgents()) {
			Identifier id = res.getIdentifier(name.getName(), IDType.instance);
			ThreadedInstanceController tc = new ThreadedInstanceController(id, name.getInstanceClass(), this, this, new Object[0], factory);
			controllers.add(tc);
		}
	}
	
	private void start() {
		Runtime.getRuntime().addShutdownHook(new DisposeOfControllersHook());
		
		// Start listening for connections
		connectionHandler.start();
		
		// Start all instances but the first in a new thread
		for (int i = 1; i < controllers.size(); i++) {
			new Thread(controllers.get(i), controllers.get(i).getLocalName()).start();
		}
		// Run the first instance in the current thread
		controllers.get(0).run();
	}
	
	@Override
	public Resolver getResolver() {
		return this.res;
	}
	
	@Override
	public void isFinished(InstanceController ic) {
		logger.log(Level.FINE, "Instance {0} is no longer running.", ic.getLocalName());
		synchronized (controllers) {
			controllers.remove(ic);
		
			if (controllers.isEmpty()) {
				logger.log(Level.INFO, "All local submodels have finished; exiting.");
				if (factory != null) factory.dispose();
				if (idManipulator != null) idManipulator.dispose();
				if (connectionHandler != null) connectionHandler.dispose();
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}
	
	private static LocalManager instance;

	public static LocalManager getInstance() {
		return instance;
	}
	
	private class DisposeOfControllersHook extends Thread {
		public void run() {
			if (!controllers.isEmpty()) {
				System.out.println();
				System.out.println("MUSCLE is locally shutting down; deregistering local submodels");
			}
			while (!controllers.isEmpty()) {
				InstanceController ic = null;
				synchronized (controllers) {
					if (!controllers.isEmpty()) {
						ic = controllers.remove(0);
					}
				}
				if (ic != null) ic.dispose();
			}
		}
	}
}
