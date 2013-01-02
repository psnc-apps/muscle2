/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package muscle.util.data;

/**
 *
 * @author Joris Borgdorff
 */
public interface TakeAddable<T> extends Takeable<T> {
	public boolean add(T e);
}
