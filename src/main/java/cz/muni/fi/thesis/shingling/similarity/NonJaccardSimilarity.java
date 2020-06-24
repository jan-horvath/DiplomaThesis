package cz.muni.fi.thesis.shingling.similarity;

import cz.muni.fi.thesis.shingling.Sequence;

import java.util.List;
import java.util.Map;

public class NonJaccardSimilarity {

    public static double computeSimilarity(double[] vec1, double[] vec2) {
        assert (vec1.length == vec2.length);

        double vec1Magnitude = 0.0;
        double vec2Magnitude = 0.0;
        double dotProduct = 0.0;

        for (int i = 0; i < vec1.length; ++i) {
            dotProduct += vec1[i] * vec2[i];
            vec1Magnitude += vec1[i] * vec1[i];
            vec2Magnitude += vec2[i] * vec2[i];
        }
        return dotProduct / Math.sqrt(vec1Magnitude * vec2Magnitude);
    }

    public static double computeSimilarityNoWeights(boolean[] set1, boolean[] set2) {
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
    }

    private static double dtwDistance(List<Integer> seq1, List<Integer> seq2) {
        int N = seq1.size();
        int M = seq2.size();
        double accumMatrix[][] = new double[N][M];
        accumMatrix[0][0] = (seq1.get(0).equals(seq2.get(0))) ? 0.0 : 1.0;

        for (int i = 1; i < N; ++i) {
            boolean match = seq1.get(i).equals(seq2.get(0));
            accumMatrix[i][0] = (match ? 0.0 : 1.0) + accumMatrix[i-1][0];
        }

        for (int j = 1; j < M; ++j) {
            boolean match = seq1.get(0).equals(seq2.get(j));
            accumMatrix[0][j] = (match ? 0.0 : 1.0) + accumMatrix[0][j-1];
        }

        //compute matrix values
        for (int i = 1; i < N; ++i) {
            for (int j = 1; j < M; ++j) {
                boolean match = seq1.get(i).equals(seq2.get(j));
                accumMatrix[i][j] = (match ? 0.0 : 1.0) + Math.min(accumMatrix[i-1][j], Math.min(accumMatrix[i][j-1], accumMatrix[i-1][j-1]));
            }
        }
        return accumMatrix[N-1][M-1];
    }

    public static double dtwSimilarity(List<Integer> seq1, List<Integer> seq2) {
        return 1 - dtwDistance(seq1, seq2);
    }
}
