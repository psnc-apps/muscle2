package muscle.utilities;

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Iterator;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import muscle.core.ConnectionScheme;
import muscle.core.CxADescription;

public class OTFLogger {
	// Begin the .otf file
	private native static int begin(String path);

	// Defines kernels and conduits lists
	private native static void define(String[] kernelArray, String[] conduitArray);

	// End the .otf file
	private native static void end();

	private native static void conduitBegin(int function, int process);

	private native static void conduitEnd(int function, int process);

	private native static void send(int from, int to, int size);

	private native static void receive(int from, int to, int size);

	// Source of information about kernels and conduits
	private ConnectionScheme d;
	
	// Number of kernels which ran OTFLogger
	private int kernelsRun = 0;
	
	// Lock for kernelsRun
	private Lock lockObject = new ReentrantLock();
	
	// Timer for time_limit
	private Timer timer;
	
	private Lock lockInstance = new ReentrantLock();
	
	// Singletone instance
	private static OTFLogger instance;	
	
	// OTFLogger settings
	private static boolean DEBUG = false;	
	private String DIR = "";
	private static boolean ENABLED = false;
	private static boolean closed = false;
	private static boolean TIMER_EXIT = false;
	
	private OTFLogger()
	{ 
		loadProperties();	
	}
	
	protected void finalize()
	{
		close();
		instance = null;
	}

	public synchronized static OTFLogger getInstance()
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
								throw new FileNotFoundException("Change otf logging directory");
				
						if (begin(DIR+"/"+kernel+".otf") != 0)
							throw new Exception("libotf Manager, writer or handlers failed");

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
	
	// Loads otf.properties file or sets default values
	private void loadProperties()
	{
		Properties properties = new Properties();
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
				
			if(properties.getProperty("timer_close","disabled").equals("enabled"))
				TIMER_EXIT = true;
			else
				TIMER_EXIT = false;
				
			int seconds = Integer.parseInt(properties.getProperty("time_limit","0"));
			
			log("OTFLogger.loadProperties dir "+DIR );	
			log("OTFLogger.loadProperties debug "+DEBUG );
			log("OTFLogger.loadProperties generate_otf "+ENABLED );		
			log("OTFLogger.loadProperties timer_close "+TIMER_EXIT );
			log("OTFLogger.loadProperties time_limit "+seconds);
			
			if(seconds != 0)
			{
				log("OTFLogger.loadProperties Running timer");
				TimerClose(seconds);
			}
		}
		else{
			DIR = "otf_files";
			DEBUG = false;
			ENABLED = false;
		}
	}

	// Close otf file when all other kernels have ended
	public void close() {
		if(ENABLED &&  !closed){
			synchronized(lockObject){					
					if (--kernelsRun == 0) {	
						ENABLED = false;
						closed = true;				
						log("OTFLogger close()");
						end();						
					}
			}
		}
	}
	
	// Close otf file immediately
	public void closeNow() {
		if(ENABLED &&  !closed){
			synchronized(lockObject){			
						ENABLED = false;	
						closed = true;		
						log("OTFLogger closeNow()");
						end();
			}
		}
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
	
	// Display all conduits and kernels
	private void displayConduitsKernels() {
		try {
			ConnectionScheme d = CxADescription.ONLY.getConnectionSchemeClass().newInstance();

			Iterator i = d.conduitList.iterator();
			while (i.hasNext()) {
				log("OTFLogger.displayConduitsKernels Conduit:" + i.next());
			}

			Iterator j = d.kernelList.iterator();
			while (j.hasNext()) {
				log("OTFLogger.displayConduitsKernels kernel:" + j.next());
			}

		} catch (Exception e) {
			System.err.println("OTFLogger.displayConduitsKernels error " + e.getMessage());
		}
	}
	
	// Gets conduit part of the sink index from conduits list
	private int getConduitIndex(String sink) {
		return d.conduitList.indexOf(sink) + 2;
	}


	// Get kernel part of the sink index from kernels list
	private int getKernelIndex(String sink) {
		return d.kernelList.indexOf(sink.substring(sink.indexOf("@") + 1)) + 2;
	}

	// Get entrace of given sink
	public String getEntrance(String sink) {
		if(ENABLED)
			return d.getEntrance(sink);
		else 
			return "";
	}

	// Gets size of given data
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
	
	// Log text to standard output
	private static void log(String text)
	{
		String dateFormat = "yyyy-MM-dd HH:mm:ss";
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		String time =  sdf.format(cal.getTime());
		
		if(ENABLED && DEBUG)
		{
			System.out.println(time+" "+text);
		}
	}

	// Logs to otf sending data from sender to receiver
	public void logSend(Object data, String sender, String receiver) {
		if(ENABLED) {
			log("OTFLogger.logSend " + getConduitIndex(sender) + " " + getConduitIndex(receiver) + " ( " + sender + " -> " + receiver + ")");
			send(getKernelIndex(sender), getKernelIndex(receiver), getSize(data));
		}
	}

	// Logs to otf receiving data from sender in receiver
	public void logReceive(Object data, String sender, String receiver) {
		if(ENABLED)	{
			log("OTFLogger.logReceive " + getConduitIndex(sender) + " " + getConduitIndex(receiver) + " ( " + sender + " -> " + receiver +")");
			receive(getKernelIndex(sender), getKernelIndex(receiver), getSize(data));
		}
	}
	
	// Starts timer which stops logging nad if specified closes
	// application after time_limit
	private void TimerClose(int seconds)
	{
		timer = new Timer();
		timer.schedule(new TimerCloseAction(), seconds * 1000); 
	}
	
	class TimerCloseAction extends TimerTask {
		public void run(){
			log("OTFLogger.TimerCloseAction");
			closeNow();					
			timer.cancel();
			if(TIMER_EXIT)
				System.exit(1);
		}
	}

	static {
		System.loadLibrary("muscle_utilities_OTFLogger");
	}

}
