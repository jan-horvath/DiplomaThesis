package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.episode.HmwEpisode;

import java.util.List;
import java.util.Map;

public class HmwShingleSimilarity {

    public static double DTW(HmwEpisode episode1, HmwEpisode episode2) {
        List<Integer> seq1 = episode1.getHmwSequence();
        List<Integer> seq2 = episode2.getHmwSequence();
        int N = seq1.size();
        int M = seq2.size();
        double[][] accumMatrix = new double[N][M];

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
        return 1.0 - accumMatrix[N-1][M-1];
    }

    public static double jaccardOnSet(HmwEpisode episode1, HmwEpisode episode2) {
        boolean[] set1 = episode1.toSet();
        boolean[] set2 = episode2.toSet();
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

    public static double jaccardOnBag(HmwEpisode episode1, HmwEpisode episode2) {
        double[] bag1 = episode1.toBag();
        double[] bag2 = episode2.toBag();
        if (bag1.length != bag2.length) {
            throw new IllegalArgumentException("Input arrays for JaccardSimilarity coefficient have different sizes.");
        }

        int intersection = 0;
        int union = 0;
        for (int i = 0; i < bag1.length; ++i) {
            union += bag1[i] + bag2[i];
            intersection += Math.min(bag1[i], bag2[i]);
        }
        return ((double) intersection)/union;
    }

    public static double jaccardOnIdf(HmwEpisode episode1, HmwEpisode episode2) {
        boolean[] set1 = episode1.toSet();
        boolean[] set2 = episode2.toSet();
        Map<Integer, Double> idf = HmwEpisode.getIdf();
        double intersection = 0.0;
        double union = 0.0;

        for (int i = 0; i < set1.length; ++i) {
            if ((set1[i]) || (set2[i])) {
                union += idf.get(i);
                if (set1[i] == set2[i]) {
                    intersection += idf.get(i);
                }
            }
        }

        return  intersection/union;
    }

    public static double cosineOnSet(HmwEpisode episode1, HmwEpisode episode2) {
        boolean[] set1 = episode1.toSet();
        boolean[] set2 = episode2.toSet();
        if (set1.length != set2.length) {
            throw new IllegalArgumentException("Input arrays for cosine similarity have different sizes.");
        }

        int intersection = 0;
        int vec1Magnitude = 0;
        int vec2Magnitude = 0;
        for (int i = 0; i < set1.length; ++i) {
            vec1Magnitude += set1[i] ? 1 : 0;
            vec2Magnitude += set2[i] ? 1 : 0;
            intersection += (set1[i] && set2[i]) ? 1 : 0;
        }
        return ((double) intersection)/Math.sqrt(vec1Magnitude * vec2Magnitude);
    }

    public static double cosineOnBag(HmwEpisode episode1, HmwEpisode episode2) {
        double[] vec1 = episode1.toBag();
        double[] vec2 = episode2.toBag();
        return cosine(vec1, vec2);
    }

    public static double cosineOnIdf(HmwEpisode episode1, HmwEpisode episode2) {
        double[] vec1 = episode1.toIdfWeights();
        double[] vec2 = episode2.toIdfWeights();
        return cosine(vec1, vec2);
    }

    public static double cosineOnTfIdf(HmwEpisode episode1, HmwEpisode episode2) {
        double[] vec1 = episode1.toTfIdfWeights();
        double[] vec2 = episode2.toTfIdfWeights();
        return cosine(vec1, vec2);
    }

    private static double cosine(double[] vec1, double[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Input arrays for cosine similarity have different sizes");
        }

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

}
