using Google.ProtocolBuffers;
using System;
using System.Diagnostics;
using tutorial;

namespace ProtoBenchmark
{
    class MainClass
    {
        public static void Main(string[] args)
        {
            RunBechmark("BenchmarkSerializePerson", BenchmarkSerializePerson, 10 * 1000, 10 * 1000 * 1000);

            RunBechmark("BenchmarkSerializeNewPerson", BenchmarkSerializeNewPerson, 10 * 1000, 10 * 1000 * 1000);

            RunBechmark("BenchmarkSerializeJustEmailPerson", BenchmarkSerializeJustEmailPerson, 10 * 1000, 10 * 1000 * 1000);

            RunBechmark("BenchmarkSerializeJustIdPerson", BenchmarkSerializeJustIdPerson, 10 * 1000, 10 * 1000 * 1000);

            RunBechmark("BenchmarkSerializeJustIdPersonToPreallocatedArray", BenchmarkSerializeJustIdPersonToPreallocatedArray, 10 * 1000, 10 * 1000 * 1000);

            RunBechmark("BenchmarkParse", BenchmarkParse, 10 * 1000, 1000 * 1000);
            
            RunBechmark("BenchmarkWriteInt32", BenchmarkWriteInt32, 10 * 1000, 10 * 1000 * 1000);

            RunBechmark("BenchmarkWriteShortString", (i) => BenchmarkWriteString(i, "abc"), 10 * 1000, 10 * 1000 * 1000);

            RunBechmark("BenchmarkWriteLongString", (i) => BenchmarkWriteString(i, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."), 10 * 1000, 10 * 1000 * 1000);
        }

        public static void RunBechmark(string name, Func<int, long> benchmarkFunc, int warmupIterations, int benchmarkIterations)
        {
            System.GC.Collect();  // Make sure we are not influenced by memory pollution from previous tests.

            Console.WriteLine("Benchmark: " + name);
            benchmarkFunc(warmupIterations);
            Stopwatch stopwatch = new Stopwatch();
            stopwatch.Start();
            long processedBytes = benchmarkFunc(benchmarkIterations);
            stopwatch.Stop();

            Console.WriteLine("Iterations: " + benchmarkIterations);
            long oneIterationNanos = (stopwatch.ElapsedTicks * 1000L * 1000L *1000L) / (long) benchmarkIterations / (long) Stopwatch.Frequency;
            Console.WriteLine("Time(ns): " + oneIterationNanos);

            double throughput = (double) processedBytes / 1024 / 1024 / (stopwatch.Elapsed.TotalSeconds);
            Console.WriteLine("Throughput: " + throughput + "MB/s");
            Console.WriteLine();
        }

        private static Person CreatePerson()
        {
            return Person.CreateBuilder()
                .SetId(1234)
                .SetName("John Doe")
                .SetEmail("jdoe@example.com")
               .AddPhone(
                       Person.Types.PhoneNumber.CreateBuilder()
                         .SetNumber("555-4321")
                         .SetType(Person.Types.PhoneType.HOME).Build()
               )
                    .Build();
        }

        private static byte[] CreateProtobuf()
        {
            var person = CreatePerson();
            return person.ToByteArray();
        }

        private static long BenchmarkSerializePerson(int iterations)
        {
            var person = CreatePerson();

            byte[] buffer = null;
            for(int i = 0; i < iterations; i++)
            {
                buffer = person.ToByteArray();
            }

            return person.SerializedSize * (long) iterations;
        }

        private static long BenchmarkSerializeNewPerson(int iterations)
        {
            byte[] buffer = null;
            for (int i = 0; i < iterations; i++)
            {
                var person = CreatePerson();
                buffer = person.ToByteArray();
            }

            return buffer.LongLength * (long)iterations;
        }

        private static long BenchmarkSerializeJustEmailPerson(int iterations)
        {
            var person = Person.CreateBuilder().SetEmail("jdoe@example.com").Build();

            byte[] buffer = null;
            for (int i = 0; i < iterations; i++)
            {
                buffer = person.ToByteArray();
            }

            return person.SerializedSize * (long)iterations;
        }

        private static long BenchmarkSerializeJustIdPerson(int iterations)
        {
            var person = Person.CreateBuilder().SetId(1234).Build();

            byte[] buffer = null;
            for (int i = 0; i < iterations; i++)
            {
                buffer = person.ToByteArray();
            }

            return person.SerializedSize * (long)iterations;
        }

        private static long BenchmarkSerializeJustIdPersonToPreallocatedArray(int iterations)
        {
            var person = Person.CreateBuilder().SetId(1234).Build();

            var buffer = new byte[person.SerializedSize];

            for (int i = 0; i < iterations; i++)
            {
                // TODO: reuse the open stream...
                var codedOutputStream = CodedOutputStream.CreateInstance(buffer);
                person.WriteTo(codedOutputStream);
            }

            return person.SerializedSize * (long)iterations;
        }

        private static long BenchmarkParse(int iterations)
        {
            byte[] buffer = CreateProtobuf();

            Person person;
            for(int i = 0; i < iterations; i++)
            {
                person = Person.ParseFrom(buffer);
            }

            return buffer.LongLength * (long) iterations;
        }

        private static long BenchmarkWriteInt32(int iterations)
        {
            int value = 1234;
            int size = CodedOutputStream.ComputeInt32Size(2, value);
            var buffer = new byte[size];
            CodedOutputStream codedOutputStream;

            for (int i = 0; i < iterations; i++)
            {
                // TODO: reuse the open stream...
                codedOutputStream = CodedOutputStream.CreateInstance(buffer);
                codedOutputStream.WriteInt32(2, "Id", 1234);
            }
            return size * (long)iterations;
        }

        private static long BenchmarkWriteString(int iterations, string s)
        {
            int size = CodedOutputStream.ComputeStringSizeNoTag(s);
            byte[] buffer = new byte[size];
            CodedOutputStream codedOutputStream;

            for (int i = 0; i < iterations; i++)
            {
                // TODO: is there a way to reuse the codedOutputStream?
                codedOutputStream = CodedOutputStream.CreateInstance(buffer);
                codedOutputStream.WriteStringNoTag(s);
            }
            return size * (long)iterations;
        }
    }
}
