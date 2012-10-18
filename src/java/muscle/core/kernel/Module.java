/*
 * 
 */

package muscle.core.kernel;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.core.CxADescription;
import muscle.util.FileTool;

/**
 *
 * @author Joris Borgdorff
 */
public abstract class Module {
	private final static Logger logger = Logger.getLogger(Module.class.getName());
	
	protected String name = null;
	
	public void beforeExecute() {};
	
	/**
	 * Get the local name of the current kernel.
	*/

	public String getLocalName() {
		return name;
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
	
	
	public String getTmpPath() {
		File tmpDir = FileTool.joinPaths(CxADescription.ONLY.getTmpRootPath(), FileTool.portableFileName(getLocalName(), ""));
		// create our kernel tmp dir if not already there
		tmpDir.mkdir();
		if (!tmpDir.isDirectory()) {
			logger.log(Level.SEVERE, "invalid tmp path <{0}> for kernel <{1}>", new Object[]{tmpDir, getLocalName()});
		}

		return tmpDir.getPath();
	}
}
