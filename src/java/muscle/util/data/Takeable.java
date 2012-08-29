/*
 * 
 */
package muscle.util.data;

/**
 *
 * @author jborgdo1
 */
public interface Takeable<T> {
	public T take() throws InterruptedException;
	public boolean isEmpty();
}
