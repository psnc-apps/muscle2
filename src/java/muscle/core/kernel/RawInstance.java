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

import eu.mapperproject.jmml.util.ArrayMap;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.*;
import muscle.core.model.Distance;
import muscle.core.model.Timestamp;
import muscle.exception.IgnoredException;
import muscle.exception.MUSCLERuntimeException;
import muscle.util.MiscTool;
import muscle.util.OSTool;
import muscle.util.jni.JNIMethod;
import muscle.util.serialization.DataConverter;
import muscle.util.serialization.PipeConverter;

// experimental info mode with
// coast sk:coast.cxa.test.sandbox.RawKernel\("execute true"\) --cxa_file src/coast/cxa/test.sandbox --main
/**
A basic kernel, that all kernels must extend
@author Jan Hegewald
 */
public abstract class RawInstance {
	private static final Logger logger = Logger.getLogger(RawInstance.class.getName());
	private Object[] arguments;
	protected Map<String,ConduitEntranceController> entrances = new ArrayMap<String,ConduitEntranceController>();
	protected Map<String,ConduitExitController> exits = new ArrayMap<String,ConduitExitController>();
	private boolean acceptPortals;
	protected InstanceController controller;
	protected Timestamp maxTime = null;
	protected Timestamp originTime = null;
	private Scale currentScale = null;
	
	/**
	 * Get the local name of the current kernel. This call is delegated to the InstanceController.
	*/
	protected String getLocalName() {
		return this.controller.getLocalName();
	}

	/**
	currently returns true if all portals of this kernel have passed a maximum timestep given in the cxa properties
	(this is a temporary implementation because this mechanism is going to change anyway)
	note: if init portals are being used, they do increment their time only once!
	do not change signature! (used from native code)
	 */
	public boolean willStop() {
		if (originTime == null) {
			originTime = Timestamp.ZERO;
		}
		if (maxTime == null) {
			maxTime = Timestamp.valueOf(CxADescription.ONLY.getProperty(CxADescription.Key.MAX_TIMESTEPS.toString()));
		}
		Distance omegaInterval = getScale().getOmegaT();
		Timestamp omegaT;
		if (omegaInterval == null) {
			omegaT = maxTime;
		} else {
			omegaT = originTime.add(omegaInterval);
			if (maxTime.compareTo(omegaT) < 0) {
				omegaT = maxTime;
			}
		}
		Timestamp portalTime = originTime;

		final boolean isLogFiner = logger.isLoggable(Level.FINER);
		Object[] msg = isLogFiner ? new Object[2] : null;
		
		// search for the maximum "time" in our portals
		for (ConduitEntranceController p : entrances.values()) {
			if (isLogFiner) {
				msg[0] = p; msg[1] = p.getSITime();
				logger.log(Level.FINER, "Entrance SI Time of {0} is {1}", msg);
			}
			if (p.getSITime().compareTo(portalTime) > 0) {
				portalTime = p.getSITime();
			}
		}
		for (ConduitExitController p : exits.values()) {
			if (isLogFiner) {
				msg[0] = p; msg[1] = p.getSITime();
				logger.log(Level.FINER, "Exit SI Time of {0} is {1}", msg);
			}
			if (p.getSITime().compareTo(portalTime) > 0) {
				portalTime = p.getSITime();
			}
		}

		return portalTime.compareTo(omegaT) >= 0;
	}

