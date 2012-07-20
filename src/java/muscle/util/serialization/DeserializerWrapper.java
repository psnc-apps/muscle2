/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.serialization;

import java.io.IOException;

/**
 *
 * @author Joris Borgdorff
 */
public interface DeserializerWrapper {
	/** Call refresh before reading any values. Call it once for each time the opposite side calls flush. It implies cleanup. */
	public void refresh() throws IOException;
	/** Cleanup any resources associated to the previous message. */
	public void cleanUp() throws IOException;
	/** Read a single int; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public int readInt() throws IOException;
	/** Read a single boolean may only be called after refresh has been called, one refresh per flush of the sending side. */
	public boolean readBoolean() throws IOException;
	/** Read an array of bytes; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public byte[] readByteArray() throws IOException;
	/** Read a string; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public String readString() throws IOException;
	/** Read a double; may only be called after refresh has been called, one refresh per flush of the sending side. */
	public double readDouble() throws IOException;
	/** Close the deserializer, it can not be used again. */
	public void close() throws IOException;
}
