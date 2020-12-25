package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.sequences.MomwEpisode;

import java.util.List;

public class MomwSimilarity {
    private static int MATCHINGS_REQUIRED = 1;

    //TODO consider moving this to some utility class and rename it
    private static boolean overlayMotionWordsMatch(MomwEpisode.MOMW momw1, MomwEpisode.MOMW momw2, int matchingsRequired) {
        /*int[] mw1 = momw1.getMW();
        int[] mw2 = momw2.getMW();
        assert(mw1.length == mw2.length);

        int matchingsFound = 0;
        for (int i = 0; i < mw1.length; ++i) {
            if (mw1[i] == mw2[i]) {
                ++matchingsFound;
            }
        }
        return matchingsFound >= matchingsRequired;*/
        return overlayMotionWordsMatch(momw1.getMW(), momw2.getMW(), matchingsRequired);
    }

    public static boolean overlayMotionWordsMatch(int[] MOMW1, int[] MOMW2, int matchingsRequired) {
        assert(MOMW1.length == MOMW2.length);
        int matchingsFound = 0;
        for (int i = 0; i < MOMW1.length; ++i) {
            if (MOMW1[i] == MOMW2[i]) {
                ++matchingsFound;
            }
        }
        return matchingsFound >= matchingsRequired;
    }

    public static double dtwSimilarity(MomwEpisode episode1, MomwEpisode episode2) {
        List<MomwEpisode.MOMW> seq1 = episode1.getSequence();
        List<MomwEpisode.MOMW> seq2 = episode2.getSequence();
        int N = seq1.size();
        int M = seq2.size();
        double[][] accumMatrix = new double[N][M];

        accumMatrix[0][0] = overlayMotionWordsMatch(seq1.get(0), seq2.get(0), MATCHINGS_REQUIRED) ? 0.0 : 1.0;

        for (int i = 1; i < N; ++i) {
            boolean match = overlayMotionWordsMatch(seq1.get(i), seq2.get(0), MATCHINGS_REQUIRED);
            accumMatrix[i][0] = (match ? 0.0 : 1.0) + accumMatrix[i-1][0];
        }

        for (int j = 1; j < M; ++j) {
            boolean match = overlayMotionWordsMatch(seq1.get(0), seq2.get(j), MATCHINGS_REQUIRED);
            accumMatrix[0][j] = (match ? 0.0 : 1.0) + accumMatrix[0][j-1];
        }

        //compute matrix values
        for (int i = 1; i < N; ++i) {
            for (int j = 1; j < M; ++j) {
                boolean match = overlayMotionWordsMatch(seq1.get(i), seq2.get(j), MATCHINGS_REQUIRED);
                accumMatrix[i][j] = (match ? 0.0 : 1.0) + Math.min(accumMatrix[i-1][j], Math.min(accumMatrix[i][j-1], accumMatrix[i-1][j-1]));
            }
        }
        return 1.0 - accumMatrix[N-1][M-1];
    }
}
