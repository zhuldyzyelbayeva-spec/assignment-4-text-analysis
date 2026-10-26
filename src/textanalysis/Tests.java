package textanalysis;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class Tests {
    private static int assertions;

    private Tests() { }

    private static void equal(Object expected, Object actual, String label) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(label + ": expected " + expected + ", actual " + actual);
        }
        assertions++;
    }

    private static void matching(String text, String pattern, List<Integer> expected) {
        equal(expected, Algorithms.naive(text, pattern).indices(), "Naive");
        equal(expected, Algorithms.kmp(text, pattern).indices(), "KMP");
        equal(expected, Algorithms.rabinKarp(text, pattern).indices(), "Rabin-Karp");
    }

    private static String randomString(Random random, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append("abcd".charAt(random.nextInt(4)));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        equal(3, Algorithms.lcs("ABCDEF", "ACE"), "LCS example");
        equal(3, Algorithms.lcs("abc", "abc"), "LCS identical");
        equal(0, Algorithms.lcs("abc", "xyz"), "LCS disjoint");
        equal(0, Algorithms.lcs("", "abc"), "LCS empty");
        equal(0, Algorithms.editDistance("same", "same"), "Edit identical");
        equal(3, Algorithms.editDistance("", "abc"), "Edit empty");
        equal(1, Algorithms.editDistance("cat", "cut"), "Substitution");
        equal(1, Algorithms.editDistance("cat", "cats"), "Insertion");
        equal(1, Algorithms.editDistance("cats", "cat"), "Deletion");
        equal(3, Algorithms.editDistance("kitten", "sitting"), "Edit classic");

        matching("abcde", "ab", List.of(0)); // Beginning
        matching("abcde", "bc", List.of(1)); // Middle
        matching("abcde", "de", List.of(3)); // End
        matching("aaaa", "aa", List.of(0, 1, 2)); // Overlapping
        matching("abc", "x", List.of()); // Absent
        matching("abc", "abcd", List.of()); // Longer pattern
        matching("abc", "", List.of(0, 1, 2, 3)); // Every boundary
        matching("", "", List.of(0)); // Empty text and pattern
        matching("abababa", "aba", List.of(0, 2, 4)); // Multiple matches

        equal("[5, 3, 1, 0, 4, 2]", Arrays.toString(Algorithms.suffixArray("banana")),
                "Banana suffix array");
        equal("[0, 1, 3, 0, 0, 2]",
                Arrays.toString(Algorithms.kasai("banana", new int[]{5, 3, 1, 0, 4, 2})),
                "Banana LCP");
        equal("[]", Arrays.toString(Algorithms.kasai("", new int[0])), "Empty Kasai");

        Random random = new Random(42);
        for (int trial = 0; trial < 150; trial++) {
            String text = randomString(random, random.nextInt(35));
            String pattern = randomString(random, random.nextInt(9));
            matching(text, pattern, Algorithms.naive(text, pattern).indices());
        }
        System.out.println("PASS: " + assertions + " assertions");
    }
}
