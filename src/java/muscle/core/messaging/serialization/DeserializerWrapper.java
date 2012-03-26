/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.core.messaging.serialization;

import java.io.IOException;

/**
 *
 * @author Joris Borgdorff
 */
public interface DeserializerWrapper {
	public void refresh() throws IOException;
	public int readInt() throws IOException;
	public boolean readBoolean() throws IOException;
	public byte[] readByteArray() throws IOException;
	public String readString() throws IOException;
	public double readDouble() throws IOException;
	public void close() throws IOException;
}
