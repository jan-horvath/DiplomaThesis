package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.sequences.HmwEpisode;

import java.util.List;

public class HmwShingleSimilarity {

    public static double dtwSimilarity(HmwEpisode episode1, HmwEpisode episode2) {
        List<Integer> seq1 = episode1.getSequence();
        List<Integer> seq2 = episode2.getSequence();
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

//    public static double dtwSimilarity(List<Integer> seq1, List<Integer> seq2) {
//        return 1 - dtwDistance(seq1, seq2);
//    }
}
