package examples.pingpong;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.kernel.CAController;
import muscle.util.data.SerializableData;
import muscle.util.serialization.SerializableDataConverter;

public class Ping extends CAController {
	private ConduitEntrance<byte[]> entrance;
	private ConduitExit<byte[]> exit;

	@Override
	protected void addPortals() {
		entrance = addEntrance("out", byte[].class);
		exit = addExit("in", byte[].class);
	}

	@Override
	protected void execute() {
		// How many steps for a single test
		int steps = getIntProperty("steps");

		// How many steps total will be done
		int max_timesteps = getIntProperty("max_timesteps") + 1;
		
		// How many steps total will be done
		int runs = getIntProperty("same_size_runs");
		
		// size in B
		int size = getIntProperty("start_kiB_per_message")*1024;

		// helper with results for a single test
		long[] totalTimes = new long[runs];
		long[] serializationTimes = new long[runs];
		byte[] data = new byte[1024];

		// Making noise in order to give time for JVM to stabilize
		System.out.print("Preparing");
		
		int prepare_steps = getIntProperty("preparation_steps");
		
		for (int i = 0; i < prepare_steps; i++) {
			entrance.send(data);
			exit.receive();
			if (i % 5 == 0) {
				System.out.print(".");
			}
		}
		System.out.println("\n\nValues are NOT divided by 2. Each value is calculated for RTT.");
		System.out.println("Sending " + max_timesteps + " messages in total. For each data size, " + runs + " tests are performed, each sending " + steps + " messages.\n");
		
		System.out.printf("|=%10s|=%10s|=%10s|=%10s|=%10s|=%13s|\n","Size (kiB)","Total (ms)","Avg (us)","StdDev(ms)","StdDev(%)","Speed (MiB/s)");
		
		for (int i = 0; i < getIntProperty("tests_count"); i++) {
			long sum = doComputation(size, totalTimes, runs, steps);
			
			printOutcomes(totalTimes, sum, size, steps);
			if (size == 0) {
				size = 1024;
			} else if (size < Integer.MAX_VALUE / 2) {
				size *= 2;
			} else {
				break;
			}
		}
	}
	
	private long doComputation(int size, long[] totalTimes, int runs, int steps) {
		SerializableDataConverter<byte[]> converter = new SerializableDataConverter<byte[]>();
		byte[] data = new byte[size];
		long sum = 0;
		for (int test = 0; test < runs; test++) {
			long tAll = System.nanoTime();
			for (int i = 0; i < steps; i++) {
				if (willStop()) {
					System.out.println("willStop now!");
					return 0;
				}
				// The step
				entrance.send(data);
				exit.receive();
			}
			sum += totalTimes[test] = System.nanoTime() - tAll;
		}
		
		return sum;
	}
	
	private void printOutcomes(long[] totalTimes, long sum, int size, int steps) {	
		double avg = sum/((double)(totalTimes.length * steps));
		double stdDev = stdDev(totalTimes, avg, steps);

		// nano -> micro
		avg /= 1000d;
		stdDev /= 1000d;
		// MB/s -> 2*((KBytes/1024)/(micro/1000000)) -> 2*1000000*KBytes/1024*micro ->  15625*size / 8*avg
		double speed = (15625l * size) / (8192d*avg);
		System.out.printf("| %10d| %10d| %10.0f| %10.0f| %10.1f| %13.1f| \n", size, sum/1000000, avg, stdDev, 100*stdDev/avg, speed == 0 ? Double.NaN : speed);
	}

	private double average(long[] times, int factor) {
		return ((double) sum(times)) / (times.length * factor);
	}
	
	private long sum(long[] times) {
		long sum = 0;
		for (long time : times) {
			sum += time;
		}
		return sum;
	}

	private double stdDev(long[] times, double avg, int factor) {
		double dev = 0.0;
		for (long time : times) {
			dev -= (time/(double)factor - avg) * (avg - time/(double)factor);
		}
		return Math.sqrt(dev / (times.length - 1));
	}
}
