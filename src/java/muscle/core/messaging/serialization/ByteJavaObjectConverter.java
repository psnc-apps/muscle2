/*
 * 
 */
package muscle.core.messaging.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Deserialize an object from given byte array
 * @author Joris Borgdorff
 */
public class ByteJavaObjectConverter<T> implements DataConverter<T, byte[]> {

	/**
	serialize an object
	 */
	public byte[] serialize(T object) {

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(byteStream);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		try {
			// write object to the byteStream
			out.writeObject(object);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
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

	public T deserialize(byte[] data) {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);

		ObjectInputStream in;
		try {
			in = new ObjectInputStream(byteStream);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}

		// read an object from the byteStream
		try {
			return (T) in.readObject();
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		} catch (java.lang.ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
