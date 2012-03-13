package muscle.core;

import com.beust.jcommander.*;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import muscle.core.ident.InstanceClass;
import muscle.core.kernel.RawKernel;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalManagerOptions {
	private JCommander jcom;
	
	@Parameter(description="INST_NAME:INST_CLASS ...",required=true,converter=AgentNameConverter.class)
	private List<InstanceClass> agents = new ArrayList<InstanceClass>();
	
	@Parameter(names={"-m", "--manager"},required=true,converter=SocketAddressConverter.class)
	private InetSocketAddress managerAddress;
	
	@Parameter(names={"-a", "--address"},converter=SocketAddressConverter.class)
	private InetSocketAddress localAddress = new InetSocketAddress("127.0.0.1", 6872);
	
	public LocalManagerOptions(String... args) {
		this.jcom = new JCommander(this);
		try {
			jcom.parse(args);
		} catch (ParameterException ex) {
			System.err.println("Could not parse command line arguments: " + ex);
			jcom.usage();
			System.exit(1);
		}
	}
	
	public void printUsage() {
		jcom.usage();
	}
	
	public List<InstanceClass> getAgents() {
		return this.agents;
	}
	
	public InetSocketAddress getManagerSocketAddress() {
		return this.managerAddress;
	}
	
	public InetSocketAddress getLocalSocketAddress() {
		return this.localAddress;
	}
	
	public static class AgentNameConverter implements IStringConverter<InstanceClass> {
		@Override
		public InstanceClass convert(String value) {
			String[] s = value.split(":");
			if (s.length < 2) {
				throw new ParameterException("An Instance should be specified by a name and a class, separated by a colon (:); argument " + value + " is not formatted as such.");
			} else if (s.length > 2) {
				throw new ParameterException("The name or class of an instance may not contain a colon; argument " + value + " contains too many colons.");
			}
			
			Class<?> clazz;
			
			if (s[0].equals("")) {
				throw new ParameterException("An Instance name may not be empty; argument " + value + " does not specify a name.");
			}
			
			if (s[1].equals("")) {
				throw new ParameterException("An Instance class may not be empty; argument " + value + " does not specify a class.");
			} else {
				try {
					clazz = Class.forName(s[1]);
				} catch (ClassNotFoundException ex) {
					throw new ParameterException("Instance class " + s[1] + " of argument " + value + " is not found; make sure the cxa file and the class name match, and that all sources are included in the CLASSPATH.\nHINT: adjust the cxa file to include your build directory with\nm = Muscle.LAST\nm.add_classpath File.dirname(__FILE__)+\"[REL_PATH_TO_CLASSES]\"\n");
				}
			}
			if (!RawKernel.class.isAssignableFrom(clazz)) {
				throw new ParameterException("Can only instantiate classes inhereting muscle.core.kernel.RawKernel");
			}
			return new InstanceClass(s[0], clazz);		
		}
	}
	
	
	public static class SocketAddressConverter implements IStringConverter<InetSocketAddress> {
		@Override
		public InetSocketAddress convert(String value) {
			String[] s = value.split(":");
			if (s.length != 2) {
				throw new ParameterException("Location should be specified by an address and a port, separated by a colon (:); argument " + value + " is not formatted as such.");
			}
			
			InetAddress addr;
			
			try {
				addr = InetAddress.getByName(s[0]);
			} catch (UnknownHostException ex) {
				throw new ParameterException("Internet address " + s[0] + " in address " + value + " is not reachable.");
			}
			
			int port;
			
			try {
				port = Integer.parseInt(s[1]);
			} catch (NumberFormatException ex) {
				throw new ParameterException("Port " + s[0] + " in address " + value + " is not a valid number.");
			}
			
			return new InetSocketAddress(addr, port);
		}
	}
	
	public static class WritableFile implements IParameterValidator {
		@Override
		public void validate(String name, String value) throws ParameterException {
			File f = new File(value).getAbsoluteFile();

			File parent = f.getParentFile();
			if (parent == null || !parent.exists()) {
				throw new ParameterException("Directory of file " + value
						+ " of parameter " + name + " does not exist");
			}
		}
	}
	
	public static class ReadableFile implements IParameterValidator {
		@Override
		public void validate(String name, String value) throws ParameterException {
			File f = new File(value);

			if (!f.canRead()) {
				throw new ParameterException("File " + value
						+ " of parameter " + name + " can not be read");
			}
		}
	}
}
