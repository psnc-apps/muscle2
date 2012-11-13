package muscle.core.standalone;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import muscle.core.*;
import muscle.core.kernel.CAController;
import muscle.exception.MUSCLERuntimeException;
import muscle.util.data.SerializableData;
import muscle.util.data.SerializableDatatype;

/** A kernel used with a native command.
   *
   * It builds the command line arguments from the parameters debugger, command and args. It keeps in contact
   * with the started command using a TCP IP connection that is controlled by NativeGateway.
   */
public class NativeKernel extends CAController  implements NativeGateway.CallListener {

	private final static Logger logger = Logger.getLogger(NativeKernel.class.toString());
	private final static String TMPFILE = System.getProperty("muscle.native.tmpfile");
	private SerializableDatatype type;
	private Process child;

	private boolean isDone;
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	/*
	@SuppressWarnings("rawtypes")
	protected Map<String, ConduitEntrance> entrances =  new HashMap<String, ConduitEntrance>();
	@SuppressWarnings("rawtypes")
	protected Map<String, ConduitExit> exits = new HashMap<String, ConduitExit>();
	*/
	
	public NativeKernel() {
		super();
		isDone = false;
		child = null;
		type = SerializableDatatype.NULL;
	}
	
	/**
	 * Initializes all ports for you.
	 */
	@Override
	protected void addPortals() {
		ConnectionScheme cs = ConnectionScheme.getInstance();
		Map<String, ? extends PortDescription> ports = cs.entranceDescriptionsForIdentifier(this.controller.getIdentifier());
		if (ports != null) {
			for (PortDescription entrance : ports.values()) {
				this.addSharedDataEntrance(entrance.getID().getPortName(), Serializable.class);
			}
		}
		logger.log(Level.FINE, "{0}: added all conduit entrances", getLocalName());
		ports = cs.exitDescriptionsForIdentifier(this.controller.getIdentifier());
		if (ports != null) {
			for (PortDescription exit : ports.values()) {
				this.addExit(exit.getID().getPortName(), Serializable.class);
			}
		}
		logger.log(Level.FINE, "{0}: added all conduit exits", getLocalName());
	}
	
	public synchronized void send(String entranceName, SerializableData data) {
		ConduitEntranceController ec = entrances.get(entranceName);
		ConduitEntrance entrance;
		if (ec == null || (entrance = ec.getEntrance()) == null) {
			throw new MUSCLERuntimeException("Unknown entrance: '" + entranceName + "' in " + getLocalName() + " (valid entrances are " + entrances.keySet() + ")");
		}
		
		entrance.send(data.getValue());		
	}
	
	public synchronized SerializableData receive(String exitName) {
		ConduitExitController ec = exits.get(exitName);
		ConduitExit exit;
		if (ec == null || (exit = ec.getExit()) == null) {
			throw new MUSCLERuntimeException("Unknown conduit exit: '" + exitName + "' in " + getLocalName() + " (valid exits are " + exits.keySet() + ")");
		}
		
		if (exit.hasNext()) {
			Serializable data = exit.receive();
			SerializableData sdata;
			if (data == null) {
				logger.log(Level.WARNING, "Null values, from exit {0}, are not supported in native code.", exit);
				sdata = SerializableData.valueOf(null, SerializableDatatype.NULL);
			} else if (type.getDataClass() == null || !type.getDataClass().isInstance(data)) {
				sdata = SerializableData.valueOf(data);
				type = sdata.getType();
			} else {
				sdata = SerializableData.valueOf(data, type);
			}
			if (type == SerializableDatatype.JAVA_BYTE_OBJECT) {
				logger.log(Level.WARNING, "Received Java object {0}, from exit {1}, not supported in native code.", new Object[] {data, exit});
			}
			return sdata;
		} else {
			return null;
		}
	}
	
	public synchronized boolean hasNext(String exitName) {
		ConduitExitController ec = exits.get(exitName);
		ConduitExit exit;
		if (ec == null || (exit = ec.getExit()) == null) {
			throw new MUSCLERuntimeException("Unknown conduit exit: '" + exitName + "' in " + getLocalName() + " (valid exits are " + exits.keySet() + ")");
		}
		
		return exit.hasNext();
	}

