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


import java.util.List;
import java.util.ArrayList;
import java.io.StreamTokenizer;
import java.net.URL;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;


/**
creates a multi dimensional double matrix from an input stream
@author Jan Hegewald
*/
public class MatrixReader {


	// we assume that all cols have the same length
	static public void parse(InputStream in, double[] ... cols) {

		if(cols.length == 0)
			return;
		
		Scanner scanner = new Scanner(new InputStreamReader(in));

		int colCount = cols.length;
		int rowCount = cols[0].length;
		for(int i = 0; scanner.hasNextDouble() && i < rowCount*colCount; i++) {				
			
			int c = i%colCount;
			int r = (i-c)%rowCount;
			cols[c][r] = scanner.nextDouble();
//			System.out.println(c+"/"+r+" "+cols[c][r]);
		}
	}

	
	// for testing
	public static void main(String[] args) throws java.io.FileNotFoundException {

		MatrixReader.parse(new FileInputStream(args[0]), new double[3], new double[3], new double[3], new double[3]);
	
//		Scanner scanner = new Scanner(new InputStreamReader(new FileInputStream(args[0])));
//		int i = 0;
//		while(scanner.hasNext())
//			System.out.println((i++)+" "+scanner.next());	
	}
}
