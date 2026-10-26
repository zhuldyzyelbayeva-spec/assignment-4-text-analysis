package textanalysis;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Random;
import java.util.function.LongSupplier;

public final class Benchmark {
    private static final Random RANDOM = new Random(20261025L);
    private static final int REPETITIONS = 5;
    private static final int WARMUPS = 2;
    private static volatile long sink;

    private Benchmark() { }

    private static String randomString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append("abcd".charAt(RANDOM.nextInt(4)));
        }
        return builder.toString();
    }

    private static double averageMillis(LongSupplier algorithm) {
        for (int i = 0; i < WARMUPS; i++) {
            sink ^= algorithm.getAsLong();
        }
        long totalNanoseconds = 0;
        for (int i = 0; i < REPETITIONS; i++) {
            long start = System.nanoTime();
            long result = algorithm.getAsLong();
            long elapsed = System.nanoTime() - start;
            sink ^= result;
            totalNanoseconds += elapsed;
        }
        return totalNanoseconds / (double) REPETITIONS / 1_000_000.0;
    }

    private static void record(PrintWriter csv, String name, int size, String scenario,
                               double millis, long operations) {
        csv.printf(Locale.US, "%s,%d,%s,%.6f,%d%n", name, size, scenario, millis, operations);
        System.out.printf(Locale.US, "%-13s %7d %-8s %10.4f ms%n",
                name, size, scenario, millis);
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("results"));
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(Path.of("results/benchmark.csv")))) {
            csv.println("algorithm,input_size,scenario,avg_ms,operations");
            for (int size : new int[]{100, 500, 1000}) {
                String a = randomString(size);
                String b = randomString(size);
                record(csv, "LCS", size, "random",
                        averageMillis(() -> Algorithms.lcsMeasured(a, b).value()),
                        Algorithms.lcsMeasured(a, b).cells());
                record(csv, "EditDistance", size, "random",
                        averageMillis(() -> Algorithms.editDistanceMeasured(a, b).value()),
                        Algorithms.editDistanceMeasured(a, b).cells());
            }
            for (int size : new int[]{10000, 50000, 100000}) {
                String text = randomString(size);
                String present = text.substring(size / 2, size / 2 + 16);
                String absent = "z".repeat(16);
                for (String scenario : new String[]{"present", "absent"}) {
                    String pattern = scenario.equals("present") ? present : absent;
                    Algorithms.MatchResult naive = Algorithms.naive(text, pattern);
                    Algorithms.MatchResult kmp = Algorithms.kmp(text, pattern);
                    Algorithms.MatchResult rk = Algorithms.rabinKarp(text, pattern);
                    if (!naive.indices().equals(kmp.indices()) || !naive.indices().equals(rk.indices())) {
                        throw new AssertionError("String match algorithms disagree");
                    }
                    record(csv, "Naive", size, scenario,
                            averageMillis(() -> Algorithms.naive(text, pattern).indices().size()),
                            naive.comparisons());
                    record(csv, "KMP", size, scenario,
                            averageMillis(() -> Algorithms.kmp(text, pattern).indices().size()),
                            kmp.comparisons());
                    record(csv, "RabinKarp", size, scenario,
                            averageMillis(() -> Algorithms.rabinKarp(text, pattern).indices().size()),
                            rk.comparisons());
                }
            }
            for (int size : new int[]{1000, 5000, 10000}) {
                String text = randomString(size);
                int[] suffixArray = Algorithms.suffixArray(text);
                long comparisons = Algorithms.kasaiMeasured(text, suffixArray).comparisons();
                record(csv, "Kasai", size, "random",
                        averageMillis(() -> Algorithms.kasai(text, suffixArray).length), comparisons);
            }
        }
        System.out.println("Benchmark complete; checksum=" + sink);
    }
}
