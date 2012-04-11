/*
 * 
 */

package utilities.data;

import java.util.concurrent.BlockingQueue;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Joris Borgdorff
 */
public class BlockingQueueTest {
	//private  queue;
	
	@Test
	public void testSizes() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>(100);
		assertEquals(0, queue.size());
		assertEquals(100, queue.remainingCapacity());
		assertTrue(queue.isEmpty());
		queue.add("something");
		assertEquals(1, queue.size());
		assertEquals(99, queue.remainingCapacity());
		assertFalse(queue.isEmpty());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testFull() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>(1);
		queue.add("something");
		queue.add("something");
	}

	@Test(expected = IllegalStateException.class)
	public void testEmpty() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>(1);
		queue.add("something");
		assertEquals("something", queue.remove());
		assertTrue(queue.isEmpty());
		queue.remove();
	}

	@Test
	public void testTakePut() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>(1);
		try {
			queue.put("something");
			assertEquals("something", queue.take());
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void testSizesFlipside() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>(5);
		queue.offer("something1");
		queue.offer("something2");
		queue.offer("something3");
		queue.offer("something4");
		queue.offer("something5");
		assertEquals("something1", queue.poll());
		queue.poll();
		queue.poll();
		queue.offer("something6");
		assertEquals(2, queue.remainingCapacity());
		assertEquals(3, queue.size());
	}
}
