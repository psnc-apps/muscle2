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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.lang.reflect.Array;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
static methods for miscellaneous stuff
@author Jan Hegewald
*/
public class MiscTool {

	private static final String ARRAY_SEPARATOR = "\t";
	public static final double COMPARE_THESHOLD = 1e-15;


	/**
	amount of installed RAM on the host machine (physical memory) in bytes
	*/
	static public long getRAMSize() {
		
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		if ( !(osBean instanceof com.sun.management.OperatingSystemMXBean) ) {
			throw new java.lang.UnsupportedOperationException("can not determine ram size because there is not a <com.sun.management.OperatingSystemMXBean>, but a <"+osBean.getClass()+">");
		}
		
		return ((com.sun.management.OperatingSystemMXBean)osBean).getTotalPhysicalMemorySize();
	}


	//
	static public String toString(Object item) {
	
		XStream xstream = new XStream();
		return xstream.toXML(item);
	}


	/**
	converts an e.g. Object[] to a String[]
	*/
	public static <T> String[] toStringArray(T array) {

		if( !array.getClass().isArray() )
			throw new IllegalArgumentException("arg must be a C style array, e.g. Object[]");
		
		String[] strings = new String[Array.getLength(array)];		
		for(int i = 0; i < strings.length; i++)
			strings[i] = (String)Array.get(array, i);
		
		return strings;
	}


	/**
	automatically compares all fields of two objects,
	throws IllegalAccessException if a field can not be compared because it is private
	*/
	static public boolean equals(Object a, Object b) throws IllegalAccessException {
		
		if( a == null || b == null || !a.getClass().isInstance(b) )
			return false;
		
		java.lang.reflect.Field[] aFields = a.getClass().getDeclaredFields();
		LinkedList<java.lang.reflect.Field> bFields = new LinkedList<java.lang.reflect.Field>((List<java.lang.reflect.Field>)Arrays.asList(b.getClass().getDeclaredFields()));
		for(java.lang.reflect.Field af : aFields) {
			if( af.isSynthetic())
				continue;
			int index = bFields.indexOf(af);
			if( index == -1 )
				return false;
			java.lang.reflect.Field bf = bFields.get(index);
			if( !af.equals(bf) )
				return false;
			Object aValue = af.get(a);
			Object bValue = bf.get(b);
			if( aValue != null && !aValue.equals(bValue) )
				return false;
			else if( aValue == null && bValue != null )
				return false;
		
			bFields.remove(index);
		}
		
		for(java.lang.reflect.Field bf : bFields) {
			if( !bf.isSynthetic())
				return false; // b has more fields than a
		}
		
		return true;
	}


	//
	public static String pwd() {
		return System.getProperty("user.dir");
	}
	

