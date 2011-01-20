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

package javatool;

import java.math.BigDecimal;

import javax.measure.DecimalMeasure;
import javax.measure.quantity.Quantity;


/**
additional functionality for javax.measure.DecimalMeasure
@author Jan Hegewald
*/
public class DecimalMeasureTool {


	/**
	add two DecimalMeasure, result has the unit of the first passed argument
	*/
	public static <Q extends Quantity> DecimalMeasure<Q> add(DecimalMeasure<Q> a, DecimalMeasure<Q> b) {

		return new DecimalMeasure<Q>(a.getValue().add(b.to(a.getUnit()).getValue()), a.getUnit());
	}


	/**
	multiply a DecimalMeasure with a factor
	*/
	public static <Q extends Quantity> DecimalMeasure<Q> multiply(DecimalMeasure<Q> val, BigDecimal factor) {

		return new DecimalMeasure<Q>(val.getValue().multiply(factor), val.getUnit());
	}

}



