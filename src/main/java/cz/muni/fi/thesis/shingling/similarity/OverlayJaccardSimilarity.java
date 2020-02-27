package cz.muni.fi.thesis.shingling.similarity;

import cz.muni.fi.thesis.shingling.OverlaySequence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OverlayJaccardSimilarity {

    public static final int SET_EQUIVALENT = 1;
    public static final int COUNT_EACH_ONCE = 2;
    public static final int MULTISET_EQUIVALENT = 3;

    public static boolean overlayMotionWordsMatch(int[] mw1, int[] mw2, int matchingsRequired) {
        assert(mw1.length == mw2.length);
        int matchingsFound = 0;
        for (int i = 0; i < mw1.length; ++i) {
            if (mw1[i] == mw2[i]) {
                ++matchingsFound;
            }
        }
        return matchingsFound >= matchingsRequired;
    }

    public static double weighedOverlayJaccard3(OverlaySequence os1, OverlaySequence os2, int matchingsRequired) {
        Set<Integer> seq1_matched = new HashSet<>();
        Set<Integer> seq2_matched = new HashSet<>();

        double weightOfMatchedMotionWords = 0;
        for (int i = 0; i < os1.motionWords.size(); ++i) {
            for (int j = 0; j < os2.motionWords.size(); ++j) {
                if (overlayMotionWordsMatch(os1.motionWords.get(i), os2.motionWords.get(j), matchingsRequired)) {
                    if (!seq1_matched.contains(i)) {
                        seq1_matched.add(i);
                        weightOfMatchedMotionWords += os1.weights.get(i);
                    }
                    if (!seq2_matched.contains(j)) {
                        seq2_matched.add(j);
                        weightOfMatchedMotionWords += os2.weights.get(j);
                    }
                }
            }
        }

        double weightOfAllMotionWords = 0.0;
        for (int i = 0; i < os1.motionWords.size(); ++i) {weightOfAllMotionWords += os1.weights.get(i);}
        for (int i = 0; i < os2.motionWords.size(); ++i) {weightOfAllMotionWords += os2.weights.get(i);}

        return weightOfMatchedMotionWords/weightOfAllMotionWords;
    }

    public static double overlayJaccard3(List<int[]> seq1, List<int[]> seq2, int matchingsRequired) {
        Set<Integer> seq1_matched = new HashSet<>();
        Set<Integer> seq2_matched = new HashSet<>();
        double matchesFound = 0;
        for (int i = 0; i < seq1.size(); ++i) {
            for (int j = 0; j < seq2.size(); ++j) {
                if (overlayMotionWordsMatch(seq1.get(i), seq2.get(j), matchingsRequired)) {
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

    public static double overlayJaccard2(List<int[]> seq1, List<int[]> seq2, int matchingsRequired) {
        int matchCount = 0;
        for (int[] mw1 : seq1) {
            for (int[] mw2 : seq2) {
                if (overlayMotionWordsMatch(mw1, mw2, matchingsRequired)) {
                    ++matchCount;
                }
            }
        }
        return ((double) matchCount)/(seq1.size() * seq2.size());
    }

    public static double overlayJaccard1(List<int[]> seq1, List<int[]> seq2, int matchingsRequired) {
        boolean matchFound = false;
        int matchCount = 0;

        if (seq1.size() > seq2.size()) {
            List<int[]> tmp = seq1;
            seq1 = seq2;
            seq2 = tmp;
        }

        for (int[] mw1 : seq1) {
            for (int[] mw2 : seq2) {
                if (overlayMotionWordsMatch(mw1, mw2, matchingsRequired)) {
                    matchFound = true;
                    break;
                }
            }
            if (matchFound) {++matchCount;}
            matchFound = false;
        }
        return ((double) matchCount)/(seq1.size() + seq2.size() - matchCount);
    }

    private static double dtwDistance(List<int[]> seq1, List<int[]> seq2) {
        int N = seq1.size();
        int M = seq2.size();
        double accumMatrix[][] = new double[N][M];
        accumMatrix[0][0] = overlayMotionWordsMatch(seq1.get(0), seq2.get(0), 1) ? 0.0 : 1.0;
        
        for (int i = 1; i < N; ++i) {
            boolean match = overlayMotionWordsMatch(seq1.get(i), seq2.get(0), 1);
            accumMatrix[i][0] = (match ? 0.0 : 1.0) + accumMatrix[i-1][0];
        }

        for (int j = 1; j < M; ++j) {
            boolean match = overlayMotionWordsMatch(seq1.get(0), seq2.get(j), 1);
            accumMatrix[0][j] = (match ? 0.0 : 1.0) + accumMatrix[0][j-1];
        }

        //compute matrix values
        for (int i = 1; i < N; ++i) {
            for (int j = 1; j < M; ++j) {
                boolean match = overlayMotionWordsMatch(seq1.get(i), seq2.get(j), 1);
                accumMatrix[i][j] = (match ? 0.0 : 1.0) + Math.min(accumMatrix[i-1][j], Math.min(accumMatrix[i][j-1], accumMatrix[i-1][j-1]));
            }
        }
        return accumMatrix[N-1][M-1];
    }

    public static double dtwSimilarity(List<int[]> seq1, List<int[]> seq2) {
        return 1 - dtwDistance(seq1, seq2);
    }
}