	public synchronized String getKernelName() {
		return getLocalName();
	}
	
	public synchronized String getProperty(String name) {
		return super.getProperty(name);
	}
	
	public synchronized String getProperties() {
		return CxADescription.ONLY.getLegacyProperties();
	}
	
	public synchronized String getTmpPath() {
		return super.getTmpPath();
	}
	
	public synchronized void isFinished() {
		isDone = true;
		notify();
	}
	
	/** Adds the command to execute to the given list. */
	protected void buildCommand(List<String> command) {
		if (hasInstanceProperty("debugger")) {
			command.add(getProperty("debugger"));
		}
		
		if (hasInstanceProperty("command")) {
			command.add(getProperty("command"));
		}
		else {
			throw new IllegalArgumentException("Missing property: " + getLocalName() + ":command" );
		}
		
		if (hasInstanceProperty("args")) {
			String args[] = getProperty("args").split(" ");
			command.addAll(Arrays.asList(args));
		}
	}
	
	protected void runCommand(String host, String port) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder();
			
		buildCommand(pb.command());

		pb.environment().put("MUSCLE_GATEWAY_PORT", port);
		pb.environment().put("MUSCLE_GATEWAY_HOST", host);
	
		getLogger().log(Level.INFO, "Spawning standalone kernel: {0}", pb.command());
		getLogger().log(Level.FINE, "Contact information: {0}", port);	
		
		if (getLogger().isLoggable(Level.FINEST)) {
			for (String envName : pb.environment().keySet()) {
				getLogger().log(Level.FINEST, "Env: {0}={1}", new Object[]{ envName, pb.environment().get(envName)});	
			}
		}
		
		synchronized (this) {
			child = pb.start();
		}

		StreamRipper stdoutR = new StreamRipper("stdout-reader-"+getLocalName(), System.out, child.getInputStream());
		StreamRipper stderrR = new StreamRipper("stderr-reader-"+getLocalName(), System.err, child.getErrorStream());

		stdoutR.start();
		stderrR.start();

		int exitCode = child.waitFor();

		if (exitCode == 0) {
			getLogger().log(Level.INFO, "Command {0} finished.", pb.command());
		} else {
			getLogger().log(Level.WARNING, "Command {0} failed with exit code {1}.", new Object[]{pb.command(), exitCode});
		}
	}
	
	protected void writeContactInformation(String host, String port) throws InterruptedException, IOException {
		FileWriter fw = new FileWriter(TMPFILE);
		StringBuilder sb = new StringBuilder(24);
		sb.append(host).append(':').append(port);
		fw.append(sb);
		fw.close();

		synchronized (this) {
			while (!isDone) {
				wait();
			}
		}
	}

	@Override
	protected void execute() {
		NativeGateway gateway = null;

		try {
			gateway = new NativeGateway(this);
			gateway.start();
			String port = Integer.toString(gateway.getPort());
			String host = gateway.getInetAddress().getHostAddress();
			
			if (TMPFILE == null) {
				this.runCommand(host, port);
			} else {
				this.writeContactInformation(host, port);
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE, getLocalName() + " could not start communicating with native code", ex);
			this.controller.fatalException(ex);
		} finally {
			// Make sure the gateway thread quits
			if (gateway != null) {
				gateway.dispose();
			}
		}
	}
	
	/**
	 * Get int value of the log Level {@link java.util.logger.Level}.
	 * 
	 * Returns the log level of the ConsoleHandler, if set, otherwise returns Level.ALL.
	 */
	public int getLogLevel() {
		String strConsoleLevel = LogManager.getLogManager().getProperty("java.util.logging.ConsoleHandler.level");
		try {
			return Level.parse(strConsoleLevel).intValue();
		} catch (Throwable ex) {
			// Just log everything if there is no well-defined log level.
			return 0;
		}
	}
	
	public synchronized void afterExecute() {
		if (child != null) {
			child.destroy();
		}
	}
	
	public void fatalException(Throwable thr) {
		this.controller.fatalException(thr);
	}
}
