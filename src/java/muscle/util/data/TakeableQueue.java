/*
 * 
 */
package muscle.util.data;

import java.util.concurrent.BlockingQueue;

/**
 *
 * @author jborgdo1
 */
public interface TakeableQueue<T> extends Takeable<T>, BlockingQueue<T> {
	
}
