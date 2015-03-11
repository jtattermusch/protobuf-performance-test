package protobuf_java_benchmark;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import tutorial.Test.Person;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.InvalidProtocolBufferException;

public class Benchmark {
	
	public static void main(String[] args) {
		runBechmark("benchmarkSerializePerson", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {
				return benchmarkSerializePerson(iterations);
			}
		}, 10 * 1000, 10 * 1000 * 1000);
		
		runBechmark("benchmarkSerializeJustIdPerson", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {
				return benchmarkSerializeJustIdPerson(iterations);
			}
		}, 10 * 1000, 10 * 1000 * 1000);
		
		runBechmark("benchmarkSerializeJustIdPersonToPreallocatedArray", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {

				return benchmarkSerializeJustIdPersonToPreallocatedArray(iterations);
			}
		}, 10 * 1000, 10 * 1000 * 1000);

		runBechmark("benchmarkParse", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {

				return benchmarkParse(iterations);
			}
		}, 10 * 1000, 1000 * 1000);
		
		runBechmark("benchmarkWriteInt32", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {

				return benchmarkWriteInt32(iterations);
			}
		}, 10 * 1000, 10 * 1000 * 1000);
		
		runBechmark("benchmarkWriteShortString", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {

				return benchmarkWriteString(iterations, "abc");
			}
		}, 10 * 1000, 10 * 1000 * 1000);
		
		runBechmark("benchmarkWriteLongString", new Function<Integer, Long>() {
			public Long apply(Integer iterations) {

				return benchmarkWriteString(iterations, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
			}
		}, 10 * 1000, 10 * 1000 * 1000);
	}

	public static void runBechmark(String name,
			Function<Integer, Long> benchmarkFunc, int warmupIterations,
			int benchmarkIterations) {
		 
		System.gc();  // Make sure we are not influenced by memory pollution from previous tests.
		
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

	private static long benchmarkSerializePerson(int iterations) {
		Person person = createPerson();

		byte[] buffer = null;
		for (int i = 0; i < iterations; i++) {
			buffer = person.toByteArray();
		}

		return person.getSerializedSize() * (long) iterations;
	}
	
	private static long benchmarkSerializeJustIdPerson(int iterations) {
		Person person = Person.newBuilder().setId(1234).build();

		byte[] buffer = null;
		for (int i = 0; i < iterations; i++) {
			buffer = person.toByteArray();
		}

		return person.getSerializedSize() * (long) iterations;
	}
	
	private static long benchmarkSerializeJustIdPersonToPreallocatedArray(int iterations)
    {
		try {
			Person person = Person.newBuilder().setId(1234).build();

			byte[] buffer = new byte[person.getSerializedSize()];

			for (int i = 0; i < iterations; i++) {
				// TODO: is there a way to reuse the codedOutputStream?
				CodedOutputStream codedOutputStream = CodedOutputStream
						.newInstance(buffer);
				person.writeTo(codedOutputStream);
			}

			return person.getSerializedSize() * (long) iterations;
		} catch(IOException e) {
			throw Throwables.propagate(e);
		}
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

    private static long benchmarkWriteInt32(int iterations)
    {
		try {
			int value = 1234;
			int size = CodedOutputStream.computeInt32Size(2, value);
			byte[] buffer = new byte[size];
			CodedOutputStream codedOutputStream;

			for (int i = 0; i < iterations; i++) {
				// TODO: is there a way to reuse the codedOutputStream?
				codedOutputStream = CodedOutputStream.newInstance(buffer);
				codedOutputStream.writeInt32(2, 1234);
			}
			return size * (long) iterations;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
    }
    
    private static long benchmarkWriteString(int iterations, String s)
    {
		try {
			int size = CodedOutputStream.computeStringSizeNoTag(s);
			byte[] buffer = new byte[size];
			CodedOutputStream codedOutputStream;

			for (int i = 0; i < iterations; i++) {
				// TODO: is there a way to reuse the codedOutputStream?
				codedOutputStream = CodedOutputStream.newInstance(buffer);
				codedOutputStream.writeStringNoTag(s);
			}
			return size * (long) iterations;
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
    }
}
