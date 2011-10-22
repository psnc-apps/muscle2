/*
 * 
 */
package muscle.core.messaging.serialization;

/**
 *
 * @author jborgdo1
 */
public interface DataConverter<E, F> {
	public F serialize(E data);
	public E deserialize(F data);
}
