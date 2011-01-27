package muscle.utilities;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import muscle.core.ConnectionScheme;
import muscle.core.CxADescription;

public class OTFLogger {
	// begin of the .orf file
	private native static int begin(String path);

	// Defines kernels and conduits lists
	private native static void define(String[] kernelArray, String[] conduitArray);

	// End of the .otf file
	private native static void end();

	private native static void conduitBegin(int functionm, int process);

	private native static void conduitEnd(int functionm, int process);

	private native static void send(int from, int to, int size);

	private native static void receive(int from, int to, int size);

	private ConnectionScheme d;

	private int kernelsRun = 0;

	private Properties properties;

	private Lock lockObject = new ReentrantLock();

	private static OTFLogger instance;
	
	private static boolean DEBUG = false;	
	private String DIR = "";
	private static boolean ENABLED = false;
	private static boolean closed = false;
	private OTFLogger()
	{ 
		loadProperties();
		
		log("Declaring signal handle");
      
		SignalHandler handler = new SignalHandler () {
			public void handle(Signal sig) {
			   System.out.println("Shutdown hook ran!");
						close();
						//System.exit(1);
			}
		};
		Signal.handle(new Signal("INT"), handler);
			
	}
	
	protected void finalize()
	{
		close();
		instance = null;
	}

	public static OTFLogger getInstance()
	{
		if(instance == null){	
			instance = new OTFLogger();
		}
			
		return instance;
	}
	
	public void init(String kernel) {	
		if(ENABLED)	{	
			log("OTFLogger.init invoke");			
			
			synchronized(lockObject) {
				 if(kernelsRun++ == 0) {
					log("OTFLogger init()");
					try {				
						d = CxADescription.ONLY.getConnectionSchemeClass().newInstance();
						d.generateLists();
						if(! (new File(DIR)).exists())
							if(!(new File(DIR)).mkdir())
								throw new FileNotFoundException("Change path");
				
						if (begin(DIR+"/"+kernel+".otf") != 0)
							throw new Exception("Manager, writer or handlers failed");

						String[] kernelArray = (String[]) d.kernelList.toArray(new String[d.kernelList.size()]);
						String conduitArray[] = (String[]) d.conduitList.toArray(new String[d.conduitList.size()]);

						define(kernelArray, conduitArray);
					} catch (Exception e) {
						System.err.println("OTFLogger.init error " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void close() {
		if(ENABLED &&  !closed){
			synchronized(lockObject){
					if (--kernelsRun == 0) {
						log("OTFLogger close()");
						end();
					}
			}
		}
	}

	public String getEntrance(String sink) {
		if(ENABLED)
			return d.getEntrance(sink);
		else 
			return "";
	}

	private int getSize(Object data) {
		int size = 0;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream stream = new ObjectOutputStream(bout);
			stream.writeObject(data);
			stream.close();
			size = bout.size();
		} catch (IOException e) {
			System.err.println("OTFLogger.getSize error: " + e.getMessage());
		}
		return size;
	}

	public void logSend(Object data, String sender, String receiver) {
		if(ENABLED) {
			log("OTFLogger.logSend " + getConduitIndex(sender) + " " + getConduitIndex(receiver) + " ( " + sender + " -> " + receiver + ")");
			send(getKernelIndex(sender), getKernelIndex(receiver), getSize(data));
		}
	}

	public void logReceive(Object data, String sender, String receiver) {
		if(ENABLED)	{
			log("OTFLogger.logReceive " + getConduitIndex(sender) + " " + getConduitIndex(receiver) + " ( " + sender + " -> " + receiver +")");
			receive(getKernelIndex(sender), getKernelIndex(receiver), getSize(data));
		}
	}

	private int getConduitIndex(String sink) {
		return d.conduitList.indexOf(sink) + 2;
	}

	private int getKernelIndex(String sink) {
		return d.kernelList.indexOf(sink.substring(sink.indexOf("@") + 1)) + 2;
	}

	public void conduitEnter(String sink) {
		if(ENABLED)
		{
			log("OTFLogger.conduitEnter " + sink + " " + getConduitIndex(sink) +" " +getKernelIndex(sink));
			conduitBegin(getConduitIndex(sink),getKernelIndex(sink));
		}
	}

	public void conduitLeave(String sink) {
		if(ENABLED) {		
			log("OTFLogger.conduiLeave " + sink + " " + getConduitIndex(sink) +" " +getKernelIndex(sink));
			conduitEnd(getConduitIndex(sink),getKernelIndex(sink));
		}
	}

	private void displayConduitsKernels() {
		try {
			ConnectionScheme d = CxADescription.ONLY.getConnectionSchemeClass().newInstance();

			Iterator i = d.conduitList.iterator();
			while (i.hasNext()) {
				System.out.println("Conduit:" + i.next());
			}

			Iterator j = d.kernelList.iterator();
			while (j.hasNext()) {
				System.out.println("kernel:" + j.next());
			}

		} catch (Exception e) {
			System.err.println("OTFLogger.displayConduitsKernels error " + e.getMessage());
		}
	}
	
	private static void log(String text)
	{
		if(ENABLED)
		{
			System.out.println(text);
		}
	}
	
	private void loadProperties()
	{
		properties = new Properties();
		File file = new File("otf.properties");	
		
		if(file.exists()){
			try{
				InputStream is = new FileInputStream(file);
				properties.load(is);
			}
			catch(Exception e){
				e.printStackTrace();
			}
					
			DIR = properties.getProperty("dir","otf_files");
			if(properties.getProperty("debug","off").equals("on"))
				DEBUG = true;
			else
				DEBUG = false;
			if(properties.getProperty("generate_otf","disabled").equals("enabled"))
				ENABLED = true;
			else
				ENABLED = false;
		}
		else{
			DIR = "otf_files";
			DEBUG = false;
		}
	}

	static {
		System.loadLibrary("muscle_utilities_OTFLogger");
	}

}
