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

	private static final long serialVersionUID = 1L;
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
		int max_timesteps = Integer.parseInt(getCxAProperty("max_timesteps"));

		// size in B
		int size = 0;

		// step in current test
		int stepCount = 0;

		// number of the test
		int testNumber = 0;

		// helper with results for a single test
		long[] times = new long[steps];

		double[] avgs = new double[max_timesteps / steps];
		double[] stDevs = new double[max_timesteps / steps];

		byte[] ba;

		// Making noise in order to give time for JVM to stabilize
		System.out.print("Prepairing");
		for (int i = 0; i < steps && !willStop(); ++i) {
			ba = new byte[1024 * 1024 * 32 / (i + 1)];
			entrance.send(ba);
			exit.receive();
			System.out.print(".");
		}
		System.out.println("\n\nValues are NOT divided by 2. Each value is calculated for RTT.\n");

		printHeder(steps);

		ba = new byte[size];

		System.out.printf("%10d", size);

		while (!willStop()) {

			// The step
			long _ = System.currentTimeMillis();
			entrance.send(ba);
			exit.receive();
			_ -= System.currentTimeMillis();

			// Managing results
			times[stepCount] = -_;
			System.out.printf(" %9d", -_);

			// As the test finishes, write the results
			if (++stepCount == steps) {

				// calculate avg and std dev
				avgs[testNumber] = average(times);
				stDevs[testNumber] = stDev(times, avgs[testNumber]);
				System.out.printf(" %10.3f %10.3f", avgs[testNumber],
						stDevs[testNumber]);

				// MB
				double speed = 953.67432 * (size == 0 ? Double.NaN : size)
						/ avgs[testNumber] / 10e6;

				System.out.printf(" %4.1f %14.6f", stDevs[testNumber]
						/ avgs[testNumber] * 100, speed);

				// setting new size
				if (size == 0)
					size = 1024;
				else if (size < 1024 * 1024 * 32)
					size *= 2;

				ba = new byte[size];

				System.out.printf("\n%10d", size);

				stepCount = 0;
				testNumber++;
			}

		}
		System.out.printf("\n");
	}

	private void printHeder(int steps) {
		System.out.printf("%10s", "Size[B]");
		for (int i = 0; i < steps; ++i)
			System.out.printf("  t_%02d[ms]", i);
		System.out.printf(" %10s", "Avg[ms]");
		System.out.printf(" %10s", "StdDev[ms]");
		System.out.printf(" %4s", "[%]");
		System.out.printf(" %14s", "[MB/s]");
		System.out.println();
	}

	private double average(long[] times) {
		long sum = 0;
		for (long time : times)
			sum += time;
		return ((double) sum) / times.length;
	}

	private double stDev(long[] times, double avg) {
		double dev = 0.0;
		for (long time : times)
			dev -= ((double) time - avg) * (avg - (double) time);
		return Math.sqrt(dev / ((double) times.length - 1));
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
