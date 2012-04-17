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
package muscle.core.kernel;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.*;
import muscle.core.ident.Identifier;
import muscle.core.ident.PortalID;
import muscle.core.messaging.Timestamp;
import muscle.core.messaging.serialization.DataConverter;
import muscle.core.messaging.serialization.PipeConverter;
import muscle.exception.IgnoredException;
import muscle.exception.MUSCLERuntimeException;
import utilities.MiscTool;
import utilities.OSTool;
import utilities.data.ArrayMap;
import utilities.jni.JNIMethod;

// experimental info mode with
// coast sk:coast.cxa.test.sandbox.RawKernel\("execute true"\) --cxa_file src/coast/cxa/test.sandbox --main
/**
A basic kernel, that all kernels must extend
@author Jan Hegewald
 */
public abstract class RawKernel {
	private static final Logger logger = Logger.getLogger(RawKernel.class.getName());
	private Object[] arguments;
	protected Map<String,ConduitEntranceController> entrances = new ArrayMap<String,ConduitEntranceController>();
	protected Map<String,ConduitExitController> exits = new ArrayMap<String,ConduitExitController>();
	private boolean acceptPortals;
	protected InstanceController controller;

	public void setInstanceController(InstanceController ic) {
		this.controller = ic;
	}
	
	/**
	 * Get the local name of the current kernel. This call is delegated to the InstanceController.
	*/
	protected String getLocalName() {
		return this.controller.getLocalName();
	}

