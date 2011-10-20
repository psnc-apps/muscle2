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

package utilities;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;

/**
provies utility methods regarding output streams
@author Jan Hegewald
*/
public class OutputStreamWriterTool {


	/**
	create a custom OutputStreamWriter where the close method is disabled if underlying stream is System.out or System.err since we do not want to close those
	*/
	public static OutputStreamWriter create(OutputStream out) {

		if(out.equals(System.out) || out.equals(System.err)) {
			return new OutputStreamWriter(out) {		
				public void close() throws IOException {
				}
			};
		}
		
		return new OutputStreamWriter(out);
	}

}