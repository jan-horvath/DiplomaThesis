public class Jaccard {
    public static double computeJaccard(boolean[] set1, boolean[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for Jaccard coefficient have different sizes.");
        }

        int matching = 0;
        for (int i = 0; i < set1.length; ++i) {
            if ((set1[i]) && (set1[i] == set2[i])) {
                ++matching;
            }
        }
        return ((double) matching)/set1.length;
    }

    public static double computeJaccardOnMinhashes(int[] set1, int[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for Jaccard coefficient have different sizes.");
        }

        int matching = 0;
        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] == set2[i]) {
                ++matching;
            }
        }
        return ((double) matching)/set1.length;
    }

    public static double computeJaccardOnMultisets(int[] set1, int[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for Jaccard coefficient have different sizes.");
        }

        int matching = 0;
        int total = 0;
        for (int i = 0; i < set1.length; ++i) {
            total += set1[i] + set2[i];
            matching += Math.min(set1[i], set2[i]);
        }
        return ((double) matching)/total;
    }
}
