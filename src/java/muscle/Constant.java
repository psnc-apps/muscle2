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

package muscle;


/**
various constants used within MUSCLE which can not be associated with a specific class
*/
public interface Constant {


	//
	public interface Text {

		public static final String COMMENT_INDICATOR = "#";

	}


	//
	public interface Filename {

		public static final String CXA_PROPERTIES = "cxa.properties.config";
		public static final String CXA_CONNECTION_SCHEME = "cxa.connection_scheme.config";
		public static final String CXA_KERNEL_BOOT_INFOS = "cxa.kernel.config";
		public static final String AGENT_INFO_PREFIX = "muscle.";
		public static final String AGENT_INFO_SUFFIX = ".txt";
		public static final String JVM_INFO = AGENT_INFO_PREFIX+"jvm"+AGENT_INFO_SUFFIX;

	}


	//
	public interface JADE {

		public static final String MAIN_CONTAINER = "Main-Container";
		public static final String LOCAL_PORT = "1099";
		public static final String RMA_AGENT = "rma";
		public static final String RMA_CLASS = "jade.tools.rma.rma";

	}


	//
	public interface Key {

		public static final String CONNECTION_SCHEME_CLASS = "CONNECTION_SCHEME_CLASS";
		public static final String ENTRANCE_INFO = "EntranceInfo";
		public static final String EXIT_INFO = "ExitInfo";
		public static final String TRACE_DATA_TRANSFER = "TRACE_DATA_TRANSFER";
	}


	//
	public interface Protocol {

		public static final String ANNOUNCE_ENTRANCE = "MUSCLE_ANNOUNCE_ENTRANCE";
		public static final String ANNOUNCE_EXIT = "MUSCLE_ANNOUNCE_EXIT";
		public static final String DATA_TRANSFER = "MUSCLE_DATA_TRANSFER";
		public static final String PORTAL_DETACH = "MUSCLE_PORTAL_DETACH";
		public static final String PORTAL_ATTACH = "MUSCLE_PORTAL_ATTACH";
		public static final String DOAGENT_RESULTS = "DOAGENT_RESULTS";
		public static final String ADMINISTRATIVE_CALL = "ADMINISTRATIVE_CALL";

	}


	//
	public interface Service {

		public static final String ENTRANCE = "MUSCLE_ENTRANCE_SERVICE";
		public static final String EXIT = "MUSCLE_EXIT_SERVICE";

	}

}
