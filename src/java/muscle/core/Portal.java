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
import java.math.BigDecimal;
import java.util.logging.Logger;

import javatool.DecimalMeasureTool;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;

import muscle.core.kernel.RawKernel;
import muscle.exception.MUSCLERuntimeException;
import muscle.utilities.NullOutputStream;


//
public abstract class Portal<T> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

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

		this.portalID = newPortalID;
		this.ownerAgent = newOwnerAgent;
		this.rate = newRate;
		this.dataTemplate = newDataTemplate;

		// set custom time to 0
		this.customSITime = DecimalMeasure.valueOf(new BigDecimal(0), this.dataTemplate.getScale().getDt().getUnit());

		// we do not send message trace output by default
		this.setTraceOutputStream(new NullOutputStream());
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

		this.ownerAgent = newOwnerAgent;
	}



	//
	public void setTraceOutputStream(OutputStream traceOutput) {

		assert traceOutput != null;
		if(this.traceWriter != null) {
			try {
				this.traceWriter.close();
			} catch (IOException e) {
				throw new MUSCLERuntimeException(e);
			}
		}

		this.traceWriter = utilities.OutputStreamWriterTool.create(traceOutput);
	}


	// write a trace message
	public void trace(String text) {

		try {
			this.traceWriter.write(text);
			this.traceWriter.flush();
		} catch (IOException e) {
			throw new MUSCLERuntimeException(e);
		}
	}


	// remove this in favor of the close method?
	public void detachOwnerAgent() {

		if(this.traceWriter != null) {
			try {
				this.traceWriter.close();
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

		return this.portalID.getName();
	}


//	//
//	public String getStrippedName() {
//
//		return portalID.getStrippedName();
//	}


	//
	public PortalID getPortalID() {
		return this.portalID;
	}


	//
	public DataTemplate getDataTemplate() {
		return this.dataTemplate;
	}


	/**
	true if this portal does not pass data at a predefined rate (e.g. every iteration of the kernel)
	*/
	public boolean isLoose() {
		return this.rate == LOOSE;
	}


// temporary workaround to be able to use portals only once (their time will increment only once which makes it hard for the RawKernel to tell if it is still in use)
private boolean oneShot = false;
public void oneShot() {
	this.oneShot = true;
}

	// free our resources and disallow passing of messages
// TODO: switch to a NULL implementation after close (put current impl in a strategy and duplicate public interface of that strategy in the portal)
	private void close() {

		this.usedCount = Integer.MAX_VALUE;
	}


	/**
	current time of this portal in SI units
	*/
	public DecimalMeasure<Duration> getSITime() {

		if(this.rate == LOOSE) {
			// return dt*usedCount*rate
			return this.customSITime;
		}
		else {
			// return dt*usedCount*rate
			return DecimalMeasureTool.multiply(this.dataTemplate.getScale().getDt(), new BigDecimal(this.usedCount*this.rate));
		}
	}


	//
	@Override
	public boolean equals(Object obj) {

		if( this.getClass().isInstance(obj) ) {
			return ((Portal)obj).getLocalName().equals(this.getLocalName());
		}
		return super.equals(obj);
	}


	//
	@Override
	public String toString() {

		return this.getLocalName()+" used: "+this.usedCount+" scale: "+this.dataTemplate.getScale()+" SI time: "+this.getSITime();
	}


	//
	void increment() {

		this.usedCount ++;
		if(this.oneShot) {
			this.close();
		}
	}


	// deserialize
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

		// do default deserialization
		in.defaultReadObject();

		Logger logger = muscle.logging.Logger.getLogger(this.getClass());
		// init transient fields
		this.traceWriter = utilities.OutputStreamWriterTool.create(new NullOutputStream());
		logger.finest(this.getClass().getName()+" initialized OutputStream <traceWriter> with a NullOutputStream after deserialization");
	}

}
