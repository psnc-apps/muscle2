/*
 * 
 */
package muscle.util.serialization;

/**
 *
 * @author jborgdo1
 */
public interface DataConverter<E, F> {
	public F serialize(E data);
	public E deserialize(F data);
	public E copy(E data);
}
