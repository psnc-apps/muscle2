/*
Copyright 2008,2009 Complex Automata Simulation Technique (COAST) consortium

GNU Lesser General Public License

This file is part of MUSCLE (Multiscale Coupling Library and Environment).

    MUSCLE is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MUSCLE is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MUSCLE.  If not, see <http://www.gnu.org/licenses/>.
*/

package muscle.client;

import eu.mapperproject.jmml.util.ArraySet;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.*;
import java.util.*;
import muscle.Constant;
import muscle.Version;
import muscle.client.id.JadeAgentIDManipulator;
import muscle.client.instance.JadeInstanceController;
import muscle.core.ConnectionScheme;
import muscle.id.Resolver;
import muscle.id.ResolverFactory;
import muscle.util.JVM;
import muscle.util.MiscTool;

/**
handle booting/terminating of MUSCLE
@author Jan Hegewald
*/
public class JadeBoot implements ResolverFactory {

	private final List<Thread> otherHooks = new LinkedList<Thread>();
	private final File infoFile;
	private final Map<String, String> agentNames;
	private Resolver resolver;
	private static JadeBoot instance;
	private final String[] args;
	private boolean isDone;
	private Set<String> monitorQuit;
	
	static {
		// try to workaround LogManager deadlock http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6487638
		java.util.logging.LogManager manager = java.util.logging.LogManager.getLogManager();
		// n.b.: we still sometimes see the LogManager deadlock
	}

	public static JadeBoot getInstance() {
		return instance;
	}
	
	public static JadeBoot getInstance(String[] args) {
		if (instance == null) {
			instance = new JadeBoot(args);
		}
		return instance;
	}
	
	//
	private JadeBoot(String[] args) {
		this.monitorQuit = null;
		// make sure the JVM singleton has been inited
		JVM jvm = JVM.ONLY;

		System.out.println("booting muscle jvm " + jvm.name());
		
		infoFile = jvm.tmpFile(Constant.Filename.JVM_INFO);
		
		// note: it seems like loggers can not be used within shutdown hooks
		Runtime.getRuntime().addShutdownHook(new JVMHook() );
		
		writeInitialInfo();
		
		this.resolver = null;
		
		agentNames = new HashMap<String,String>();
		this.args = mapAgentsToInstances(args);
		this.isDone = false;
	}
	
