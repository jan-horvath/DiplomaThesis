package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.sequences.OverlaySequence;

import java.util.*;

public class OverlaySimilarity {

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

    /**
     * Jaccard inspired weighted similarity for MOMWs (based on the non-weighed vesion 3)
     */
//    public static double weighedOverlayJaccard3(OverlaySequence os1, OverlaySequence os2, int matchingsRequired) {
//        Set<int[]> seq1_matched = new HashSet<>();
//        Set<int[]> seq2_matched = new HashSet<>();
//
//        double weightOfMatchedMotionWords = 0;
//        for (Map.Entry<OverlaySequence.MOMW5, Double> mw1Entry : os1.getMotionWords().entrySet()) {
//            int[] mw1 = mw1Entry.getKey().motionWord;
//            for (Map.Entry<OverlaySequence.MOMW5, Double> mw2Entry : os2.getMotionWords().entrySet()) {
//                int[] mw2 = mw2Entry.getKey().motionWord;
//                if (overlayMotionWordsMatch(mw1, mw2, matchingsRequired)) {
//                    if (!seq1_matched.contains(mw1)) {
//                        seq1_matched.add(mw1);
//                        weightOfMatchedMotionWords += mw1Entry.getValue();
//                    }
//                    if (!seq2_matched.contains(mw2)) {
//                        seq2_matched.add(mw2);
//                        weightOfMatchedMotionWords += mw2Entry.getValue();
//                    }
//                }
//            }
//        }
//
//        double weightOfAllMotionWords = 0.0;
//        for (Double weight : os1.getMotionWords().values()) {weightOfAllMotionWords += weight;}
//        for (Double weight : os2.getMotionWords().values()) {weightOfAllMotionWords += weight;}
//
//        return weightOfMatchedMotionWords/weightOfAllMotionWords;
//    }

    /**
     * Jaccard inspired similarity for MOMWs (version 3)
     * This version has the best results
     */
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

//    private static double dtwDistance(List<int[]> seq1, List<int[]> seq2, int matchingsRequired) {
//        int N = seq1.size();
//        int M = seq2.size();
//        double accumMatrix[][] = new double[N][M];
//        accumMatrix[0][0] = overlayMotionWordsMatch(seq1.get(0), seq2.get(0), matchingsRequired) ? 0.0 : 1.0;
//
//        for (int i = 1; i < N; ++i) {
//            boolean match = overlayMotionWordsMatch(seq1.get(i), seq2.get(0), matchingsRequired);
//            accumMatrix[i][0] = (match ? 0.0 : 1.0) + accumMatrix[i-1][0];
//        }
//
//        for (int j = 1; j < M; ++j) {
//            boolean match = overlayMotionWordsMatch(seq1.get(0), seq2.get(j), matchingsRequired);
//            accumMatrix[0][j] = (match ? 0.0 : 1.0) + accumMatrix[0][j-1];
//        }
//
//        //compute matrix values
//        for (int i = 1; i < N; ++i) {
//            for (int j = 1; j < M; ++j) {
//                boolean match = overlayMotionWordsMatch(seq1.get(i), seq2.get(j), matchingsRequired);
//                accumMatrix[i][j] = (match ? 0.0 : 1.0) + Math.min(accumMatrix[i-1][j], Math.min(accumMatrix[i][j-1], accumMatrix[i-1][j-1]));
//            }
//        }
//        return accumMatrix[N-1][M-1];
//    }
//
//    public static double dtwSimilarity(List<int[]> seq1, List<int[]> seq2, int matchingsRequired) {
//        return 1 - dtwDistance(seq1, seq2, matchingsRequired);
//    }

    private static double dtwDistance(List<OverlaySequence.MOMW5> seq1, List<OverlaySequence.MOMW5> seq2, int matchingsRequired) {
        int N = seq1.size();
        int M = seq2.size();
        double accumMatrix[][] = new double[N][M];
        accumMatrix[0][0] = overlayMotionWordsMatch(seq1.get(0).getMotionWord(), seq2.get(0).getMotionWord(), matchingsRequired) ? 0.0 : 1.0;

        for (int i = 1; i < N; ++i) {
            boolean match = overlayMotionWordsMatch(seq1.get(i).getMotionWord(), seq2.get(0).getMotionWord(), matchingsRequired);
            accumMatrix[i][0] = (match ? 0.0 : 1.0) + accumMatrix[i-1][0];
        }

        for (int j = 1; j < M; ++j) {
            boolean match = overlayMotionWordsMatch(seq1.get(0).getMotionWord(), seq2.get(j).getMotionWord(), matchingsRequired);
            accumMatrix[0][j] = (match ? 0.0 : 1.0) + accumMatrix[0][j-1];
        }

        //compute matrix values
        for (int i = 1; i < N; ++i) {
            for (int j = 1; j < M; ++j) {
                boolean match = overlayMotionWordsMatch(seq1.get(i).getMotionWord(), seq2.get(j).getMotionWord(), matchingsRequired);
                accumMatrix[i][j] = (match ? 0.0 : 1.0) + Math.min(accumMatrix[i-1][j], Math.min(accumMatrix[i][j-1], accumMatrix[i-1][j-1]));
            }
        }
        return accumMatrix[N-1][M-1];
    }

    public static double dtwSimilarity(List<OverlaySequence.MOMW5> seq1, List<OverlaySequence.MOMW5> seq2, int matchingsRequired) {
        return 1 - dtwDistance(seq1, seq2, matchingsRequired);
    }
}

