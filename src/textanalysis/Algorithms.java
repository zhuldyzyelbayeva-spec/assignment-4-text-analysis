package textanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Algorithms {
    private Algorithms() { }

    public record MatchResult(List<Integer> indices, long comparisons) { }
    public record DpResult(int value, long cells) { }
    public record LcpResult(int[] lcp, long comparisons) { }

    public static int lcs(String a, String b) {
        return lcsMeasured(a, b).value();
    }

    public static DpResult lcsMeasured(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return new DpResult(dp[a.length()][b.length()], (long) a.length() * b.length());
    }

    public static int editDistance(String a, String b) {
        return editDistanceMeasured(a, b).value();
    }

    public static DpResult editDistanceMeasured(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= b.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int substitutionCost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                int delete = dp[i - 1][j] + 1;
                int insert = dp[i][j - 1] + 1;
                int substitute = dp[i - 1][j - 1] + substitutionCost;
                dp[i][j] = Math.min(delete, Math.min(insert, substitute));
            }
        }
        return new DpResult(dp[a.length()][b.length()], (long) a.length() * b.length());
    }

    private static List<Integer> emptyPatternMatches(String text) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i <= text.length(); i++) {
            indices.add(i);
        }
        return indices;
    }

    public static MatchResult naive(String text, String pattern) {
        if (pattern.isEmpty()) {
            return new MatchResult(emptyPatternMatches(text), 0);
        }
        List<Integer> indices = new ArrayList<>();
        long comparisons = 0;
        for (int start = 0; start + pattern.length() <= text.length(); start++) {
            int j = 0;
            while (j < pattern.length()) {
                comparisons++;
                if (text.charAt(start + j) != pattern.charAt(j)) {
                    break;
                }
                j++;
            }
            if (j == pattern.length()) {
                indices.add(start);
            }
        }
        return new MatchResult(indices, comparisons);
    }

    public static int[] lps(String pattern) {
        int[] lps = new int[pattern.length()];
        int prefixLength = 0;
        int i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(prefixLength)) {
                lps[i++] = ++prefixLength;
            } else if (prefixLength > 0) {
                prefixLength = lps[prefixLength - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    public static MatchResult kmp(String text, String pattern) {
        if (pattern.isEmpty()) {
            return new MatchResult(emptyPatternMatches(text), 0);
        }
        int[] lps = lps(pattern);
        List<Integer> indices = new ArrayList<>();
        int i = 0;
        int j = 0;
        long comparisons = 0;
        while (i < text.length()) {
            comparisons++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    indices.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return new MatchResult(indices, comparisons);
    }

    public static MatchResult rabinKarp(String text, String pattern) {
        if (pattern.isEmpty()) {
            return new MatchResult(emptyPatternMatches(text), 0);
        }
        int n = text.length();
        int m = pattern.length();
        List<Integer> indices = new ArrayList<>();
        if (m > n) {
            return new MatchResult(indices, 0);
        }
        final long modulus = 1_000_000_007L;
        final long base = 257L;
        long highestPower = 1;
        long patternHash = 0;
        long windowHash = 0;
        long comparisons = 0;
        for (int i = 1; i < m; i++) {
            highestPower = highestPower * base % modulus;
        }
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % modulus;
            windowHash = (windowHash * base + text.charAt(i)) % modulus;
        }
        for (int start = 0; start <= n - m; start++) {
            if (patternHash == windowHash) {
                int j = 0;
                while (j < m) {
                    comparisons++;
                    if (text.charAt(start + j) != pattern.charAt(j)) {
                        break;
                    }
                    j++;
                }
                if (j == m) {
                    indices.add(start);
                }
            }
            if (start < n - m) {
                long removed = text.charAt(start) * highestPower % modulus;
                windowHash = ((windowHash - removed + modulus) * base
                        + text.charAt(start + m)) % modulus;
            }
        }
        return new MatchResult(indices, comparisons);
    }

    public static int[] kasai(String text, int[] suffixArray) {
        return kasaiMeasured(text, suffixArray).lcp();
    }

    public static LcpResult kasaiMeasured(String text, int[] suffixArray) {
        int n = text.length();
        if (suffixArray.length != n) {
            throw new IllegalArgumentException("suffix array length mismatch");
        }
        int[] rank = new int[n];
        int[] lcp = new int[n];
        boolean[] seen = new boolean[n];
        for (int r = 0; r < n; r++) {
            int start = suffixArray[r];
            if (start < 0 || start >= n || seen[start]) {
                throw new IllegalArgumentException("invalid suffix array permutation");
            }
            seen[start] = true;
            rank[start] = r;
        }
        int h = 0;
        long comparisons = 0;
        for (int i = 0; i < n; i++) {
            int r = rank[i];
            if (r == 0) {
                h = 0;
                continue;
            }
            int previousSuffix = suffixArray[r - 1];
            while (i + h < n && previousSuffix + h < n) {
                comparisons++;
                if (text.charAt(i + h) != text.charAt(previousSuffix + h)) {
                    break;
                }
                h++;
            }
            lcp[r] = h;
            if (h > 0) {
                h--;
            }
        }
        return new LcpResult(lcp, comparisons);
    }

    public static int[] suffixArray(String text) {
        Integer[] indices = new Integer[text.length()];
        for (int i = 0; i < text.length(); i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (left, right) -> {
            int i = left;
            int j = right;
            while (i < text.length() && j < text.length()) {
                int comparison = Character.compare(text.charAt(i++), text.charAt(j++));
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(text.length() - left, text.length() - right);
        });
        int[] result = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = indices[i];
        }
        return result;
    }
}