   /**
	serialize an object
	*/
	static public byte[] serialize(Serializable object) {

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream out;
      try {
      out = new ObjectOutputStream(byteStream);
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		try {
			// write object to the byteStream
			out.writeObject(object);
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				out.close();
			}
			catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
			try {
				byteStream.close();
			}
			catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		return byteStream.toByteArray();
	}


	/**
   deserialize an object from given byte array
	*/
	static public <T> T deserialize(byte[] data) {
				
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

		ObjectInputStream in;
      try {
            in = new ObjectInputStream(byteStream);
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}

		// read an object from the byteStream
		try {
         return (T)in.readObject();
		}
		catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		catch (java.lang.ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}


	//
	public static String hostName() {

		String hostname = null;
		try {
			java.net.InetAddress addr = java.net.InetAddress.getLocalHost();

			// Get IP Address
			byte[] ipAddr = addr.getAddress();

			// Get hostname
			hostname = addr.getHostName();
		}
		catch (java.net.UnknownHostException e) {
			e.printStackTrace();
		}

		return hostname;
	}


	//
	public static boolean equalObjectValues(Object valA, Object valB) {
		
		return equalObjectValues(valA, valB, COMPARE_THESHOLD);
	}
	
	
	//
	public static boolean equalObjectValues(Object valA, Object valB, final double threshold) {

		if( valA == null || valB == null )
			throw new NullPointerException();
		
		if(valA instanceof Number && valB instanceof Number)
			return Math.abs(((Number)valA).doubleValue()-((Number)valB).doubleValue()) <= threshold; // allow threshold to be zero

		else if(valA instanceof Boolean && valB instanceof Boolean)
			return valA.equals(valB);
			
		return false;
	}


	//
	public static List<String> stringToList(String text) {
		
		List<String> list = new LinkedList<String>();
		String[] items = text.split(ARRAY_SEPARATOR);

		for(String s : items) {
			list.add(s);
		}

		return list;
	}
	
	
	//
	public static String listToString(List<String> list) {
	
		return joinItems(list, ARRAY_SEPARATOR);
	}
	
	
	//
	public static <T> String joinItems(T[] array, String separator) {

		return joinItems(Arrays.asList(array), separator);
	}
	

	//
	public static <A> String joinItems(List<A> list, String separator) {

		StringBuilder joined = null;
		
		for( A item : list ) {
			if( joined == null ) {
				joined = new StringBuilder(item.toString());
			}
			else {
				joined.append(separator);
				joined.append(item.toString());
			}
		}
		
		return joined == null ? null : joined.toString();
	}
	
	
	//
	public static String joinPaths(String ... paths) {

		StringBuilder joined = null;
		
		for( String item : paths ) {
			
			if( joined == null ) {
				joined = new StringBuilder(item);
			}
			else {
				// insert separator char only once
				if( !joined.substring(joined.length()-1).equals(System.getProperty("file.separator")) )
					joined.append(System.getProperty("file.separator"));

				if( item.startsWith(System.getProperty("file.separator")) ) {
					if(item.length() > 1)
						joined.append(item.substring(1));
				}
				else
					joined.append(item);
			}
		}
		
		return joined == null ? null : joined.toString();
	}
	
	
	/**
	try to expand tilde at beginning of path
	returns the unmodified path if there is no tilde at the beginning
	*/
	public static String resolveTilde(String path) {

		final String homePrefix = "~"+System.getProperty("file.separator");
		if(path.startsWith(homePrefix)) {
			return MiscTool.joinPaths(System.getProperty("user.home"), path.substring(homePrefix.length()));
		}
		else if(path.equals("~")) {
			return System.getProperty("user.home");
		}
		
		return path;
	}


	//
	public static String fileToString(File file) throws IOException, java.io.FileNotFoundException {

		StringBuilder fileData = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead;
		while( (numRead = reader.read(buf) ) != -1) {				
			fileData.append(buf, 0, numRead);
		}
		
		reader.close();
		
		return fileData.toString();
	}


	//
	public static String fileToString(File file, String commentIndicator) throws IOException, java.io.FileNotFoundException {

		StringBuilder fileData = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line;
		while( (line = reader.readLine() ) != null) {
			if( line.trim().startsWith(commentIndicator) == false ) {
				fileData.append(line);
				fileData.append("\n");
			}
		}
		
		reader.close();
		
		return fileData.toString();
	}


	/**
	returns true if any of the passed objects is null
	*/
	public static boolean anyNull(Object ... objects) {
					
		for(Object o : objects) {		
			if(o == null)
				return true;
		}
		
		return false;
	}

	
	/**
	returns true if any of the passed objects is equal to the reference object
	*/
	public static boolean anyOf(Object reference, Object ... objects) {
					
		for(Object o : objects) {		
			if(reference != null && reference.equals(o))
				return true;
			else if(reference == null && o == null)
				return true;
		}
		
		return false;
	}
	
	
	//
	public static String[] getAbsolutePathsFromDir(String directory, String pattern) throws IOException{

		String[] paths = getNamesFromDir(directory, pattern);
		for(int j = 0; j < paths.length; j++)
			paths[j] = directory+System.getProperty("file.separator")+paths[j];

		return paths;
	}


	// returns all filenames of a given directory which mach given Regex (in sorted order)
	public static String[] getNamesFromDir(String directory, String pattern) throws IOException{

		class PatternFileFilter implements FilenameFilter{
			String regex;
			public PatternFileFilter(String pattern) {
				regex = pattern;
			}
         @Override
			public boolean accept(File directory, String name) {
				if(name.matches(regex)) return true;
				return false;
			}

		}

		File dir = new File(directory);
		String[] files;
		if(dir.isDirectory()) {
			files = dir.list(new PatternFileFilter(pattern));
		} else {
			throw new IOException("<"+dir.toString()+"> is not a directory");
		}

		class MyStringComparator implements Comparator<String> {
         @Override
			public int compare(String o1, String o2) {
				int a, b;
				Pattern pattern = Pattern.compile("(\\D*)(\\d+)(.*)");
				Matcher matcherOne = pattern.matcher(o1);
				Matcher matcherTwo = pattern.matcher(o2);

				if(matcherOne.matches() && matcherTwo.matches() && matcherOne.group(1).equals(matcherTwo.group(1)) && matcherOne.group(3).equals(matcherTwo.group(3))) {
					a = Integer.parseInt(matcherOne.group(2));
					b = Integer.parseInt(matcherTwo.group(2));
					return (a-b);
				}

			    return ((String)o1).compareTo( (String)o2 );
			}
		}

		Arrays.sort(files, new MyStringComparator());
		return files;
	}

	
	
//	public static byte[] gzip(java.io.Serializable object) {
//		try {
//			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
//			GZIPOutputStream gzOut = new GZIPOutputStream(bOut); // this uses a DeflaterOutputStream with new Deflater(Deflater.DEFAULT_COMPRESSION, true)
//			ObjectOutputStream objectOut = new ObjectOutputStream(gzOut);
//			objectOut.writeObject(object);
//			gzOut.finish();
//			bOut.flush();
//			bOut.close();
//
//			return bOut.toByteArray();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public static <T> T gunzip(byte[] bytes) {
//		try {
//			ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
//			GZIPInputStream gzIn = new GZIPInputStream(bIn);
//			ObjectInputStream oIn = new ObjectInputStream(gzIn);
//			return (T)oIn.readObject();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}


	//
	static public class NotEqualException extends RuntimeException {
		public NotEqualException() {
			super();
		}
		public NotEqualException(String message) {
			super(message);
		}
		public NotEqualException(Throwable cause) {
			super(cause);
		}
		public NotEqualException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	//
//	public static void main (String args[]) {
//
//	}
}
