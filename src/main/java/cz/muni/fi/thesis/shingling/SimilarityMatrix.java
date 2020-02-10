package cz.muni.fi.thesis.shingling;

import java.util.*;

public class SimilarityMatrix {
    private Map<Integer, List<JaccardEntry>> matrix = new HashMap<>();

    public static SimilarityMatrix createMatrixFromSets(Map<Integer, boolean[]> data, boolean weightedJaccard, boolean ignoreMaxIDFShingles) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, boolean[]> entry1 : data.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<JaccardEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, boolean[]> entry2 : data.entrySet()) {
                double jaccardValue;
                if (weightedJaccard) {
                    jaccardValue = Jaccard.computeWeighedJaccard(entry1.getValue(), entry2.getValue(), Shingles.getIDF());
                } else {
                    jaccardValue = Jaccard.computeJaccard(entry1.getValue(), entry2.getValue(), ignoreMaxIDFShingles);
                }
                jaccardEntries.add(new JaccardEntry(entry2.getKey(), jaccardValue));
            }
        }
        return similarityMatrix;
    }

    public static SimilarityMatrix createMatrixFromSets(Map<Integer, boolean[]> data, boolean weightedJaccard) {
        return createMatrixFromSets(data, weightedJaccard, false);
    }

    public static SimilarityMatrix createMatrixFromMinhashes(Map<Integer, int[]> data) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, int[]> entry1 : data.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<JaccardEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, int[]> entry2 : data.entrySet()) {
                double jaccardValue = Jaccard.computeJaccardOnMinhashes(entry1.getValue(), entry2.getValue());
                jaccardEntries.add(new JaccardEntry(entry2.getKey(), jaccardValue));
            }
        }
        return similarityMatrix;
    }

    public static SimilarityMatrix createMatrixFromMultisets(Map<Integer, int[]> data) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, int[]> entry1 : data.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<JaccardEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, int[]> entry2 : data.entrySet()) {
                double jaccardValue = Jaccard.computeJaccardOnMultisets(entry1.getValue(), entry2.getValue());
                jaccardEntries.add(new JaccardEntry(entry2.getKey(), jaccardValue));
            }
        }
        return similarityMatrix;
    }

    private static boolean overlayMotionWordsMatch(int[] mw1, int[] mw2, int matchingsRequired) {
        assert(mw1.length == mw2.length);
        int matchingsFound = 0;
        for (int i = 0; i < mw1.length; ++i) {
            if (mw1[i] == mw2[i]) {
                ++matchingsFound;
            }
        }
        return matchingsFound >= matchingsRequired;
    }

    private static double multisetSimilarityOfOverlayedRecordings(List<int[]> rec1, List<int[]> rec2, int matchingsRequired) {
        int matchCount = 0;
        for (int[] mw1 : rec1) {
            for (int[] mw2 : rec2) {
                if (overlayMotionWordsMatch(mw1, mw2, matchingsRequired)) {
                    ++matchCount;
                }
            }
        }
        return ((double) matchCount)/(rec1.size() * rec2.size());
    }

    private static double setSimilarityOfOverlayedRecordings(List<int[]> rec1, List<int[]> rec2, int matchingsRequired) {
        boolean matchFound = false;
        int matchCount = 0;

        if (rec1.size() > rec2.size()) {
            List<int[]> tmp = rec1;
            rec1 = rec2;
            rec2 = tmp;
        }

        for (int[] mw1 : rec1) {
            for (int[] mw2 : rec2) {
                if (overlayMotionWordsMatch(mw1, mw2, matchingsRequired)) {
                    matchFound = true;
                    break;
                }
            }
            if (matchFound) ++matchCount;
            matchFound = false;
        }
        return ((double) matchCount)/(rec1.size() + rec2.size() - matchCount);
    }

    public static SimilarityMatrix createMatrixFromOverlayData(Map<Integer, List<int[]>> overlayData, int matchingsRequired, boolean isSet) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, List<int[]>> entry1 : overlayData.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<JaccardEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            if (isSet) {
                for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
                    double value = setSimilarityOfOverlayedRecordings(entry1.getValue(), entry2.getValue(), matchingsRequired);
                    jaccardEntries.add(new JaccardEntry(entry2.getKey(), value));
                }
            } else {
                for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
                    double value = multisetSimilarityOfOverlayedRecordings(entry1.getValue(), entry2.getValue(), matchingsRequired);
                    jaccardEntries.add(new JaccardEntry(entry2.getKey(), value));
                }
            }
        }
        return similarityMatrix;
    }

    public Map<Integer, List<JaccardEntry>> getMatrix() {
        return matrix;
    }

    public static class JaccardEntry implements Comparable<JaccardEntry> {
        public final int recordID;
        public final double jaccardValue;

        public JaccardEntry(int recordID, double jaccardValue) {
            this.recordID = recordID;
            this.jaccardValue = jaccardValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JaccardEntry entry = (JaccardEntry) o;
            return Double.compare(entry.jaccardValue, jaccardValue) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(jaccardValue);
        }

        @Override
        public int compareTo(JaccardEntry o) {
            return Double.compare(jaccardValue, o.jaccardValue);
        }

        @Override
        public String toString() {
            return "(" + recordID + ", " + jaccardValue + ")";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<JaccardEntry>> entry : matrix.entrySet()) {
            sb.append(entry.getKey()).append(": ");
            for (JaccardEntry jaccardEntry : entry.getValue()) {
                sb.append(jaccardEntry.toString()).append(", ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
