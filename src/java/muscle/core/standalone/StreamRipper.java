package muscle.core.standalone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;


public class StreamRipper extends Thread {
	

	private BufferedReader br;
	private PrintStream ps;
	
	public StreamRipper(PrintStream ps, InputStream is) {
		this.ps = ps;
		br = new BufferedReader(new InputStreamReader(is));
	}
	
	public void run() {
		String line = null;
		
		try {
		
			while ((line = br.readLine()) != null) {
				ps.println(line);
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
