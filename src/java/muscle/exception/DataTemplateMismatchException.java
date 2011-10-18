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

package muscle.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;


/**
custom exception
@author Jan Hegewald
*/
public class DataTemplateMismatchException extends Exception {
//		public DataTemplateMismatchException() {
//			
//		}
		public DataTemplateMismatchException(String message) {
			super(message);
		}
//		public DataTemplateMismatchException(Throwable cause) {
//			super(cause);
//		}
//		public DataTemplateMismatchException(String message, Throwable cause) {
//			super(message, cause);
//		}

}

