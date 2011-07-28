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

package muscle.core;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.logging.Logger;
import muscle.core.kernel.RawKernel;
import muscle.utilities.NullOutputStream;
import muscle.exception.MUSCLERuntimeException;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import java.math.BigDecimal;
import javatool.DecimalMeasureTool;


//
public abstract class Portal<T> implements Serializable {
	
	public static final int LOOSE = -1; // if there is no rate accociated with this portal
	
	transient RawKernel ownerAgent;
	
	private PortalID portalID;
	private DataTemplate dataTemplate;
	private int usedCount;
	private int rate;
	private DecimalMeasure<Duration> customSITime;
	
	transient private OutputStreamWriter traceWriter;


	//
	Portal(PortalID newPortalID, RawKernel newOwnerAgent, int newRate, DataTemplate newDataTemplate) {
		
		portalID = newPortalID;
		ownerAgent = newOwnerAgent;
		rate = newRate;
		dataTemplate = newDataTemplate;
		
		// set custom time to 0
		customSITime = DecimalMeasure.valueOf(new BigDecimal(0), dataTemplate.getScale().getDt().getUnit());
		
		// we do not send message trace output by default
		setTraceOutputStream(new NullOutputStream());
	}


//	//
//	Portal(PortalID newPortalID, DataTemplate newDataTemplate) {
//		
//		// we do not send message trace output by default
//		this(newPortalID, null, newDataTemplate, new NullOutputStream());
//	}
//	//
//	Portal(PortalID newPortalID, AID newControllerID, DataTemplate newDataTemplate) {
//		
//		// we do not send message trace output by default
//		this(newPortalID, newControllerID, newDataTemplate, new NullOutputStream());
//	}
//	//
//	Portal(PortalID newPortalID, AID newControllerID, DataTemplate newDataTemplate, OutputStream newTraceOutput) {
//		
//		portalID = newPortalID;
//		dataTemplate = newDataTemplate;
//		
//		dt = newDataTemplate.getScale().getDt();
//		
//		setTraceOutputStream(newTraceOutput);
//	}
	

	/**
	if a portal is deserialized, we need to attach it to the current owner agent
	*/
	public void setOwner(RawKernel newOwnerAgent) {
		
		ownerAgent = newOwnerAgent;
	}


	
	//
	public void setTraceOutputStream(OutputStream traceOutput) {
		
		assert traceOutput != null;		
		if(traceWriter != null) {
			try {
				traceWriter.close();
			} catch (IOException e) {
				throw new MUSCLERuntimeException(e);
			}
		}

		traceWriter = utilities.OutputStreamWriterTool.create(traceOutput);
	}


	// write a trace message
	public void trace(String text) {
	
		try {
			traceWriter.write(text);
			traceWriter.flush();
		} catch (IOException e) {
			throw new MUSCLERuntimeException(e);
		}
	}
	
	
	// remove this in favor of the close method?
	public void detachOwnerAgent() {	

		if(traceWriter != null) {
			try {
				traceWriter.close();
			} catch (IOException e) {
				throw new MUSCLERuntimeException(e);
			}
		}		
	}
	

	//
//	public void assertTimestep(int t) {
//	
////		if(timestep != t)
////			throw new MUSCLERuntimeException("timestep mismatch: <"+timestep+"> vs <"+t+">");
//	}


	//
	public String getLocalName() {

		return portalID.getName();
	}


//	//
//	public String getStrippedName() {
//	
//		return portalID.getStrippedName();
//	}


	//
	public PortalID getPortalID() {
		return portalID;
	}


	//
	public DataTemplate getDataTemplate() {
		return dataTemplate;
	}
	
	
	/**
	true if this portal does not pass data at a predefined rate (e.g. every iteration of the kernel) 
	*/
	public boolean isLoose() {
		return rate == LOOSE;
	}
	
	
// temporary workaround to be able to use portals only once (their time will increment only once which makes it hard for the RawKernel to tell if it is still in use)
private boolean oneShot = false;
public void oneShot() {
	oneShot = true;
}

	// free our resources and disallow passing of messages
// TODO: switch to a NULL implementation after close (put current impl in a strategy and duplicate public interface of that strategy in the portal)
	private void close() {

		usedCount = Integer.MAX_VALUE;
	}


	/**
	current time of this portal in SI units
	*/
	public DecimalMeasure<Duration> getSITime() {
	
		if(rate == LOOSE) {
			// return dt*usedCount*rate
			return customSITime;
		}
		else {
			// return dt*usedCount*rate
			return DecimalMeasureTool.multiply(dataTemplate.getScale().getDt(), new BigDecimal(usedCount*rate));
		}
	}


	//
	public boolean equals(Object obj) {
		
		if( getClass().isInstance(obj) ) {
			return ((Portal)obj).getLocalName().equals(getLocalName());
		}
		return super.equals(obj);
	}
	
	
	//
	public String toString() {

		return getLocalName()+" used: "+usedCount+" scale: "+dataTemplate.getScale()+" SI time: "+getSITime();
	}


	//
	void increment() {
		
		usedCount ++;
		if(oneShot)
			close();
	}


	// deserialize
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		// do default deserialization
		in.defaultReadObject();
		
		Logger logger = muscle.logging.Logger.getLogger(getClass());
		// init transient fields
		traceWriter = utilities.OutputStreamWriterTool.create(new NullOutputStream());
		logger.finest(getClass().getName()+" initialized OutputStream <traceWriter> with a NullOutputStream after deserialization");
	}
	
}
