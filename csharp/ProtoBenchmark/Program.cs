using System;
using System.Diagnostics;
using tutorial;

namespace ProtoBenchmark
{
    class MainClass
    {
        public static void Main(string[] args)
        {
            RunBechmark("BenchmarkSerialize", BenchmarkSerialize, 10000, 1000000);

            RunBechmark("BenchmarkParse", BenchmarkParse, 10000, 1000000);
        }

        public static void RunBechmark(string name, Func<int, long> benchmarkFunc, int warmupIterations, int benchmarkIterations)
        {
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
                ).Build();
        }

        private static byte[] CreateProtobuf()
        {
            var person = CreatePerson();
            return person.ToByteArray();
        }

        // TODO(jtattermusch): benchmark alloc

        private static long BenchmarkSerialize(int iterations)
        {
            var person = CreatePerson();

            byte[] buffer = null;
            for(int i = 0; i < iterations; i++)
            {
                buffer = person.ToByteArray();
            }

            return buffer.LongLength * (long) iterations;
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
    }
}
