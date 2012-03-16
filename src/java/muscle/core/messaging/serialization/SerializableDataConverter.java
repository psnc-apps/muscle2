/*
 * 
 */

package muscle.core.messaging.serialization;

import java.io.Serializable;
import utilities.data.SerializableData;

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
