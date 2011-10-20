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

package examples.transmutable;

import muscle.core.DataTemplate;
import muscle.core.Scale;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.JNIConduitEntrance;
import muscle.core.JNIConduitExit;
import muscle.core.EntranceDependency;
import muscle.core.CxADescription;
import muscle.core.PortalID;
import muscle.exception.MUSCLERuntimeException;
import utilities.jni.JNIMethod;
import utilities.MiscTool;
import javatool.ArraysTool;
import utilities.Transmutable;
import utilities.PipeTransmuter;
import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;


/**
example of a kernel which is using native code to send and receive data to itself<br>
this example uses a Transmutable to map cpp data to a Java class
@author Jan Hegewald
*/
public class Kernel extends muscle.core.kernel.CAController {

	//
	static {
		System.loadLibrary("transmutable_lib");
	}


	private JNIConduitEntrance<double[],String> entrance;
	private JNIConduitExit<String,double[]> exit;
	

	private native void callNative(int length, JNIConduitExit exitJref, JNIConduitEntrance entranceJref);
	

	//
	public muscle.core.Scale getScale() {
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1), SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1), SI.METER);
		return new Scale(dt,dx);
	}

	
	//
	public void addPortals() {

		entrance = addJNIEntrance("writer", 1, double[].class, String.class, new DA2S());

		exit = addJNIExit("reader", 1, String.class, double[].class, new S2DA());
	}


	class S2DA implements Transmutable<String,double[]> {
		public double[] transmute(String in) {
			double[] out = new double[Integer.valueOf(in)];
			out[0] = Double.valueOf(in);
			out[Integer.valueOf(in)-1] = -Double.valueOf(in);
			return out;
		}
	}
double[] gin;
	class DA2S implements Transmutable<double[],String> {
		public String transmute(double[] in) {
gin = in;
for(int i = 0; i < gin.length; i++)
	System.out.println(i+": "+gin[i]);		

			return new String(""+in.length);
		}
	}


	//
	protected void execute() {

	Object[] args = getArguments();
	if(args.length > 0) {
		System.out.println("first kernel arg: "+(String)args[0]);
	}

	
		int length = 4;

		callNative(length, exit, entrance);	
	}	

}
