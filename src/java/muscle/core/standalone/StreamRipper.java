package muscle.core.standalone;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamRipper extends Thread {
	private BufferedReader br;
	private PrintStream ps;
	private final static Logger logger = Logger.getLogger(StreamRipper.class.getName());
	
	public StreamRipper(String name, PrintStream ps, InputStream is) {
		super(name);
		this.ps = ps;
		br = new BufferedReader(new InputStreamReader(is));
	}
	
	public void run() {
		String line;
		
		try {
			while ((line = br.readLine()) != null) {
				ps.println(line);
			}
		} catch (IOException ex) {
			logger.log(Level.WARNING, this.getName() + " no longer capturing output of process", ex);
		}
	}
}
