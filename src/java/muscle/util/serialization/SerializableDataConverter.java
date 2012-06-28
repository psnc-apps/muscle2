/*
 * 
 */

package muscle.util.serialization;

import java.io.Serializable;
import muscle.util.data.SerializableData;

/**
 *
 * @author Joris Borgdorff
 */
public class SerializableDataConverter<T extends Serializable> implements DataConverter<T,SerializableData> {
	@Override
	public SerializableData serialize(T data) {
		return SerializableData.valueOf(data);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(SerializableData data) {
		return (T)data.getValue();
	}

	@Override
	public T copy(T data) {
		return SerializableData.createIndependent(data);
	}
}