	// in case we want do do custom initialization where we need e.g. the JADE services already activated
	protected void beforeSetup() {
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
	 * SI Scale will be specified in the CxA file, not in MUSCLE.
	return the SI scale of a kernel<br>
	for a 1D kernel with dx=1meter and dt=1second this would e.g. be<br>
	javax.measure.DecimalMeasure<javax.measure.quantity.Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.SECOND);<br>
	javax.measure.DecimalMeasure<javax.measure.quantity.Length> dx = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.METER);<br>
	return new Scale(dt,dx);
	* return null if the kernel is dimensionless
	 */
	public Scale getScale() {
		if (this.currentScale == null) {
			Distance dt, omegaT, next;
			List<Distance> dx = new ArrayList<Distance>(3);

			dt = getScaleProperty("dt", "default_dt", true);
			omegaT = getScaleProperty("T", "max_timesteps", true);
			next = getScaleProperty("dx", "default_dx", false);
			if (next != null) {
				dx.add(next);
			}
			next = getScaleProperty("dy", "default_dy", false);
			if (next != null) {
				dx.add(next);
			}
			next = getScaleProperty("dz", "default_dz", false);
			if (next != null) {
				dx.add(next);
			}

			currentScale = new Scale(dt, omegaT, dx);
		}
		return currentScale;
	}
	
	protected void setScale(Scale scale) {
		this.currentScale = scale;
	}
	
	private Distance getScaleProperty(String name, String global, boolean warn) {
		Object raw = null;
		Distance dist;
		if (hasInstanceProperty(name)) {
			raw = getRawProperty(name);
		} else if (hasGlobalProperty(global)) {
			raw = CxADescription.ONLY.getRawProperty(global);
		}
		
		if (raw == null) {
			if (warn) {
				logger.log(Level.WARNING, "Time step (dt) of instance {0} could not be determined: properties ''{0}:{1}'' and ''{2}'' are not set; using dt = 1s.", new Object[] {getLocalName(), name, global});
				dist = Distance.ONE;
			} else {
				dist = null;
			}
		} else if (raw instanceof Double) {
			dist = new Distance((Double)raw);
		} else {
			dist = Distance.valueOf(raw.toString());
		}
		return dist;
	}

	protected <T extends Serializable> ConduitExit<T> addExit(String portName, Class<T> dataClass) {
		ConduitExitController<T> ec = controller.createConduitExit(false, portName, new DataTemplate<T>(dataClass));

		ConduitExit<T> e = new ConduitExit<T>(ec);
		ec.setExit(e);
		addExitToList(portName, ec);

		return e;
	}
	
	protected <T extends Serializable, R> JNIConduitExit<T, R> addJNIExit(String portName, Class<T> dataClass, Class<R> jniClass, DataConverter<R, T> transmuter) {
		ConduitExitController<T> ec = controller.createConduitExit(true, portName, new DataTemplate<T>(dataClass));

		JNIConduitExit<T,R> e = new JNIConduitExit<T,R>(transmuter, jniClass, ec);
		ec.setExit(e);
		addExitToList(portName, ec);

		return e;		
	}
	protected <T extends Serializable> JNIConduitExit<T, T> addJNIExit(String portName, Class<T> dataClass, Class<T> jniClass) {
		return addJNIExit(portName, dataClass, jniClass, new PipeConverter<T>());
	}

	protected <T extends Serializable> ConduitEntrance<T> addEntrance(String portName, Class<T> dataClass) {
		ConduitEntranceController<T> ec = controller.createConduitEntrance(true, portName, new DataTemplate<T>(dataClass));

		Distance dt = getScale() == null ? Distance.ZERO : getScale().getDt();
		ConduitEntrance<T> e = new ConduitEntrance<T>(ec, originTime == null ? Timestamp.ZERO : originTime, dt);
		ec.setEntrance(e);
		addEntranceToList(portName, ec);

		return e;
	}
	
	protected <T extends Serializable> ConduitEntrance<T> addSynchronizedEntrance(String portName, Class<T> dataClass) {
		ConduitEntranceController<T> ec = controller.createConduitEntrance(false, portName, new DataTemplate<T>(dataClass));

		Distance dt = getScale() == null ? Distance.ZERO : getScale().getDt();
		ConduitEntrance<T> e = new ConduitEntrance<T>(ec, originTime == null ? Timestamp.ZERO : originTime, dt);
		ec.setEntrance(e);
		addEntranceToList(portName, ec);

		return e;
	}

	protected <T extends Serializable, R> JNIConduitEntrance<R, T> addJNIEntrance(String portName, Class<R> jniClass, Class<T> dataClass, DataConverter<R, T> transmuter) {
		ConduitEntranceController<T> ec = controller.createConduitEntrance(true, portName, new DataTemplate<T>(dataClass));

		Distance dt = getScale() == null ? Distance.ZERO : getScale().getDt();
		JNIConduitEntrance<R,T> e = new JNIConduitEntrance<R,T>(transmuter, jniClass, ec, originTime == null ? Timestamp.ZERO : originTime, dt);
		ec.setEntrance(e);
		addEntranceToList(portName, ec);

		return e;		
	}
	protected <T extends Serializable> JNIConduitEntrance<T, T> addJNIEntrance(String portName, Class<T> dataClass) {
		return addJNIEntrance(portName, dataClass, dataClass, new PipeConverter<T>());
	}

	/**
	returns path to a tmp directory for this kernel<br>
	do not change signature! (used from native code)
	 */
	public String getTmpPath() {
		File tmpDir = MiscTool.joinPaths(CxADescription.ONLY.getTmpRootPath(), OSTool.portableFileName(getLocalName(), ""));
		// create our kernel tmp dir if not already there
		tmpDir.mkdir();
		if (!tmpDir.isDirectory()) {
			getLogger().log(Level.SEVERE, "invalid tmp path <{0}> for kernel <{1}>", new Object[]{tmpDir, getLocalName()});
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

	protected Logger getLogger() {
		return Logger.getLogger(getClass().getName());
	}
	
	protected void log(String msg) {
		log(msg, Level.INFO);
	}

	protected void log(String msg, Level lvl) {
		getLogger().log(lvl, msg);
	}

	public void setArguments(Object[] args) {
		this.arguments = args;
	}

	public Object[] getArguments() {
		if (arguments == null) {
			return new Object[0];
		}

		return arguments;
	}
	
	public boolean hasProperty(String name) {
		return hasInstanceProperty(name) || hasGlobalProperty(name);
	}
	public boolean hasInstanceProperty(String name) {
		return CxADescription.ONLY.hasProperty(getLocalName() + ":" + name);
	}
	public boolean hasGlobalProperty(String name) {
		return CxADescription.ONLY.hasProperty(name);
	}
	
	public File getPathProperty(String name) {
		String lname = getLocalName() + ":" + name;
		if (CxADescription.ONLY.hasProperty(lname)) {
			return CxADescription.ONLY.getPathProperty(lname);
		} else if (CxADescription.ONLY.hasProperty(name)) {
			return CxADescription.ONLY.getPathProperty(name);
		} else {
			throw new NoSuchElementException("Property " + name + " does not exist in instance " + getLocalName());
		}
	}
	
	public String getProperty(String name) {
		String lname = getLocalName() + ":" + name;
		if (CxADescription.ONLY.hasProperty(lname)) {
			return CxADescription.ONLY.getProperty(lname);
		} else if (CxADescription.ONLY.hasProperty(name)) {
			return CxADescription.ONLY.getProperty(name);
		} else {
			throw new NoSuchElementException("Property " + name + " does not exist in instance " + getLocalName());
		}
	}
	
	
	public Object getRawProperty(String name) {
		String lname = getLocalName() + ":" + name;
		if (CxADescription.ONLY.hasProperty(lname)) {
			return CxADescription.ONLY.getRawProperty(lname);
		} else if (CxADescription.ONLY.hasProperty(name)) {
			return CxADescription.ONLY.getRawProperty(name);
		} else {
			throw new NoSuchElementException("Property " + name + " does not exist in instance " + getLocalName());
		}
	}
	
	public int getIntProperty(String name) {
		String lname = getLocalName() + ":" + name;
		if (CxADescription.ONLY.hasProperty(lname)) {
			return CxADescription.ONLY.getIntProperty(lname);
		} else if (CxADescription.ONLY.hasProperty(name)) {
			return CxADescription.ONLY.getIntProperty(name);
		} else {
			throw new NoSuchElementException("Property " + name + " does not exist in instance " + getLocalName());
		}
	}
	public double getDoubleProperty(String name) {
		String lname = getLocalName() + ":" + name;
		if (CxADescription.ONLY.hasProperty(lname)) {
			return CxADescription.ONLY.getDoubleProperty(lname);
		} else if (CxADescription.ONLY.hasProperty(name)) {
			return CxADescription.ONLY.getDoubleProperty(name);
		} else {
			throw new NoSuchElementException("Property " + name + " does not exist in instance " + getLocalName());
		}
	}
	public boolean getBooleanProperty(String name) {
		String lname = getLocalName() + ":" + name;
		if (CxADescription.ONLY.hasProperty(lname)) {
			return CxADescription.ONLY.getBooleanProperty(lname);
		} else if (CxADescription.ONLY.hasProperty(name)) {
			return CxADescription.ONLY.getBooleanProperty(name);
		} else {
			throw new NoSuchElementException("Property " + name + " does not exist in instance " + getLocalName());
		}
	}
	
	public File getGlobalPathProperty(String name) {
		return CxADescription.ONLY.getPathProperty(name);
	}
	
	public String getGlobalProperty(String name) {
		return CxADescription.ONLY.getProperty(name);
	}
	public int getGlobalIntProperty(String name) {
		return CxADescription.ONLY.getIntProperty(name);
	}
	public double getGlobalDoubleProperty(String name) {
		return CxADescription.ONLY.getDoubleProperty(name);
	}
	public boolean getGlobalBooleanProperty(String name) {
		return CxADescription.ONLY.getBooleanProperty(name);
	}
	
	// ==============MANAGEMENT====================//
	
	public void setInstanceController(InstanceController ic) {
		this.controller = ic;
	}
	
	
	/**
	prints info about a given kernel to stdout
	 */
	public static String info(Class<? extends RawInstance> controllerClass) {
		// instantiates a controller which is not intended to run as a JADE agent
		RawInstance kernel = null;
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
	
	
	public JNIMethod stopJNIMethod() {
		return new JNIMethod(this, "willStop");
	}
	
	// currently we can not add portals dynamically during runtime
	// add all portals here (and only here) at once
	public final void connectPortals() {
		acceptPortals = true;
		addPortals();
		acceptPortals = false;
	}

	public final void beforeExecute() {
		beforeSetup();
	}
	public final void start() {
		execute();
	}
	
	/**
	Returns null. helper method to create a scale object where dt is a multiple of the kernel scale
	* @deprecated use MML to specify this
	 */
	@Deprecated
	private Scale getPortalScale(int rate) {
		return null;
	}
	
	
	public String infoText() {
		StringBuilder sb = new StringBuilder(25*(1 + exits.size()+entrances.size()));
		sb.append(getLocalName()).append(": exits: ").append(exits.values()).append("; entrances: ").append(entrances.values());
		return sb.toString();
	}

	/**
	pass one or more controller class names to get an info about these controllers
	 */
	public static void main(String[] args) throws java.lang.ClassNotFoundException {

		if (args.length > 0) {
			ArrayList<Class<? extends RawInstance>> classes = new ArrayList<Class<? extends RawInstance>>();
			for (String arg : args) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends RawInstance> c = (Class<? extends RawInstance>) Class.forName(arg);
					classes.add(c);
				} catch (ClassCastException e) {
					Logger.getLogger(RawInstance.class.getName()).log(Level.SEVERE, "Class " + arg + " does not represent a RawKernel", e);
				}
			}

			for (Class<? extends RawInstance> c : classes) {
				System.out.println(RawInstance.info(c) + "\n");
			}

		}
	}
	
	private <T extends Serializable> void addExitToList(String portName, ConduitExitController<T> exit) {
		if (!acceptPortals) {
			throw new IgnoredException("adding of portals not allowed here");
		}
		// only add if not already added
		if (exits.containsKey(portName)) {
			throw new MUSCLERuntimeException("can not add exit twice <" + exit + ">");
		}
		exits.put(portName, exit);		
	}
	
	private <T extends Serializable> void addEntranceToList(String portName, ConduitEntranceController<T> entrance) {
		if (!acceptPortals) {
			throw new IgnoredException("adding of portals not allowed here");
		}
		// only add if not already added
		if (entrances.containsKey(portName)) {
			throw new MUSCLERuntimeException("can not add entrance twice <" + entrance + ">");
		}
		entrances.put(portName, entrance);		
	}
	
	@Deprecated
	protected <T extends Serializable, R> JNIConduitExit<T, R> addJNIExit(String newPortalName, int newRate, Class<T> newDataClass, Class<R> newJNIClass, DataConverter<R, T> newTransmuter) {
		return addJNIExit(newPortalName, newDataClass, newJNIClass, newTransmuter);
	}

	@Deprecated
	protected <T extends Serializable> JNIConduitExit<T, T> addJNIExit(String newPortalName, int newRate, Class<T> newDataClass) {
		return addJNIExit(newPortalName, newDataClass, newDataClass);
	}
	
	@Deprecated
	protected <T extends Serializable> ConduitExit<T> addExit(String newPortalName, int newRate, Class<T> newDataClass) {
		return addExit(newPortalName, newDataClass);
	}
	@Deprecated
	protected <T extends Serializable, R> JNIConduitEntrance<R, T> addJNIEntrance(String newPortalName, int newRate, Class<R> newJNIClass, Class<T> newDataClass, DataConverter<R, T> newTransmuter) {
		return addJNIEntrance(newPortalName, newJNIClass, newDataClass, newTransmuter);
	}

	@Deprecated
	protected <T extends Serializable> JNIConduitEntrance<T, T> addJNIEntrance(String newPortalName, int newRate, Class<T> newDataClass) {
		return addJNIEntrance(newPortalName, newDataClass);
	}
	
	@Deprecated
	protected <T extends Serializable> ConduitEntrance<T> addEntrance(String newPortalName, int newRate, Class<T> newDataClass) {
		return addEntrance(newPortalName, newDataClass);
	}
}
