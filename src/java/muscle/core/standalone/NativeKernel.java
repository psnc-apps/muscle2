package muscle.core.standalone;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import muscle.core.*;
import muscle.core.kernel.CAController;
import muscle.exception.MUSCLERuntimeException;
import muscle.util.data.SerializableData;
import muscle.util.data.SerializableDatatype;


public abstract class NativeKernel extends CAController  implements NativeGateway.CallListener {

	private final static String TMPFILE = System.getProperty("muscle.native.tmpfile");
	private SerializableDatatype type;

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
		type = SerializableDatatype.NULL;
	}
	
	public synchronized void send(String entranceName, SerializableData data) {
		ConduitEntranceController ec = entrances.get(entranceName);
		ConduitEntrance entrance;
		if (ec == null || (entrance = ec.getEntrance()) == null)
			throw new MUSCLERuntimeException("Unknown entrance: '" + entranceName + "' in " + getLocalName() + " (valid entrances are " + entrances.keySet() + ")");
		
		entrance.send(data.getValue());		
	}
	
	public synchronized SerializableData receive(String exitName) {
		ConduitExitController ec = exits.get(exitName);
		ConduitExit exit;
		if (ec == null || (exit = ec.getExit()) == null)
			throw new MUSCLERuntimeException("Unknown exit: '" + exitName + "' in " + getLocalName() + " (valid exits are " + exits.keySet() + ")");
		
		Serializable data = exit.receive();
		SerializableData sdata;
		if (data == null) {
			sdata = SerializableData.valueOf(null, SerializableDatatype.NULL);
		} else if (type.getDataClass() == null || !type.getDataClass().isInstance(data)) {
			sdata = SerializableData.valueOf(data);
			type = sdata.getType();
		} else {
			sdata = SerializableData.valueOf(data, type);
		}
		return sdata;
	}

	public synchronized String getKernelName() {
		return getLocalName();
	}
	
	public synchronized String getProperty(String name) {
		return CxADescription.ONLY.getProperty(name);
	}
	
	public synchronized String getProperties() {
		return CxADescription.ONLY.getLegacyProperties();
	}
	
	public synchronized String getTmpPath() {
		return CxADescription.ONLY.getTmpRootPath();
	}
	
	public synchronized void isFinished() {
		isDone = true;
		notify();
	}
	
	protected void buildCommand(List<String> command) {
		if (CxADescription.ONLY.containsKey(getLocalName() + ":debugger"))
			command.add(CxADescription.ONLY.getProperty(getLocalName() + ":debugger"));
		
		if (CxADescription.ONLY.containsKey(getLocalName() + ":command"))
			command.add(CxADescription.ONLY.getProperty(getLocalName() + ":command"));
		else
			throw new IllegalArgumentException("Missing property: " + getLocalName() + ":command" );
		
		if (CxADescription.ONLY.containsKey(getLocalName() + ":args")) {
			String args[] = CxADescription.ONLY.getProperty(getLocalName() + ":args").split(" ");
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
		Process child = pb.start();

		StreamRipper stdoutR = new StreamRipper(System.out, child.getInputStream());
		StreamRipper stderrR = new StreamRipper(System.err, child.getErrorStream());

		stdoutR.start();
		stderrR.start();

		int exitCode = child.waitFor();

		if (exitCode == 0)
			getLogger().log(Level.INFO, "Command `{0}' finished.", pb.command());
		else
			getLogger().log(Level.WARNING, "Command `{0}' failed with exit code {1}.", new Object[]{pb.command(), exitCode});
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
		
		try {
			NativeGateway gateway = new NativeGateway(this);
			gateway.start();
			String port = Integer.toString(gateway.getPort());
			String host = gateway.getInetAddress().getHostAddress();
			
			if (TMPFILE == null) {
				this.runCommand(host, port);
			} else {
				this.writeContactInformation(host, port);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
