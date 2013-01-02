/*
 * 
 */

package muscle.util.data;

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import muscle.util.Timer;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Joris Borgdorff
 */
public class BlockingQueueTest {
	//private  queue;
	
	@Test(expected = IllegalStateException.class)
	public void testEmpty() throws InterruptedException {
		TakeAddable<String> queue = new SingleProducerConsumerBlockingQueue<String>();
		queue.add("something");
		assertEquals("something", queue.take());
		assertTrue(queue.isEmpty());
		queue.take();
	}

	@Test
	public void testTakePut() {
		TakeAddable<String> queue = new SingleProducerConsumerBlockingQueue<String>();
		try {
			queue.add("something");
			assertEquals("something", queue.take());
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void testSizesFlipside() throws InterruptedException {
		TakeAddable<String> queue = new SingleProducerConsumerBlockingQueue<String>();
		queue.add("something1");
		queue.add("something2");
		queue.add("something3");
		queue.add("something4");
		queue.add("something5");
		assertEquals("something1", queue.take());
		queue.take();
		queue.take();
		queue.add("something6");
		queue.take();
		queue.take();
		queue.take();
		assertTrue(queue.isEmpty());
	}
	
	
	private final static int TESTS = 1000;
	private final static int MSGS_PER_TEST = 10000;
	
	public static void main(String[] args) {
		ExecutorService exec = Executors.newFixedThreadPool(2);
//		int[] nums = {3, 5, 10, 15, 25, 100};
		
//		System.out.println("Old queue implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
//			runWithQueue(exec, new SingleProducerConsumerBlockingQueue<String>(num));
//		}

		System.out.println("New queue implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
			runWithQueue(exec, new SingleProducerConsumerBlockingQueue<String>());
//		}
		
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
//		System.out.println("Multi linked queue implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
//			runWithQueue(exec, new MultiProducerSingleConsumerBlockingQueue<String>());

			System.out.println("Java linked queue implementation");
//		for (int num : nums) {
//			System.out.print("Computing time with queue size " + num + "... ");
			runWithQueue(exec, new LinkedBlockingQueue<String>());
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
//						System.out.print("-");
//						System.out.flush();
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
//					System.out.print("+");
//					System.out.flush();
					q.add(s);
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
//	private static int doSemiheavyCalculation(String s) {
//		return (int)Math.sin(Math.sqrt(Double.valueOf(s + s)));
//	}
}
