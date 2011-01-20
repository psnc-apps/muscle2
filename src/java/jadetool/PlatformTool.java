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

package jadetool;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/**
utility methods to ease the usage of JADE
@author Jan Hegewald
*/
public class PlatformTool {


	/**
	this will start an additional thread
	which kills the JVM with exit status 0 on failure if setCloseVM(true) was set<br>
	a failure will occur if there is already a Main-Container listening on 'port'
	*/
	public static void launchMainContainer(boolean closeJVM, int port) {

		// get a handle to JADE
		Runtime rt = Runtime.instance();

		// Exit the JVM when there are no more containers around
		rt.setCloseVM(closeJVM);

		// create a default Profile
		Profile profile = new ProfileImpl(null, port/*use -1 for default port*/, null/*use default platform name*/);

		rt.createMainContainer(profile);
	}


	//
	public static void main(String[] args) {

		int port = 1099;

		// try to read cli args
		if(args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e) {
			}
		}


		PrintStream originalOut = System.out;
		PrintStream originalErr = System.err;
		// be silent
		ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outBuffer));
		System.setErr(new PrintStream(errBuffer));

		Thread.currentThread();
		PlatformTool.launchMainContainer(false, port);

		// reactivate default out/err streams
		System.setOut(originalOut);
		System.setErr(originalErr);
		try {
			outBuffer.close();
			errBuffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// see if an error occured
		if( errBuffer.toString().indexOf("No ICP active") > -1 ) {
			java.lang.Runtime.getRuntime().exit(1); // an error occured
		}

		// kill the running Main-Container and this JVM
		assert Thread.activeCount() > 1; // else the Main-Container is not running
		java.lang.Runtime.getRuntime().exit(0);
	}

}