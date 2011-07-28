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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
helper class to build source code strings/streams
@author Jan Hegewald
*/
public class CodeWriter extends PrintWriter {
	
	private int indentLevel = 0;
	private static final String nl = System.getProperty("line.separator");

	private CppStrategy strategy = new CppStrategy();
	
	//
	public CodeWriter(Writer out) {
		super(out);
	}
	
	
	//
	public CodeWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
	}
	
	
	//
	public CodeWriter(OutputStream out) {
		super(out);
	}
	
	
	//
	public CodeWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}
	
	
	//
	public CodeWriter(String fileName) throws FileNotFoundException {
		super(fileName);
	}
	
	
	//
	public CodeWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
	}
	
	
	//
	public CodeWriter(File file) throws FileNotFoundException {
		super(file);
	}
	
	
	//
	public CodeWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
	}


	//
	public void iadd(int il, String text) {
		
		print(indentPrefix(il)+text);
	}


	//
	public void add(String text) {
		
		iadd(indentLevel, text);
	}


	//
	public void addln(String ... lines) {
		
		for(String l : lines)
			addln(l);
	}


	//
	public void addln(String text) {
		
		println(indentPrefix(indentLevel)+text);
	}
	

	//
	public void icomment(int il, String text) {
		
		strategy.comment(indentPrefix(il)+text);
	}

	//
	public void comment(String text) {
		
		strategy.comment(indentPrefix(indentLevel)+text);
	}


	//
	public void doc(String ... lines) {
		
		strategy.doc(lines);
	}
	
	
	//
	public void begin() {
		
		addln("{");
		indentLevel ++;
	}

	//
	public void end() {
		
		end("}"+nl);
	}
	

	//
	public void end(String text) {
		
		indentLevel --;
		add(text);
	}

	
	
	
	//
	public void setStrategy(CppStrategy s) {
	
		strategy = s;
	}


	//
	private String indentPrefix(int indent) {
		
		String indentText = "";
		for(int i = 0; i < indent; i++)
			indentText += "\t";
		
		return indentText;
	}


	//
	public class CppStrategy {
	
		CodeWriter writer = CodeWriter.this;
		
		void comment(String raw) {
		
			writer.println("// "+raw);
		}

		void doc(String ... lines) {
		
			writer.println("/**");
			for(String l : lines)
				writer.println(l);
			writer.println("*/");
		}
	}


	// for testing purposes
	public static void main(String[] args) {
	
//		CodeWriter w = new CodeWriter(System.out);
		java.io.StringWriter out = new java.io.StringWriter();
		CodeWriter w = new CodeWriter(out);
		w.addln("foo");
		w.doc("bla", "\\author Jan Hegewald");
		
		w.close();
	
		System.out.print(out);
	}
}