	/**
	prints info about a given kernel to stdout
	 */
	public static String info(Class<? extends RawKernel> controllerClass) {
		// instantiates a controller which is not intended to run as a JADE agent
		RawKernel kernel = null;
		try {
			kernel = controllerClass.newInstance();
		} catch (java.lang.InstantiationException e) {
			throw new RuntimeException(e);
		} catch (java.lang.IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		kernel.connectPortals();

		return kernel.infoText();
	}

	/**
	currently returns true if all portals of this kernel have passed a maximum timestep given in the cxa properties
	(this is a temporary implementation because this mechanism is going to change anyway)
	note: if init portals are being used, they do increment their time only once!
	do not change signature! (used from native code)
	 */
	public boolean willStop() {
		int maxSeconds = CxADescription.ONLY.getIntProperty(CxADescription.Key.MAX_TIMESTEPS.toString());
		Timestamp maxTime = new Timestamp(maxSeconds);
		Timestamp portalTime = maxTime;

		// search for the smallest "time" in our portals
		for (Portal p : entrances.values()) {
			logger.log(Level.FINER, "Entrance SI Time of {0} is {1}", new Object[]{p, p.getSITime()});
			if (p.getSITime().compareTo(portalTime) < 0) {
				portalTime = p.getSITime();
			}
		}
		for (Portal p : exits.values()) {
			logger.log(Level.FINER, "Exit SI Time of {0} is {1}", new Object[]{p, p.getSITime()});
			if (p.getSITime().compareTo(portalTime) < 0) {
				portalTime = p.getSITime();
			}
		}

		return portalTime.compareTo(maxTime) > -1;
	}

	public JNIMethod stopJNIMethod() {
		return new JNIMethod(this, "willStop");
	}

	public KernelBootInfo getKernelBootInfo() {
		return new KernelBootInfo(controller.getLocalName(), getClass());
	}

	// in case we want do do custom initialization where we need e.g. the JADE services already activated
	protected void beforeSetup() {
	}

	// currently we can not add portals dynamically during runtime
	// add all portals here (and only here) at once
	final void connectPortals() {
		acceptPortals = true;
		addPortals();
		acceptPortals = false;
	}

	// use addExit/addEntrance to add portals to this controller
	protected abstract void addPortals();

	protected abstract void execute();

	/**
	 * Just executes the kernel.
	 * 
	 * This method is used by running MPI tasks for all nodes with non-zero rank.
	 *  
	 * Override this method to provide own method of starting a slave kernel. 
	 */
	public void executeDirectly() {
		execute();
	}

	/**
	 * SI Scale will be specified using MML, not in MUSCLE.
	return the SI scale of a kernel<br>
	for a 1D kernel with dx=1meter and dt=1second this would e.g. be<br>
	javax.measure.DecimalMeasure<javax.measure.quantity.Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.SECOND);<br>
	javax.measure.DecimalMeasure<javax.measure.quantity.Length> dx = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.METER);<br>
	return new Scale(dt,dx);
	* return null if the kernel is dimensionless
	 */
	public abstract Scale getScale();
	
	/**
	helper method to create a scale object where dt is a multiple of the kernel scale
	* @deprecated use MML to specify this
	 */
	@Deprecated
	private Scale getPortalScale(int rate) {
		return null;
	}

	protected void addEntrance(ConduitEntranceController entrance) {
		String portName = entrance.getIdentifier().getPortName();

		// only add if not already added
		if (entrances.containsKey(portName)) {
			throw new MUSCLERuntimeException("can not add entrance twice <" + entrance + ">");
		}

		controller.addConduitEntrance(entrance);
		entrances.put(portName, entrance);
	}

	protected void addExit(ConduitExitController exit) {
		if (!acceptPortals) {
			throw new IgnoredException("adding of portals not allowed here");
		}
		String portName = exit.getIdentifier().getPortName();

		// only add if not already added
		if (exits.containsKey(portName)) {
			throw new MUSCLERuntimeException("can not add exit twice <" + exit + ">");
		}

		controller.addConduitExit(exit);
		exits.put(portName, exit);
	}

	
	protected <T extends Serializable> ConduitExit<T> addExit(String newPortalName, int newRate, Class<T> newDataClass) {
		if (!acceptPortals) {
			throw new IgnoredException("adding of portals not allowed here");
		}

		if (newRate < 1) {
			throw new IllegalArgumentException("portal use rate is <" + newRate + "> but can not be < 1");
		}

		ConduitExitController<T> ec = new ConduitExitController<T>(new PortalID<Identifier>(newPortalName, controller.getIdentifier()), controller, newRate, new DataTemplate<T>(newDataClass));

		ConduitExit<T> e = new ConduitExit<T>(ec);
		ec.setExit(e);
		addExit(ec);

		return e;
	}

	protected <T extends Serializable, R> JNIConduitExit<T, R> addJNIExit(String newPortalName, int newRate, Class<T> newDataClass, Class<R> newJNIClass, DataConverter<R, T> newTransmuter) {
		ConduitExitController<T> ec = new ConduitExitController<T>(new PortalID<Identifier>(newPortalName, controller.getIdentifier()), controller, newRate, new DataTemplate<T>(newDataClass));

		JNIConduitExit<T, R> e = new JNIConduitExit<T, R>(newTransmuter, newJNIClass, ec);
		ec.setExit(e);
		addExit(ec);
		return e;
	}

	protected <T extends Serializable> JNIConduitExit<T, T> addJNIExit(String newPortalName, int newRate, Class<T> newDataClass) {
		DataConverter<T, T> newTransmuter = new PipeConverter<T>();
		return addJNIExit(newPortalName, newRate, newDataClass, newDataClass, newTransmuter);
	}

	protected <T extends Serializable> ConduitEntrance<T> addEntrance(String newPortalName, int newRate, Class<T> newDataClass, EntranceDependency... newDependencies) {
		ConduitEntranceController<T> ec = new ConduitEntranceController<T>(new PortalID<Identifier>(newPortalName, controller.getIdentifier()), controller, newRate, new DataTemplate<T>(newDataClass), newDependencies);

		ConduitEntrance<T> e = new ConduitEntrance<T>(ec, getScale());
		ec.setEntrance(e);

		addEntrance(ec);
		return e;
	}

	protected <R, T extends Serializable> JNIConduitEntrance<R, T> addJNIEntrance(String newPortalName, int newRate, Class<R> newJNIClass, Class<T> newDataClass, DataConverter<R, T> newTransmuter, EntranceDependency... newDependencies) {
		ConduitEntranceController<T> ec = new ConduitEntranceController<T>(new PortalID<Identifier>(newPortalName, controller.getIdentifier()), controller, newRate, new DataTemplate<T>(newDataClass), newDependencies);

		JNIConduitEntrance<R, T> e = new JNIConduitEntrance<R, T>(newTransmuter, newJNIClass, ec, getScale());
		ec.setEntrance(e);

		addEntrance(ec);
		return e;
	}

	protected <T extends Serializable> JNIConduitEntrance<T, T> addJNIEntrance(String newPortalName, int newRate, Class<T> newDataClass, EntranceDependency... newDependencies) {
		DataConverter<T, T> newTransmuter = new PipeConverter<T>();
		return addJNIEntrance(newPortalName, newRate, newDataClass, newDataClass, newTransmuter, newDependencies);
	}

	/**
	returns path to a tmp directory for this kernel<br>
	do not change signature! (used from native code)
	 */
	public String getTmpPath() {
		String tmpPath = MiscTool.joinPaths(CxADescription.ONLY.getTmpRootPath(), OSTool.portableFileName(controller.getLocalName(), ""));
		File tmpDir = new File(tmpPath);
		// create our kernel tmp dir if not already there
		tmpDir.mkdir();
		if (!tmpDir.isDirectory()) {
			getLogger().log(Level.SEVERE, "invalid tmp path <{0}> for kernel <{1}>", new Object[]{tmpDir, controller.getLocalName()});
		}

		return tmpDir.getPath();
	}

	/**
	do not change signature! (used from native code)
	 */
	static public String getCxAProperty(String key) {
		return CxADescription.ONLY.getProperty(key);
	}

	/**
	reference to the CxA of this kernel
	do not change signature! (used from native code)
	 */
	static public CxADescription getCxA() {
		return CxADescription.ONLY;
	}

	String infoText() {
		StringBuilder sb = new StringBuilder(25*(1 + exits.size()+entrances.size()));
		sb.append(getLocalName()).append(": exits: ").append(exits).append("; entrances: ").append(entrances);
		return sb.toString();
	}

	/**
	pass one or more controller class names to get an info about these controllers
	 */
	public static void main(String[] args) throws java.lang.ClassNotFoundException {

		if (args.length > 0) {
			ArrayList<Class<? extends RawKernel>> classes = new ArrayList<Class<? extends RawKernel>>();
			for (String arg : args) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends RawKernel> c = (Class<? extends RawKernel>) Class.forName(arg);
					classes.add(c);
				} catch (ClassCastException e) {
					Logger.getLogger(RawKernel.class.getName()).log(Level.SEVERE, "Class " + arg + " does not represent a RawKernel", e);
				}
			}

			for (Class<? extends RawKernel> c : classes) {
				System.out.println(RawKernel.info(c) + "\n");
			}

		}
	}

	protected Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}
	
	protected void log(String msg) {
		log(msg, Level.INFO);
	}

	protected void log(String msg, Level lvl) {
		getLogger().log(lvl, msg);
	}

	void setArguments(Object[] args) {
		this.arguments = args;
	}

	public Object[] getArguments() {
		if (arguments == null) {
			return new Object[0];
		}

		return arguments;
	}
	
	public String getProperty(String key) {
		return CxADescription.ONLY.getProperty(key);
	}
	public int getIntProperty(String key) {
		return CxADescription.ONLY.getIntProperty(key);
	}
	public double getDoubleProperty(String key) {
		return CxADescription.ONLY.getDoubleProperty(key);
	}
	public boolean getBooleanProperty(String key) {
		return CxADescription.ONLY.getBooleanProperty(key);
	}
}
