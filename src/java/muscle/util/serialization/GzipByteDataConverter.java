/*
 * 
 */
package muscle.util.serialization;

/**
 *
 * @author Joris Borgdorff
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
compress/decompress objects
<br>
compression level (-1--9):<br>
java.util.zip.Deflater.DEFAULT_COMPRESSION (-1)
java.util.zip.Deflater.NO_COMPRESSION (0)
java.util.zip.Deflater.BEST_SPEED (1)
java.util.zip.Deflater.BEST_COMPRESSION (9)
<br>
compression strategy:<br>
java.util.zip.Deflater.DEFAULT_STRATEGY (0)
java.util.zip.Deflater.FILTERED (1)
java.util.zip.Deflater.HUFFMAN_ONLY (2)
<br>note: java.util.zip.Deflater.DEFLATED (8) is not a strategy
@author Jan Hegewald
 */
public class GzipByteDataConverter<T> extends AbstractDataConverter<T, byte[]> {

	private Deflater deflater;
	private Inflater inflater;

	public GzipByteDataConverter() {
		deflater = new Deflater();
		inflater = new Inflater();
	}

	public GzipByteDataConverter(int level, int strategy, boolean gzipCompatible) {
		deflater = new Deflater(level, gzipCompatible);
		deflater.setStrategy(strategy);

		inflater = new Inflater(gzipCompatible);
	}

	public byte[] serialize(T object) {

		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			//GZIPOutputStream gzOut = new GZIPOutputStream(bOut); // this uses a DeflaterOutputStream with new Deflater(Deflater.DEFAULT_COMPRESSION, true)
			DeflaterOutputStream deflaterStream = new DeflaterOutputStream(byteStream, deflater);
			ObjectOutputStream objectOut = new ObjectOutputStream(deflaterStream);
			objectOut.writeObject(object);
			deflaterStream.finish();
			byteStream.flush();
			byteStream.close();

			return byteStream.toByteArray();
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}

	public T deserialize(byte[] bytes) {
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
			InflaterInputStream inflaterStream = new InflaterInputStream(byteStream, inflater);
			ObjectInputStream oIn = new ObjectInputStream(inflaterStream);
			return (T) oIn.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
