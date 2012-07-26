package muscle.core.standalone;

import java.io.*;

public class StreamRipper extends Thread {
	private BufferedReader br;
	private PrintStream ps;
	
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
			ex.printStackTrace();
		}
	}

}
