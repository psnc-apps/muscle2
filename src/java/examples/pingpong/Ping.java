package examples.pingpong;

import java.math.BigDecimal;
import javax.measure.DecimalMeasure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import muscle.core.ConduitEntrance;
import muscle.core.ConduitExit;
import muscle.core.Scale;
import muscle.core.kernel.CAController;

public class Ping extends CAController {
	private ConduitEntrance<byte[]> entrance;
	private ConduitExit<byte[]> exit;

	@Override
	protected void addPortals() {
		entrance = addEntrance("out", 1, byte[].class);
		exit = addExit("in", 1, byte[].class);
	}

	@Override
	protected void execute() {
		// How many steps for a single test
		int steps = Integer.parseInt(getCxAProperty("steps"));

		// How many steps total will be done
		int max_timesteps = Integer.parseInt(getCxAProperty("max_timesteps")) + 1;
		
		// How many steps total will be done
		int runs = Integer.parseInt(getCxAProperty("same_size_runs"));
		
		// size in B
		int size = getIntProperty("start_kiB_per_message")*1024;

		// helper with results for a single test
		long[] totalTimes = new long[runs];
		byte[] data = new byte[1024*1024];

		// Making noise in order to give time for JVM to stabilize
		System.out.print("Preparing");
		for (int i = 0; i < steps && !willStop(); i++) {
			entrance.send(data);
			exit.receive();
			System.out.print(".");
		}
		System.out.println("\n\nValues are NOT divided by 2. Each value is calculated for RTT.");
		System.out.println("Sending " + max_timesteps + " messages in total. For each data size, " + runs + " tests are performed, each sending " + steps + " messages.\n");
		
		System.out.printf("|=%10s|=%10s|=%10s|=%10s|=%10s|=%10s|\n","Size (kiB)","Total (ms)","Avg (ms)","StdDev(ms)","StdDev(%)","Speed (MB/s)");
		
		while (!willStop()) {
			data = new byte[size];
			for (int test = 0; test < runs; test++) {
				long tAll = System.nanoTime();
				for (int i = 0; i < steps; i++) {
					if (willStop()) {
						System.out.println("willStop now!");
						return;
					}
					// The step
					entrance.send(data);
					exit.receive();
				}
				totalTimes[test] = System.nanoTime() - tAll;
			}
			double avg = average(totalTimes,steps);
			double stdDev = stdDev(totalTimes, avg, steps);

			// nano -> milli
			avg /= 1000000d;
			stdDev /= 1000000d;
			// MB/s -> 2*((Bytes/1000000)/(millisec/1000)) -> 2*(Bytes/1000)/millisec -> 2*(Bytes/(millisec*1000)) ->Bytes/(millisec*500)
			double speed = size / (avg * 500d);
			System.out.printf("| %10d| %10d| %10.3f| %10.3f| %10.3f| %10.3f|\n", size/1024, sum(totalTimes)/1000000, avg, stdDev, 100*stdDev/avg, speed == 0 ? Double.NaN : speed);

			if (size == 0) {
				size = 1024;
			} else if (size < Integer.MAX_VALUE / 2) {
				size *= 2;
			}
		}
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
		DecimalMeasure<Duration> dt = DecimalMeasure.valueOf(new BigDecimal(1),
				SI.SECOND);
		DecimalMeasure<Length> dx = DecimalMeasure.valueOf(new BigDecimal(1),
				SI.METER);
		return new Scale(dt, dx);
	}

}
