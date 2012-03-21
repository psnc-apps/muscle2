package muscle.core.standalone;

import java.util.Arrays;
import java.util.List;
import muscle.core.CxADescription;
import muscle.core.kernel.CAController;
import muscle.exception.MUSCLERuntimeException;


public abstract class NativeKernel extends CAController  implements NativeGateway.CallListener {
	
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
	
	@Deprecated
	public synchronized void sendDouble(String entranceName, double data[]) {
	/*		for (int i=0; i < entrances.size(); i++) {
			if (entrances.get(i).getEntrance().getPortalID().getName().split("@")[0].equals(entranceName)) {
				entrances.get(i).send(data);
				return;
			}
		}
	*/	
		throw new MUSCLERuntimeException("Unknown entrance: " + entranceName);
	}
	
	@Deprecated
	public synchronized double[] receiveDouble(String exitName) {
	/*	for (int i=0; i < exits.size(); i++) {
			if (exits.get(i).getExit().getPortalID().getName().split("@")[0].equals(exitName)) {
				return (double[])exits.get(i).receive();
			}
		}
	*/	
		throw new MUSCLERuntimeException("Unknown exit: " + exitName);
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

	@Override
	protected void execute() {
		
		try {
			NativeGateway gateway = new NativeGateway(this);
			gateway.start();
			
			//TODO: check if exitst
			ProcessBuilder pb = new ProcessBuilder();
			
			buildCommand(pb.command());

		
			pb.environment().put("MUSCLE_GATEWAY_PORT", gateway.getContactInformation());
			
			getLogger().info("Spawning standalone kernel: " + pb.command());
			getLogger().fine("Contact information: " + gateway.getContactInformation());
			
			Process child = pb.start();
			
			StreamRipper stdoutR = new StreamRipper(System.out, child.getInputStream());
			StreamRipper stderrR = new StreamRipper(System.err, child.getErrorStream());
			
			stdoutR.start();
			stderrR.start();
			
			int exitCode = child.waitFor();
			
			if (exitCode == 0)
				getLogger().info("Command " + pb.command() + " finished with exit code " + exitCode);
			else
				getLogger().info("Command " + pb.command() + " finished with exit code " + exitCode);

			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}
