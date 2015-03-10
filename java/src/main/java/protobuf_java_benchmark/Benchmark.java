package protobuf_java_benchmark;

import java.util.concurrent.TimeUnit;

import tutorial.Test.Person;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.protobuf.InvalidProtocolBufferException;

public class Benchmark {
	
	public static void main(String[] args) {
		runBechmark("BenchmarkSerialize", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {
				return benchmarkSerialize(iterations);
			}
		}, 10000, 1000000);

		runBechmark("BenchmarkParse", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {

				return benchmarkParse(iterations);
			}
		}, 10000, 1000000);
	}

	public static void runBechmark(String name,
			Function<Integer, Long> benchmarkFunc, int warmupIterations,
			int benchmarkIterations) {
		System.out.println("Benchmark: " + name);
		benchmarkFunc.apply(warmupIterations);

		Stopwatch stopwatch = Stopwatch.createStarted();
		long processedBytes = benchmarkFunc.apply(benchmarkIterations);
		stopwatch.stop();

		System.out.println("Iterations: " + benchmarkIterations);
		long oneIterationNanos = stopwatch.elapsed(TimeUnit.NANOSECONDS)
				/ (long) benchmarkIterations;
		System.out.println("Time(ns): " + oneIterationNanos);

		double throughput = (double) processedBytes * 1000 * 1000 * 1000 / 1024
				/ 1024 / stopwatch.elapsed(TimeUnit.NANOSECONDS);
		System.out.println("Throughput: " + throughput + "MB/s");
		System.out.println();
	}
	
	private static Person createPerson() {
		return Person
				.newBuilder()
				.setId(1234)
				.setName("John Doe")
				.setEmail("jdoe@example.com")
				.addPhone(
						Person.PhoneNumber.newBuilder().setNumber("555-4321")
								.setType(Person.PhoneType.HOME)).build();
	}

	private static byte[] createProtobuf() {
		Person person = createPerson();
		return person.toByteArray();
	}

	private static long benchmarkSerialize(int iterations) {
		Person person = createPerson();

		byte[] buffer = null;
		for (int i = 0; i < iterations; i++) {
			buffer = person.toByteArray();
		}

		return buffer.length * (long) iterations;
	}

	private static long benchmarkParse(int iterations) {
		try {
			byte[] buffer = createProtobuf();

			Person person;
			for (int i = 0; i < iterations; i++) {
				person = Person.parseFrom(buffer);
			}

			return buffer.length * (long) iterations;
		} catch (InvalidProtocolBufferException e) {
			throw Throwables.propagate(e);
		}
	}
	
}
