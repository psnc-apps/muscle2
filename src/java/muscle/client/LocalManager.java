/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client;

import muscle.client.ident.TcpLocation;
import muscle.client.ident.TcpIDManipulator;
import muscle.client.ident.SimpleDelegatingResolver;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.message.DataConnectionHandler;
import muscle.client.communication.TcpPortFactoryImpl;
import muscle.core.ConnectionScheme;
import muscle.client.communication.PortFactory;
import muscle.core.ident.*;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.client.instance.ThreadedInstanceController;
import muscle.net.LocalSocketFactory;
import muscle.net.SocketFactory;

/**
 * @author Joris Borgdorff
 */
public class LocalManager implements InstanceControllerListener, ResolverFactory {
	private final static Logger logger = Logger.getLogger(LocalManager.class.getName());
	private final List<InstanceController> controllers;
	private final LocalManagerOptions opts;
	private DataConnectionHandler connectionHandler;
	private SimpleDelegatingResolver res;
	private TcpIDManipulator idManipulator;
	private PortFactory factory;
	private boolean isDone;
	private DisposeOfControllersHook disposeOfControllersHook;
	private ForcefulQuitHook forcefulQuitHook;
	
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
		isDone = false;
	}
	
	private void init() throws IOException {
		SocketFactory sf = new LocalSocketFactory();
		
		// Local address, accepting data connections
		InetSocketAddress socketAddr = opts.getLocalSocketAddress();
		int port = socketAddr.getPort();
		InetAddress address = socketAddr.getAddress();
		ServerSocket ss = sf.createServerSocket(port, 1000, address);
		
		// Resetting the socket address to the actually used address
		port = ss.getLocalPort();
		logger.log(Level.CONFIG, "Data connection bound to {0}:{1,number,#}", new Object[]{address, port});
		socketAddr = new InetSocketAddress(address, port);
		TcpLocation loc = new TcpLocation(socketAddr);
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
		disposeOfControllersHook = new DisposeOfControllersHook();
		Runtime.getRuntime().addShutdownHook(disposeOfControllersHook);
		forcefulQuitHook = new ForcefulQuitHook();
		Runtime.getRuntime().addShutdownHook(forcefulQuitHook);
		
		
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
			this.tryQuit();
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
				tryQuit();
			}
			forcefulQuitHook.notifyDisposerFinished();
		}
	}
	
	private class ForcefulQuitHook extends Thread {
		boolean finished = false;
		public void run() {
			try {
				this.waitForDisposer();
			} catch (InterruptedException ex) {}
			if (!finished) {
				System.out.println("Submodels not exiting nicely; forcing exit.");
				System.exit(1);
			}
		}
		
		public synchronized void waitForDisposer() throws InterruptedException {
			if (!finished)
				wait(2000);

			// Not waiting more than 15 seconds, and interrupting every second.
			for (int i = 0; !finished && i < 15; i++) {
				forcefulQuitHook.interrupt();
				System.out.print(".");
				wait(1000);
			}
		}
		
		public synchronized void notifyDisposerFinished() {
			finished = true;
			this.notify();
		}
	}
	
	private void tryQuit() {
		synchronized (controllers) {			
			if (controllers.isEmpty()) {
				if (isDone) {
					return;
				}
				isDone = true;

				logger.log(Level.INFO, "All local submodels have finished; exiting.");
				if (factory != null) factory.dispose();
				if (idManipulator != null) idManipulator.dispose();
				if (connectionHandler != null) connectionHandler.dispose();
			}
		}
	}
}
