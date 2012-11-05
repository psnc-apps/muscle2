/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.client.communication.PortFactory;
import muscle.client.communication.TcpPortFactoryImpl;
import muscle.client.communication.message.DataConnectionHandler;
import muscle.client.communication.message.LocalDataHandler;
import muscle.client.id.DelegatingResolver;
import muscle.client.id.TcpIDManipulator;
import muscle.id.TcpLocation;
import muscle.client.instance.ThreadedInstanceController;
import muscle.core.ConnectionScheme;
import muscle.core.kernel.InstanceController;
import muscle.core.kernel.InstanceControllerListener;
import muscle.id.*;
import muscle.net.ConnectionHandlerListener;
import muscle.net.CrossSocketFactory;
import muscle.net.SocketFactory;
import muscle.util.FileTool;
import muscle.util.JVM;

/**
 * @author Joris Borgdorff
 */
public class LocalManager implements InstanceControllerListener, ResolverFactory, ConnectionHandlerListener {
	private final static Logger logger = Logger.getLogger(LocalManager.class.getName());
	private final List<InstanceController> controllers;
	private final LocalManagerOptions opts;
	private DataConnectionHandler tcpConnectionHandler;
	private LocalDataHandler localConnectionHandler;
	private DelegatingResolver res;
	private TcpIDManipulator idManipulator;
	private PortFactory factory;
	private boolean isDone;
	private DisposeOfControllersHook disposeOfControllersHook;
	private ForcefulQuitHook forcefulQuitHook;
	private boolean isShutdown;
	
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
		controllers = new ArrayList<InstanceController>(opts.getAgents().size());
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
		
		String managerDir = ((TcpLocation)idManipulator.getManagerLocation()).getTmpDir();
		if (!dir.equals(managerDir) && !managerDir.isEmpty()) {
			FileTool.createSymlink(JVM.ONLY.tmpFile("manager"), new File("../" + managerDir));
		}
		
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
		
		try {
			// Start all instances but the first in a new thread
			for (int i = 1; i < controllers.size(); i++) {
				synchronized (controllers) {
					if (i < controllers.size()) {
						new Thread(controllers.get(i), controllers.get(i).getLocalName()).start();
					}
				}
			}
			// Run the first instance in the current thread
			controllers.get(0).run();
		} catch (OutOfMemoryError er) {
			logger.log(Level.SEVERE, "Out of memory: too many submodels", er);
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
		synchronized (controllers) {
			if (isShutdown || isDone) {
				return;
			}
			isShutdown = true;
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
		
		public synchronized void waitForDisposer() throws InterruptedException {
			if (!finished)
				wait(2000);

			// Not waiting more than 15 seconds, and interrupting every second.
			for (int i = 0; !finished && i < 15; i++) {
				disposeOfControllersHook.interrupt();
				System.out.print(".");
				wait(1000);
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
				if (factory != null) factory.dispose();
				if (idManipulator != null) idManipulator.dispose();
				if (tcpConnectionHandler != null) tcpConnectionHandler.dispose();
				if (localConnectionHandler != null) localConnectionHandler.dispose();
			}
		}
	}
}
