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
public interface SerializerWrapper {
	public void writeInt(int num) throws IOException;
	public void writeBoolean(boolean bool) throws IOException;
	public void writeByteArray(byte[] bytes) throws IOException;
	public void writeString(String str) throws IOException;
	public void writeDouble(double d) throws IOException;
	public void flush() throws IOException;
	public void close() throws IOException;
}
