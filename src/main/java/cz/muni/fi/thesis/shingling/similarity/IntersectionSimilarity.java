package cz.muni.fi.thesis.shingling.similarity;

import cz.muni.fi.thesis.shingling.Sequence;
import cz.muni.fi.thesis.shingling.Shingle;

import java.util.Map;

public class IntersectionSimilarity {

    /*public static double computeSimilarityNoWeights(boolean[] set1, boolean[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for JaccardSimilarity coefficient have different sizes.");
        }

        int intersection = 0;
        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] && set2[i]) {
                intersection += 1;
            }
        }
        return intersection;
    }

    public static double computeSimilarityIdfWeights(boolean[] set1, boolean[] set2) {
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for JaccardSimilarity coefficient have different sizes.");
        }

        double intersection = 0;
        Map<Integer, Double> idf = Sequence.getIdf();

        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] && set2[i]) {
                intersection += idf.get(i);
            }
        }
        return intersection;
    }*/
}
