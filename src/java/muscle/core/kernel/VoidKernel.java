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

/**
kernel which does nothing, usefulf for testing purposes e.g. to use the QuitMonitor with a non empty CxA
@author Jan Hegewald
*/
public class VoidKernel extends muscle.core.kernel.RawKernel {


	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	//
	@Override
	public muscle.core.Scale getScale() {
		javax.measure.DecimalMeasure<javax.measure.quantity.Duration> dt = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.SECOND);
		javax.measure.DecimalMeasure<javax.measure.quantity.Length> dx = javax.measure.DecimalMeasure.valueOf(new java.math.BigDecimal(1), javax.measure.unit.SI.METER);
		return new muscle.core.Scale(dt,dx);
	}


	//
	@Override
	protected void addPortals() {
		// no portals
	}


	//
	@Override
	protected void execute() {
		// do nothing
	}

}
