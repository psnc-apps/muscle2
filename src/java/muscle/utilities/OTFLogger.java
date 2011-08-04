package muscle.utilities;

import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
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
	private native static int define(String[] kernelArray, String[] conduitArray);

	// End the .otf file
	private native static int end();

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
	private static boolean debug = false;	
	private String DIR = "";
	private static boolean enabled = false;
	private static boolean closed = false;
	private static boolean timer_exit = false;
	private static boolean libotf_not_found = false;
	
	private static Logger logger;
	
	private OTFLogger()
	{ 
		logger = muscle.logging.Logger.getLogger(this.getClass());		
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
	
	public synchronized void init(String kernel) {	
		if(enabled)	{	
			if(kernelsRun++ == 0) {
				log("init");			
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

					if(define(kernelArray, conduitArray) != 0)
						throw new Exception("could not allocate memory");						
				} catch (Exception e) {
					closeNow();		
					logger.severe(OTFLogger.class.getName()+" "+ e.getMessage());
					e.printStackTrace();
				}
			}			
		}
	}
	
	// Loads otf.properties file or sets default values
	private void loadProperties()
	{
		File file;
		Properties properties = new Properties();
		String path = System.getProperty("muscle.otf.conf");
		
		if(path != null && (file = new File(path)).exists()){
			try{
				InputStream is = new FileInputStream(file);
				properties.load(is);
				is.close();			
					
				DIR = properties.getProperty("dir","otf_files");
				
				if(properties.getProperty("generate_otf","disabled").equals("enabled"))
					enabled = true;
				else
					enabled = false;
				
				if(enabled && libotf_not_found)
				{
					enabled = false;
					logger.severe(OTFLogger.class.getName()+".loadProperties libotf not available!");
				}			
				
				if(libotf_not_found)
					return;
				
				if(properties.getProperty("debug","off").equals("on"))
					debug = true;
				else
					debug = false;	
					
				if(properties.getProperty("timer_close","disabled").equals("enabled"))
					timer_exit = true;
				else
					timer_exit = false;
					
				int seconds = Integer.parseInt(properties.getProperty("time_limit","0"));
				
				log("loadProperties dir "+DIR );	
				log("loadProperties debug "+debug );
				log("loadProperties generate_otf "+enabled );		
				log("loadProperties timer_close "+timer_exit );
				log("loadProperties time_limit "+seconds);
				
				if(seconds != 0)
				{
					log("loadProperties Running timer");
					TimerClose(seconds);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	// Close otf file when all other kernels have ended
	public synchronized void close() {
		if(enabled &&  !closed){
			if (--kernelsRun == 0) {	
				closeCore();
			}
		}
	}
	
	// Close otf file immediately
	public synchronized void closeNow() {
		if(enabled &&  !closed){
			closeCore();
		}
	}
	
	private void closeCore()
	{
		enabled = false;	
		closed = true;		
		log("OTFLogger closeNow()");
		if(end() != 0)
			logger.severe(OTFLogger.class.getName()+" log path doeas not exists. Otf will not be saved.");
	}
	
	public synchronized void conduitEnter(String sink) {
		if(enabled)
		{
			log("conduitEnter" + sink + " " + getConduitIndex(sink) +" " +getKernelIndex(sink));
			conduitBegin(getConduitIndex(sink),getKernelIndex(sink));
		}
	}

	public synchronized void conduitLeave(String sink) {
		if(enabled) {		
			log("conduitLeave" + sink + " " + getConduitIndex(sink) +" " +getKernelIndex(sink));
			conduitEnd(getConduitIndex(sink),getKernelIndex(sink));
		}
	}
	
	// Display all conduits and kernels
	private void displayConduitsKernels() {
		try {
			ConnectionScheme d = CxADescription.ONLY.getConnectionSchemeClass().newInstance();

			Iterator i = d.conduitList.iterator();
			while (i.hasNext()) {
				log("displayConduitsKernels Conduit:" + i.next());
			}

			Iterator j = d.kernelList.iterator();
			while (j.hasNext()) {
				log("displayConduitsKernels kernel:" + j.next());
			}

		} catch (Exception e) {
			logger.severe(OTFLogger.class.getName()+" "+ e.getMessage());
		}
	}
	
	// Gets conduit part of the sink index from conduits list
	private int getConduitIndex(String sink) {
		return d.conduitList.indexOf(sink) + 1;
	}


	// Get kernel part of the sink index from kernels list
	private int getKernelIndex(String sink) {
		return d.kernelList.indexOf(sink.substring(sink.indexOf("@") + 1)) + 1;
	}

	// Get entrace of given sink
	public String getEntrance(String sink) {
		if(enabled)
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
			logger.severe(OTFLogger.class.getName()+" "+e.getMessage());
		}
		return size;
	}
	
	// Log text to standard output
	private static void log(String text)
	{		
		if(enabled && debug)
			logger.finest(OTFLogger.class.getName()+"."+text);
	}

	// Logs to otf sending data from sender to receiver
	public synchronized void logSend(Object data, String sender, String receiver) {
		if(enabled) {
			log("logSend " + getConduitIndex(sender) + " " + getConduitIndex(receiver) + " ( " + sender + " -> " + receiver + ")");
			send(getKernelIndex(sender), getKernelIndex(receiver), getSize(data));
		}
	}

	// Logs to otf receiving data from sender in receiver
	public synchronized void logReceive(Object data, String sender, String receiver) {
		if(enabled)	{
			log("logReceive " + getConduitIndex(sender) + " " + getConduitIndex(receiver) + " ( " + sender + " -> " + receiver +")");
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
			if(timer_exit)
				System.exit(1);
		}
	}

	static {
		try{
			System.loadLibrary("muscle_utilities_OTFLogger");
		}
		catch(UnsatisfiedLinkError e)
		{
			libotf_not_found = true;
		}
	}

}
