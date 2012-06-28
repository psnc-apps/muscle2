/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.data;

/**
 *
 * @author Joris Borgdorff
 */
public class Tuple<S,T> {
	private final S first;
	private final T second;
	
	public Tuple(S first, T second) {
		this.first = first;
		this.second = second;
	}
	
	public S getFirst() {
		return first;
	}

	public T getSecond() {
		return second;
	}
}
