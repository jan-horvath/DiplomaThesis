package cz.muni.fi.thesis.similarity;

import java.util.Map;

/**
 * This class contains static function which compute jaccard coefficients
 */
public class JaccardSimilarity {
    /**
     * computeJaccard computes JaccardSimilarity coefficient on two sets. The sets are represented by boolean arrays (bitmaps).
     * set[i] = true means that the element i belongs to the set
     * @param set1 first set
     * @param set2 second set
     * @return JaccardSimilarity coefficient for sets. This coefficient is in range (0,1)
     */
    public static double computeJaccard(boolean[] set1, boolean[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for JaccardSimilarity coefficient have different sizes.");
        }

        int intersection = 0;
        int union = 0;

        for (int i = 0; i < set1.length; ++i) {
            if ((set1[i]) || (set2[i])) {
                ++union;
                if (set1[i] == set2[i]) {
                    ++intersection;
                }
            }
        }
        return ((double) intersection)/union;
    }

    /**
     * Weighted version of Jaccard. Works for weights which are independent of a sequence, i.e. IDF
     */
    public static double computeWeighedJaccard(boolean[] set1, boolean[] set2, Map<Integer, Double> weights) {
        double intersection = 0.0;
        double union = 0.0;

        for (int i = 0; i < set1.length; ++i) {
            if ((set1[i]) || (set2[i])) {
                union += weights.get(i);
                if (set1[i] == set2[i]) {
                    intersection += weights.get(i);
                }
            }
        }

        return  intersection/union;
    }

    /**
     * Weighted version of a Jaccard. Works even for weights which are dependent on a sequence
     */
    public static double weighedJaccard3(double[] set1, double[] set2) {
        double weightOfMatchedMotionWords = 0.0;
        double weightOfAllWords = 0.0;

        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] != 0.0 && set2[i] != 0.0) {
                weightOfMatchedMotionWords += set1[i] + set2[i];
            }
            weightOfAllWords += set1[i] + set2[i];
        }

        return weightOfMatchedMotionWords/weightOfAllWords;
    }

    /**
     * computeJaccardOnMinhashes computes JaccardSimilarity coefficient on minhashes. The sets are represented by integer arrays.
     * set[i] = k means, that the index of the first item contained in the set according to permutation i is k
     * @param set1 first set
     * @param set2 second set
     * @return JaccardSimilarity coefficient for minhashes. The value is increased for each i such that set1[i] == set2[i].
     * Result is in range (0,1)
     */
    public static double computeJaccardOnMinhashes(int[] set1, int[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for JaccardSimilarity coefficient have different sizes.");
        }

        int intersection = 0;
        int union = set1.length;

        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] == set2[i]) {
                ++intersection;
            }
        }
        return ((double) intersection)/union;
    }

    /**
     * computeJaccardOnMultisets computes JaccardSimilarity coefficient on multisets. The sets are represented by integer arrays.
     * set[i] = k means, that the i-th element is present k times in the set.
     * @param set1 first set
     * @param set2 second set
     * @return JaccardSimilarity coefficient for multisets. Result is in range (0,0.5).
     */
    public static double computeJaccardOnMultisets(int[] set1, int[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for JaccardSimilarity coefficient have different sizes.");
        }

        int intersection = 0;
        int union = 0;
        for (int i = 0; i < set1.length; ++i) {
            union += set1[i] + set2[i];
            intersection += Math.min(set1[i], set2[i]);
        }
        return ((double) intersection)/union;
    }
}
