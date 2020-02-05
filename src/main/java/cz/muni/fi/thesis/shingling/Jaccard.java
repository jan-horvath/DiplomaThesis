package cz.muni.fi.thesis.shingling;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * This class contains static function which compute jaccard coefficients
 */
public class Jaccard {

    public static double oneShingleInfluenceAverage = 0;
    public static int nonEmptyIntersections = 0;
    /**
     * computeJaccard computes Jaccard coefficient on two sets. The sets are represented by boolean arrays (bitmaps).
     * set[i] = true means that the element i belongs to the set
     * @param set1 first set
     * @param set2 second set
     * @return Jaccard coefficient for sets. This coefficient is in range (0,1)
     */
    public static double computeJaccard(boolean[] set1, boolean[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for Jaccard coefficient have different sizes.");
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

    public static double computeWeighedJaccard(boolean[] set1, boolean[] set2, Map<Integer, Double> weights) {
        double intersection = 0.0;
        double union = 0.0;

        double oneShingleInfluence = 0.0;

        for (int i = 0; i < set1.length; ++i) {
            if ((set1[i]) || (set2[i])) {
                union += weights.get(i);
                if (set1[i] == set2[i]) {
                    intersection += weights.get(i);
                    if (Shingles.getShingleFromID(i).getSize() == 1) {
                        oneShingleInfluence += weights.get(i);
                    }
                }
            }
        }
        if (intersection != 0.0) {
            oneShingleInfluenceAverage += oneShingleInfluence / intersection;
            nonEmptyIntersections++;
        }
        return  intersection/union;
    }

    /**
     * computeJaccardOnMinhashes computes Jaccard coefficient on minhashes. The sets are represented by integer arrays.
     * set[i] = k means, that the index of the first item contained in the set according to permutation i is k
     * @param set1 first set
     * @param set2 second set
     * @return Jaccard coefficient for minhashes. The value is increased for each i such that set1[i] == set2[i].
     * Result is in range (0,1)
     */
    public static double computeJaccardOnMinhashes(int[] set1, int[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for Jaccard coefficient have different sizes.");
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
     * computeJaccardOnMultisets computes Jaccard coefficient on multisets. The sets are represented by integer arrays.
     * set[i] = k means, that the i-th element is present k times in the set.
     * @param set1 first set
     * @param set2 second set
     * @return Jaccard coefficient for multisets. Result is in range (0,0.5).
     */
    public static double computeJaccardOnMultisets(int[] set1, int[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for Jaccard coefficient have different sizes.");
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
