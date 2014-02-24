package muscle.util.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import muscle.id.Location;
import muscle.util.JVM;

/**
 *
 * @author joris
 */
public class PlainActivityLogger extends ActivityWriter {
	private final Writer wr;
	private final Location loc;
	private final String locHash;
	
	public PlainActivityLogger(Location loc) throws IOException {
		this(loc, JVM.ONLY.tmpFile("activity.log"));
	}
	
	public PlainActivityLogger(Location loc, File f) throws IOException {
		wr = new BufferedWriter(new FileWriter(f));
		this.loc = loc;
		this.locHash = Integer.toHexString(loc.hashCode()) + " ";
	}

	@Override
	protected synchronized void write(ActivityProtocol action, String id, int sec, int nano) throws IOException {
		wr.write(locHash);
		wr.write(String.valueOf(sec));
		writeNano(nano);
		wr.write(' ');
		wr.write(action.name());
		wr.write(':');
		wr.write(id);
		wr.write('\n');
		wr.flush();
	}

	@Override
	public void dispose(int sec, int nano) throws IOException {
		wr.write(locHash);
		wr.write(String.valueOf(sec));
		writeNano(nano);
		wr.write(" FINALIZE\n");
		wr.flush();
		wr.close();
	}

	@Override
	protected void init(long sec, int milli) throws IOException {
		wr.write(locHash);
		wr.write(String.valueOf(sec));
		writeMilli(milli);
		wr.write(" INIT ");
		wr.write(loc.toString());
		wr.write('\n');
		wr.flush();
	}
	
	private void writeMilli(int milli) throws IOException {
		wr.write('.');
		String milliStr = String.valueOf(milli);
		for (int i = milliStr.length(); i < 3; i++) { wr.write('0'); }
		wr.write(milliStr);		
	}
	
	private void writeNano(int nano) throws IOException {
		wr.write('.');
		String nanoStr = String.valueOf(nano);
		for (int i = nanoStr.length(); i < 9; i++) { wr.write('0'); }
		wr.write(nanoStr);		
	}
}
