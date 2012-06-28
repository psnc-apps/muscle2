/*
 * 
 */

package muscle.util.data;

import muscle.util.data.SingleProducerConsumerBlockingQueue;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;
import muscle.util.Timer;

/**
 *
 * @author Joris Borgdorff
 */
public class BlockingQueueTest {
	//private  queue;
	
	@Test
	public void testSizes() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>();
		assertEquals(0, queue.size());
		assertTrue(queue.isEmpty());
		queue.add("something");
		assertEquals(1, queue.size());
		assertFalse(queue.isEmpty());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testEmpty() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>();
		queue.add("something");
		assertEquals("something", queue.remove());
		assertTrue(queue.isEmpty());
		queue.remove();
	}

	@Test
	public void testTakePut() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>();
		try {
			queue.put("something");
			assertEquals("something", queue.take());
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void testSizesFlipside() {
		BlockingQueue<String> queue = new SingleProducerConsumerBlockingQueue<String>();
		queue.offer("something1");
		queue.offer("something2");
		queue.offer("something3");
		queue.offer("something4");
		queue.offer("something5");
		assertEquals("something1", queue.poll());
		queue.poll();
		queue.poll();
		queue.offer("something6");
		assertEquals(3, queue.size());
	}
	
	
	private final static int TESTS = 100;
	private final static int MSGS_PER_TEST = 100000;
	
	public static void main(String[] args) {
		ExecutorService exec = Executors.newFixedThreadPool(2);
		int[] nums = {3, 5, 10, 15, 25, 100};
		
//		System.out.println("Old queue implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
//			runWithQueue(exec, new SingleProducerConsumerBlockingQueue<String>(num));
//		}

		System.out.println("New queue implementation");
		for (int num : nums) {
			System.out.print("Computing time with queue size " + num + "... ");
			runWithQueue(exec, new SingleProducerConsumerBlockingQueue<String>());
		}
		
//		System.out.println("New pointer implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
//			SingleProducerConsumerBlockingQueue1<String> q = new SingleProducerConsumerBlockingQueue1<String>(num);
//			runWithThreads(exec, new TakeConsumerThread(q), new PutProducerThread(q));
//		}
//
//		System.out.println("Java array queue implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
//			runWithQueue(exec, new ArrayBlockingQueue<String>(num));
//		}
//		
//		System.out.println("Java linked queue implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
//			runWithQueue(exec, new LinkedBlockingQueue<String>());
//		}

		exec.shutdown();
	}
	
	private static void runWithQueue(ExecutorService exec, BlockingQueue<String> queue) {
		runWithThreads(exec, new ConsumerThread(queue), new ProducerThread(queue));
	}
	
	private static void runWithThreads(ExecutorService exec, ConsumerThread consumer, ProducerThread producer) {
		Future<?> f1 = exec.submit(consumer);
		Future<?> f2 = exec.submit(producer);
		try {
			f1.get(); f2.get();
		} catch (InterruptedException ex) {
			Logger.getLogger(SingleProducerConsumerBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ExecutionException ex) {
			Logger.getLogger(SingleProducerConsumerBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
		}
		long sum = 0l;
		for (long l : consumer.times) {
			sum += l;
		}
		double total = TESTS * MSGS_PER_TEST;
		System.out.println("average over " + TESTS + " tests: " + Timer.toString(sum) + " s");
	}
	
	static class ConsumerThread implements Runnable {
		private final BlockingQueue<String> q;
		public long outthis, outother;
		public long[] times;
		ConsumerThread(BlockingQueue<String> newQueue) {
			this.q = newQueue;
			this.outthis = outother = 0;
			this.times = new long[TESTS];
		}
		public void run() {
			long lsum = 0;
			Timer t = new Timer();
			for (int j = 0; j < TESTS; j++) {
				for (int i = 0; i < MSGS_PER_TEST; i++) {
					try {
						q.take();
					} catch (InterruptedException ex) {
						Logger.getLogger(SingleProducerConsumerBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
					}
//					lsum += doSemiheavyCalculation(s);
				}
				outother = lsum;
				times[j] = t.reset();
			}
		}
	}
	
//	static class TakeConsumerThread extends ConsumerThread {
//		private final SingleProducerConsumerBlockingQueue1.TakePointer<String> tp;
//		TakeConsumerThread(SingleProducerConsumerBlockingQueue1<String> newQueue) {
//			super(newQueue);
//			tp = newQueue.getTakePointer();
//		}
//		public void run() {
//			long lsum = 0;
//			Timer t = new Timer();
//			for (int j = 0; j < TESTS; j++) {
//				for (int i = 0; i < MSGS_PER_TEST; i++) {
//					try {
//						tp.take();
//					} catch (InterruptedException ex) {
//						Logger.getLogger(SingleProducerConsumerBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
//					}
////					lsum += doSemiheavyCalculation(s);
//				}
//				outother = lsum;
//				times[j] = t.reset();
//			}
//		}
//	}
	
	static class ProducerThread implements Runnable {
		private final BlockingQueue<String> q;
		public long outthis, outother;
		ProducerThread(BlockingQueue<String> newQueue) {
			this.q = newQueue;
			this.outthis = outother = 0;
		}
		public void run() {
			long lsum = 0;
			String s = " bla";
			for (int j = 0; j < TESTS; j++) {
				for (int i = 0; i < MSGS_PER_TEST; i++) {
					try {
						q.put(s);
					} catch (InterruptedException ex) {
						Logger.getLogger(SingleProducerConsumerBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
					}
//					lsum += doSemiheavyCalculation(s);
				}
				outother += lsum;
			}
		}
	}
	
		
//	static class PutProducerThread extends ProducerThread {
//		private final SingleProducerConsumerBlockingQueue1.PutPointer<String> putPointer;
//		PutProducerThread(SingleProducerConsumerBlockingQueue1<String> newQueue) {
//			super(newQueue);
//			this.putPointer = newQueue.getPutPointer();
//		}
//		public void run() {
//			long lsum = 0;
//			String s = "ja ja";
//			for (int j = 0; j < TESTS; j++) {
//				for (int i = 0; i < MSGS_PER_TEST; i++) {
//					try {
//						putPointer.put(s);
//					} catch (InterruptedException ex) {
//						Logger.getLogger(SingleProducerConsumerBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
//					}
////					lsum += doSemiheavyCalculation(s);
//				}
//				outother += lsum;
//			}
//		}
//	}
//	
	private static int doSemiheavyCalculation(String s) {
		return (int)Math.sin(Math.sqrt(Double.valueOf(s + s)));
	}
}
