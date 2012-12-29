/*
 * 
 */
package muscle.util.serialization;

import java.io.*;

/**
 * Deserialize an object from given byte array
 * @author Joris Borgdorff
 */
public class ByteJavaObjectConverter<T> extends AbstractDataConverter<T, byte[]> {

	/**
	serialize an object
	 */
	public byte[] serialize(T object) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(byteStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			// write object to the byteStream
			out.writeObject(object);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (RuntimeException e) {
			throw e;
		} finally {
			try {
				out.close();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
			try {
				byteStream.close();
			} catch (java.io.IOException e) {
				throw new RuntimeException(e);
			}
		}

		return byteStream.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public T deserialize(byte[] data) {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

		ObjectInputStream in;
		try {
			in = new ObjectInputStream(byteStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// read an object from the byteStream
		try {
			return (T) in.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