	private String[] mapAgentsToInstances(String[] args) {
		StringBuilder sb = new StringBuilder();
		int agentsI = -1;
		StringBuilder locator = new StringBuilder("locator");
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-agents")) {
				agentsI = i+1;
				String agentArg = args[agentsI];
				String[] agents = agentArg.split(";");
				for (String agent : agents) {
					String[] agentInfo = agent.split(":");
					if (agentInfo[0].equals("plumber")) {
						// no plumber needed.
					}
					else if (agentInfo[0].equals("muscle.utilities.agent.QuitMonitor")) {
						// Agents to monitor are given as an argument
						int start = agentInfo[1].indexOf("(") + 1;
						int end = agentInfo[1].indexOf(")");
						String[] quitAgents = agentInfo[1].substring(start, end).split(",");
						this.monitorQuit = new ArraySet<String>(Arrays.asList(quitAgents));
					}
					else {
						agentNames.put(agentInfo[0], agentInfo[1]);
						locator.append('.').append(agentInfo[0]);
						sb.append(agentInfo[0]).append(':').append(JadeInstanceController.class.getCanonicalName()).append(';');
					}
				}
			}
		}
		
		if (agentsI != -1) {
			locator.append(':').append(JadeAgentIDManipulator.class.getCanonicalName());
			sb.append(locator);
			args[agentsI] = sb.toString();
		}

		return args;
	}
	
	public void init() {
		System.out.println("initialize JADE");
		JADE jade = new JADE(args);
		otherHooks.addAll(jade.getShutdownHooks());
	}

	public String getAgentClass(String agentName) {
		return agentNames.get(agentName);
	}
	
	public Set<String> monitorQuit() {
		return this.monitorQuit;
	}
	
	public synchronized void registerResolver(Resolver res) {
		this.resolver = res;
		this.notifyAll();
	}
	
	/** waits for the locator to register */
	public synchronized Resolver getResolver() throws InterruptedException {
		while (!this.isDone && this.resolver == null) {
			this.wait();
		}
		if (this.isDone) {
			throw new InterruptedException("Getting local resolver interrupted");
		}
		return this.resolver;
	}
	
	public synchronized void dispose() {
		this.isDone = true;
		this.notifyAll();
	}
	
	public static void main(String args[]) {
		JadeBoot boot = JadeBoot.getInstance(args);
		boot.init();
		ConnectionScheme.getInstance(boot);

//		jade.Boot.main(args); // forward booting to jade
	}
	
	
	//
	private void writeInitialInfo() {
	
		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile);
			writer.write( "this is file <"+infoFile+"> created by <"+getClass()+">"+nl );
			writer.write( "start date: "+(new java.util.Date())+nl );
			writer.write( "cwd: "+System.getProperty("user.dir")+nl );
			writer.write( "user: "+System.getProperty("user.name")+nl );
			
			OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
			writer.write( "OS: "+os.getName()+" "+os.getVersion()+nl );
			writer.write( "CPU: "+os.getAvailableProcessors()+" "+os.getArch()+nl );
			writer.write(nl);
			
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			writer.write( "name: "+runtime.getName()+nl );
			writer.write( "version: "+Version.info()+nl );			
			writer.write( "vm: "+runtime.getVmName()+" "+runtime.getVmVersion()+nl );
			writer.write( "classpath: "+runtime.getClassPath()+nl );
			writer.write( "JADE version: "+jade.core.Runtime.getVersionInfo()+nl );			
			writer.write( "arguments: "+MiscTool.joinItems(runtime.getInputArguments(), System.getProperty("path.separator"))+nl );			
			writer.write(nl);
			writer.write( "executing ..."+nl );
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				}
				catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}


	//
	private void writeClosingInfo() {
	
		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile, true);
			
			writer.write(nl);
			writer.write( "... terminating"+nl );
			writer.write(nl);
			writer.write( "uptime: "+ManagementFactory.getRuntimeMXBean().getUptime()/1000.0+" s"+nl );
			writer.write( "end date: "+(new java.util.Date())+nl );
			
			writer.close();
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				}
				catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}


	//
	private void writeStackTraces() {
	
		String nl = System.getProperty("line.separator");
		FileWriter writer = null;
		try {
			writer = new FileWriter(infoFile, true);
			
			writer.write(nl);
			writer.write( "thread dump"+nl );
			writer.write(nl);
			
			ThreadMXBean tBean = ManagementFactory.getThreadMXBean();
			ThreadInfo[] tInfos = tBean.dumpAllThreads(true, true);
			for(ThreadInfo ti : tInfos) {
				writer.write(ti.toString()+nl);
			}
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if(writer != null) {
				try {
					writer.close();
				}
				catch (java.io.IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private class JVMHook extends Thread {
		// CTRL-C is signal 2 (SIGINT)
		// see rubys Signal.list for a full list on your OS
		public void run() {
			dispose();
			System.out.println("terminating muscle jvm "+JVM.ONLY.name());
			writeClosingInfo();
			
			int i = 0;

			// wait for all other threads/shutdownhooks to die
			while( !otherHooks.isEmpty() ) {

				for(Iterator<Thread> iter = otherHooks.iterator(); iter.hasNext(); ) {
					Thread t = iter.next();
					if( !t.isAlive() )
						iter.remove();
				}
				
				if( !otherHooks.isEmpty() ) {
					if (++i == 15) {
						i = 0;
						System.out.print(".");					
					}
					try {
						sleep(50l);
					}
					catch(java.lang.InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			writeStackTraces();
			System.out.println("bye");
		}		
	}

}
