/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client;

import eu.mapperproject.jmml.util.FastArrayList;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.PortFactory;
import muscle.client.communication.TcpPortFactoryImpl;
import muscle.client.communication.message.DataConnectionHandler;
import muscle.client.communication.message.LocalDataHandler;
import muscle.client.id.DelegatingResolver;
import muscle.client.id.TcpIDManipulator;
import muscle.client.instance.ThreadedInstanceController;
import muscle.core.ConnectionScheme;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.id.*;
import muscle.net.ConnectionHandlerListener;
import muscle.net.CrossSocketFactory;
import muscle.net.SocketFactory;
import muscle.util.JVM;

/**
 * @author Joris Borgdorff
 */
public class LocalManager implements InstanceControllerListener, ResolverFactory, ConnectionHandlerListener {
	private final static Logger logger = Logger.getLogger(LocalManager.class.getName());
	private final List<InstanceController> controllers;
	private final List<Thread> controllerThreads;
	private final LocalManagerOptions opts;
	private DataConnectionHandler tcpConnectionHandler;
	private LocalDataHandler localConnectionHandler;
	private DelegatingResolver res;
	private TcpIDManipulator idManipulator;
	private PortFactory factory;
	private boolean isDone;
	private DisposeOfControllersHook disposeOfControllersHook;
	private ForcefulQuitHook forcefulQuitHook;
	private volatile boolean isShutdown;
	
	public static void main(String[] args) {
		try {
			LocalManagerOptions opts = new LocalManagerOptions(args);
			instance = new LocalManager(opts);
			instance.init();
			ConnectionScheme.getInstance(instance);
			instance.start();
		} catch (InterruptedException ex) {
			Logger.getLogger(LocalManager.class.getName()).log(Level.SEVERE, "Simulation was interrupted. Aborting.", ex);
			System.exit(121);
		} catch (IOException ex) {
			Logger.getLogger(LocalManager.class.getName()).log(Level.SEVERE, "Could not start listening for data connections. Aborting.", ex);
			System.exit(120);
		}
	}
	
	private LocalManager(LocalManagerOptions opts) {
		this.opts = opts;
		controllers = new FastArrayList<InstanceController>(opts.getAgents().size());
		controllerThreads = new FastArrayList<Thread>(opts.getAgents().size());
		res = null;
		tcpConnectionHandler = null;
		localConnectionHandler = null;
		factory = null;
		idManipulator = null;
		isDone = false;
		isShutdown = false;
	}
	
	private void init() throws IOException {
		SocketFactory sf = new CrossSocketFactory();
		
		// Local address, accepting data connections
		InetSocketAddress socketAddr = opts.getLocalSocketAddress();
		if (socketAddr == null) {
			throw new IOException("Could not construct local socket");
		}
		int port = socketAddr.getPort();
		InetAddress address = socketAddr.getAddress();
		ServerSocket ss = sf.createServerSocket(port, 1000, address);
		
		// Resetting the socket address to the actually used address
		port = ss.getLocalPort();
		logger.log(Level.CONFIG, "Data connection bound to {0}:{1,number,#}", new Object[]{address, port});
		socketAddr = new InetSocketAddress(address, port);
		String dir = JVM.ONLY.getTmpDirName();
		TcpLocation loc = new TcpLocation(socketAddr, dir);
		tcpConnectionHandler = new DataConnectionHandler(ss, this);
		localConnectionHandler = new LocalDataHandler();
		
		// Create new conduit exits/entrances using this location.
		factory = new TcpPortFactoryImpl(this, sf, tcpConnectionHandler, localConnectionHandler);
		
		// Create a local resolver
		idManipulator = new TcpIDManipulator(sf, opts.getManagerSocketAddress(), loc);
		res = new DelegatingResolver(idManipulator, opts.getAgentNames());
		idManipulator.setResolver(res);
		
		((TcpLocation)idManipulator.getManagerLocation()).createSymlink("manager", loc);
		
		// Initialize the InstanceControllers
		for (InstanceClass name : opts.getAgents()) {
			Identifier id = res.getIdentifier(name.getName(), IDType.instance);
			ThreadedInstanceController tc = new ThreadedInstanceController(id, name.getInstanceClass(), this, this, new Object[0], factory);
			controllers.add(tc);
		}
	}
	
	private void start() throws InterruptedException {
		disposeOfControllersHook = new DisposeOfControllersHook();
		Runtime.getRuntime().addShutdownHook(disposeOfControllersHook);
		forcefulQuitHook = new ForcefulQuitHook();
		Runtime.getRuntime().addShutdownHook(forcefulQuitHook);
		
		
		// Start listening for connections
		tcpConnectionHandler.start();
		localConnectionHandler.start();
		factory.start();
		
		int size = controllers.size();
		
		try {
			synchronized (controllers) {
				// Start all instances but the first in a new thread
				for (int i = 1; i < size; i++) {
					if (isShutdown) {
						return;
					}

					InstanceController ic = controllers.get(i);
					Thread t = new Thread(ic, ic.getLocalName());
					controllerThreads.add(t);
					t.start();
				}
				// Run the first instance in the current thread
				controllerThreads.add(Thread.currentThread());
				controllers.get(0).run();
			}
		} catch (OutOfMemoryError er) {
			logger.log(Level.SEVERE, "Out of memory: too many submodels; try decreasing thread memory (e.g. -D -Xss512k)", er);
			this.shutdown(2);
		}
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
	
	public void shutdown(int code) {
		if (isShutdown) {
			return;
		}
		isShutdown = true;
		// Shutdown may be called during startup, but isDone can not be.
		synchronized (controllers) {
			if (isDone) {
				return;
			}
		}
		logger.severe("Shutting down simulation due to an error");
		System.exit(code);
	}

	@Override
	public void fatalException(Throwable ex) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	private class DisposeOfControllersHook extends Thread {
		public DisposeOfControllersHook() {
			super("DisposeOfControllersHook");
		}
		public void run() {
			synchronized (controllers) {
				if (controllers.isEmpty()) {
					forcefulQuitHook.notifyDisposerFinished();
					return;
				} else {
					System.out.println();
					System.out.println("MUSCLE is locally shutting down; deregistering local submodels");
				}
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
		public ForcefulQuitHook() {
			super("ForcefulQuitHook");
		}
		public void run() {
			try {
				this.waitForDisposer();
			} catch (InterruptedException ex) {}
			
			if (!finished) {
				System.out.println("Submodels not exiting nicely; forcing exit.");
				Runtime.getRuntime().halt(1);
			}
		}
		
		public void waitForDisposer() throws InterruptedException {
			synchronized (this) {
				if (!finished)
					wait(2000);
			}

			// Not waiting more than 15 seconds, and interrupting every second.
			for (int i = 0; i < 15; i++) {
				synchronized (this) {
					if (finished) {
						break;
					}
				}
				disposeOfControllersHook.interrupt();
				synchronized(controllers) {
					for (Thread t : controllerThreads) {
						t.interrupt();
					}
				}
				synchronized (this) {
					System.out.print(".");
					wait(1000);			
				}
			}
			System.out.println();
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
				if (factory != null) {
					factory.dispose();
				}
				if (idManipulator != null) {
					idManipulator.dispose();
				}
				if (tcpConnectionHandler != null) {
					tcpConnectionHandler.dispose();
				}
				if (localConnectionHandler != null) {
					localConnectionHandler.dispose();
				}
			}
		}
	}
}
