package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.sequences.MomwEpisode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MomwSimilarity {
    private static int MATCHINGS_REQUIRED = 1;

    //TODO consider moving this to some utility class and rename it
    private static boolean overlayMotionWordsMatch(MomwEpisode.MOMW momw1, MomwEpisode.MOMW momw2, int matchingsRequired) {
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

    public static double jaccardOnSets(MomwEpisode episode1, MomwEpisode episode2) {
        Set<MomwEpisode.MOMW> set1_matched = new HashSet<>();
        Set<MomwEpisode.MOMW> set2_matched = new HashSet<>();
        double matchesFound = 0.0;

        for (MomwEpisode.MOMW mw1 : episode1.getSet()) {
            for (MomwEpisode.MOMW mw2 : episode2.getSet()) {
                if (overlayMotionWordsMatch(mw1, mw2, MATCHINGS_REQUIRED)) {
                    if (!set1_matched.contains(mw1)) {
                        set1_matched.add(mw1);
                        matchesFound += 1.0;
                    }
                    if (!set2_matched.contains(mw2)) {
                        set2_matched.add(mw2);
                        matchesFound += 1.0;
                    }
                }
            }
        }
        return matchesFound/(episode1.getSet().size() + episode2.getSet().size());
    }

    public static double jaccardOnTF(MomwEpisode episode1, MomwEpisode episode2) {
        Set<Integer> seq1_matched = new HashSet<>();
        Set<Integer> seq2_matched = new HashSet<>();

        List<MomwEpisode.MOMW> seq1 = episode1.getSequence();
        List<MomwEpisode.MOMW> seq2 = episode2.getSequence();
        double matchesFound = 0.0;

        for (int i = 0; i < seq1.size(); ++i) {
            for (int j = 0; j < seq2.size(); ++j) {
                if (overlayMotionWordsMatch(seq1.get(i), seq2.get(j), MATCHINGS_REQUIRED)) {
                    if (!seq1_matched.contains(i)) {
                        seq1_matched.add(i);
                        matchesFound += 1.0;
                    }
                    if (!seq2_matched.contains(j)) {
                        seq2_matched.add(j);
                        matchesFound += 1.0;
                    }
                }
            }
        }
        return matchesFound/(seq1.size() + seq2.size());
    }

    public static double jaccardOnIdf(MomwEpisode episode1, MomwEpisode episode2) {
        Set<MomwEpisode.MOMW> seq1_matched = new HashSet<>();
        Set<MomwEpisode.MOMW> seq2_matched = new HashSet<>();

        double weightOfMatchedMotionWords = 0.0;
        for (MomwEpisode.MOMW mw1 : episode1.getSet()) {
            for (MomwEpisode.MOMW mw2 : episode2.getSet()) {
                if (overlayMotionWordsMatch(mw1, mw2, MATCHINGS_REQUIRED)) {
                    if (!seq1_matched.contains(mw1)) {
                        seq1_matched.add(mw1);
                        weightOfMatchedMotionWords += mw1.getIDF();
                    }
                    if (!seq2_matched.contains(mw2)) {
                        seq2_matched.add(mw2);
                        weightOfMatchedMotionWords += mw2.getIDF();
                    }
                }
            }
        }

        double weightOfAllMotionWords = 0.0;
        for (MomwEpisode.MOMW momw : episode1.getSet()) {weightOfAllMotionWords += momw.getIDF();}
        for (MomwEpisode.MOMW momw : episode2.getSet()) {weightOfAllMotionWords += momw.getIDF();}

        return weightOfMatchedMotionWords/weightOfAllMotionWords;
    }

    public static double jaccardOnTfIdf(MomwEpisode episode1, MomwEpisode episode2) {
        Set<Integer> seq1_matched = new HashSet<>();
        Set<Integer> seq2_matched = new HashSet<>();

        List<MomwEpisode.MOMW> seq1 = episode1.getSequence();
        List<MomwEpisode.MOMW> seq2 = episode2.getSequence();
        double weightOfMatchedMotionWords = 0.0;

        for (int i = 0; i < seq1.size(); ++i) {
            for (int j = 0; j < seq2.size(); ++j) {
                if (overlayMotionWordsMatch(seq1.get(i), seq2.get(j), MATCHINGS_REQUIRED)) {
                    if (!seq1_matched.contains(i)) {
                        seq1_matched.add(i);
                        weightOfMatchedMotionWords += seq1.get(i).getIDF();
                    }
                    if (!seq2_matched.contains(j)) {
                        seq2_matched.add(j);
                        weightOfMatchedMotionWords += seq2.get(j).getIDF();
                    }
                }
            }
        }

        double weightOfAllMotionWords = 0.0;
        for (MomwEpisode.MOMW momw : episode1.getSequence()) {weightOfAllMotionWords += momw.getIDF();}
        for (MomwEpisode.MOMW momw : episode2.getSequence()) {weightOfAllMotionWords += momw.getIDF();}

        return weightOfMatchedMotionWords/weightOfAllMotionWords;
    }
}
