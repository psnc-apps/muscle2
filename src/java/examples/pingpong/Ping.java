package examples.pingpong;

import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.Scale;
import muscle.core.kernel.CAController;
import muscle.core.model.Distance;
import muscle.util.data.SerializableData;
import muscle.util.serialization.SerializableDataConverter;

public class Ping extends CAController {
	private ConduitEntrance<byte[]> entrance;
	private ConduitExit<byte[]> exit;

	@Override
	protected void addPortals() {
		entrance = addSynchronizedEntrance("out", byte[].class);
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
		
		System.out.printf("|=%10s|=%10s|=%10s|=%10s|=%10s|=%13s|=%10s|=%10s|=%13s|\n","Size (kiB)","Total (ms)","Avg (us)","StdDev(ms)","StdDev(%)","Speed (MiB/s)","Data (ms)","Avg (us)","Speed (MiB/s)");
		
		for (int i = 0; i < getIntProperty("tests_count"); i++) {
			long sum1 = doComputation(true, size, totalTimes, runs, steps);
			long sum2 = doComputation(false, size, serializationTimes, runs, steps);
			
			printOutcomes(totalTimes, sum1,sum2, size, steps);
			if (size == 0) {
				size = 1024;
			} else if (size < Integer.MAX_VALUE / 2) {
				size *= 2;
			} else {
				break;
			}
		}
	}
	
	private long doComputation(boolean send, int size, long[] totalTimes, int runs, int steps) {
		SerializableDataConverter<byte[]> converter = new SerializableDataConverter<byte[]>();
		byte[] data = new byte[size];
		long sum = 0;
		for (int test = 0; test < runs; test++) {
			long tAll = System.nanoTime();
			for (int i = 0; i < steps; i++) {
				if (send) {
					if (willStop()) {
						System.out.println("willStop now!");
						return 0;
					}
					// The step
					entrance.send(data);
					exit.receive();
				}
				else {
					// Enter synchronized conduit
					//data = converter.copy(data);
					// Serialize for sending
					SerializableData sdata = converter.serialize(data);
//					// 
					byte[] data2 = (byte[])sdata.getValue();
					for (int j = 0; j < data2.length; j++) {
						data[j] = data2[j];
					}
					byte[] data3 = new byte[data.length];
					for (int j = 0; j < data.length; j++) {
						data3[j] = data[j];
					}
//                    
//					// Enter synchronized conduit
//					data = converter.copy(data);
					sdata = converter.serialize(data3);
					data2 = (byte[])sdata.getValue();
					for (int j = 0; j < data2.length; j++) {
						data3[j] = data2[j];
					}
					for (int j = 0; j < data2.length; j++) {
						data[j] = data3[j];
					}
				}
			}
			sum += totalTimes[test] = System.nanoTime() - tAll;
		}
		
		return sum;
	}
	
	private void printOutcomes(long[] totalTimes, long sum1, long sum2, int size, int steps) {	
		double avg1 = sum1/((double)(totalTimes.length * steps));
		double avg2 = sum2/((double)(totalTimes.length * steps));
		double stdDev = stdDev(totalTimes, avg1, steps);

		// nano -> micro
		avg1 /= 1000d;
		avg2 /= 1000d;
		stdDev /= 1000d;
		// MB/s -> 2*((KBytes/1024)/(micro/1000000)) -> 2*1000000*KBytes/1024*micro ->  15625*size / 8*avg
		size /= 1024;
		double speed1 = 15625 * size / (8*avg1);
		double speed2 = 15625 * size / (8*avg2);
		System.out.printf("| %10d| %10d| %10.0f| %10.0f| %10.1f| %13.1f| %10d| %10.0f| %13.1f|\n", size, sum1/1000000, avg1, stdDev, 100*stdDev/avg1, speed1 == 0 ? Double.NaN : speed1, sum2/1000000, avg2, speed2);
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

	@Override
	public Scale getScale() {
		Distance delta = new Distance(1);
		return new Scale(delta,delta);
	}
}
