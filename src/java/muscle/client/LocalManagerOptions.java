/*
* Copyright 2008, 2009 Complex Automata Simulation Technique (COAST) consortium
* Copyright 2010-2013 Multiscale Applications on European e-Infrastructures (MAPPER) project
*
* GNU Lesser General Public License
* 
* This file is part of MUSCLE (Multiscale Coupling Library and Environment).
* 
* MUSCLE is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* MUSCLE is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/
package muscle.client;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import eu.mapperproject.jmml.util.FastArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.kernel.RawInstance;
import muscle.id.InstanceClass;
import muscle.net.SocketFactory;

/**
 *
 * @author Joris Borgdorff
 */
public class LocalManagerOptions {
	private final JCommander jcom;
	
	@Parameter(description="INST_NAME:INST_CLASS ...",converter=AgentNameConverter.class)
	private List<InstanceClass> agents = new FastArrayList<InstanceClass>();
	
	@Parameter(names={"-m", "--manager"},required=true,converter=SocketAddressConverter.class)
	private InetSocketAddress managerAddress;
	
	@Parameter(names={"-a", "--address"},converter=SocketAddressConverter.class)
	private InetSocketAddress localAddress = getLocalAddress(0);
	
	@Parameter(names={"-t", "--threads"})
	private int threads = 0;
	
	@Parameter(names={"-f", "--instances-file"})
	private File instancesFile = null;
	
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
	
	public List<InstanceClass> getAgents() throws FileNotFoundException, IOException {
		if (this.instancesFile != null) {
			BufferedReader reader = new BufferedReader(new FileReader(this.instancesFile));
			try {
				String line;
				AgentNameConverter converter = new AgentNameConverter();
				while ((line = reader.readLine()) != null) {
					this.agents.add(converter.convert(line));
				}
			} finally {
				reader.close();
				this.instancesFile = null;
			}
		}
		return this.agents;
	}
	
	public Set<String> getAgentNames() throws FileNotFoundException, IOException {
		List<InstanceClass> ics = this.getAgents();
		Set<String> set = new HashSet<String>(ics.size());
		for (InstanceClass cl : ics) {
			set.add(cl.getName());
		}
		return set;
	}
	
	public InetSocketAddress getManagerSocketAddress() {
		return this.managerAddress;
	}
	
	public InetSocketAddress getLocalSocketAddress() {
		return this.localAddress;
	}
	
	public int getThreads() {
		return this.threads;
	}
	
	public static class AgentNameConverter implements IStringConverter<InstanceClass> {
		@Override
		public InstanceClass convert(String value) {
			int index = value.indexOf(':');
			if (index == -1) {
				throw new ParameterException("An Instance should be specified by a name and a class, separated by a colon (:); argument " + value + " is not formatted as such.");
			} else if (index == 0) {
					throw new ParameterException("An Instance name may not be empty; argument " + value + " does not specify a name.");
			} else if (index == value.length() - 1) {
				throw new ParameterException("An Instance class may not be empty; argument " + value + " does not specify a class.");				
			} else if (index != value.lastIndexOf(':')) {
				throw new ParameterException("The name or class of an instance may not contain a colon; argument " + value + " contains too many colons.");
			}
			
			Class<?> clazz;
			
			try {
				clazz = Class.forName(value.substring(index + 1));
			} catch (ClassNotFoundException ex) {
				throw new ParameterException("Instance class " + value.substring(index + 1) + " of argument " + value + " is not found; make sure the cxa file and the class name match, and that all sources are included in the CLASSPATH.\nHINT: adjust the cxa file to include your build directory with\nm = Muscle.LAST\nm.add_classpath File.dirname(__FILE__)+\"[REL_PATH_TO_CLASSES]\"\n");
			}
			if (!RawInstance.class.isAssignableFrom(clazz)) {
				throw new ParameterException("Can only instantiate classes inhereting muscle.core.kernel.RawKernel");
			}
			return new InstanceClass(value.substring(0, index), clazz);
		}
	}
	
	
	public static class SocketAddressConverter implements IStringConverter<InetSocketAddress> {
		@Override
		public InetSocketAddress convert(String value) {
			int lastColon = value.lastIndexOf(':');
			if (lastColon == -1) {
				throw new ParameterException("Location should be specified by an address and a port, separated by a colon (:); argument " + value + " is not formatted as such.");
			}
			String addrStr = value.substring(0, lastColon);
			String portStr = value.substring(lastColon + 1);
			InetAddress addr;
			int port;
			
			try {
				addr = InetAddress.getByName(addrStr);
				port = Integer.parseInt(portStr);
			} catch (UnknownHostException ex) {
				throw new ParameterException("Internet address " + addrStr + " in address " + value + " is not reachable.");
			} catch (NumberFormatException ex) {
				throw new ParameterException("Port " + portStr + " in address " + value + " is not a valid number.");
			}
			
			return new InetSocketAddress(addr, port);
		}
	}
	
	private static InetSocketAddress getLocalAddress(int port) {
		try {
			InetAddress addr = SocketFactory.getMuscleHost();
			return new InetSocketAddress(addr, port);
		} catch (UnknownHostException ex) {
			Logger.getLogger(LocalManagerOptions.class.getName()).log(Level.SEVERE, "Could not resolve localhost, to start listening for connections.", ex);
			return null;
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
