package cz.muni.fi.thesis.shingling;

import cz.muni.fi.thesis.shingling.similarity.CosineSimilarity;
import cz.muni.fi.thesis.shingling.similarity.JaccardSimilarity;

import java.util.*;

public class SimilarityMatrix {
    //Maybe change to Map<Integer, Map<Integer, SimilarityEntry>>
    private Map<Integer, List<SimilarityEntry>> matrix = new HashMap<>();

    public static class SimilarityEntry implements Comparable<SimilarityEntry> {
        public final int recordID;
        public final double jaccardValue;

        public SimilarityEntry(int recordID, double jaccardValue) {
            this.recordID = recordID;
            this.jaccardValue = jaccardValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimilarityEntry entry = (SimilarityEntry) o;
            return Double.compare(entry.jaccardValue, jaccardValue) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(jaccardValue);
        }

        @Override
        public int compareTo(SimilarityEntry o) {
            return Double.compare(jaccardValue, o.jaccardValue);
        }

        @Override
        public String toString() {
            return "(" + recordID + ", " + jaccardValue + ")";
        }
    }

    public static SimilarityMatrix createMatrixFromSets(Map<Integer, boolean[]> data, boolean weightedJaccard, boolean ignoreMaxIDFShingles) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        if (ignoreMaxIDFShingles) {
            for (Map.Entry<Integer, boolean[]> entry : data.entrySet()) {
                boolean[] set = entry.getValue();
                for (int i : ShingleUtility.getMaxIDFShingles()) {
                    set[i] = false;
                }
            }
        }

        for (Map.Entry<Integer, boolean[]> entry1 : data.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, boolean[]> entry2 : data.entrySet()) {
                double jaccardValue;
                if (weightedJaccard) {
                    jaccardValue = JaccardSimilarity.computeWeighedJaccard(entry1.getValue(), entry2.getValue(), ShingleUtility.getIDF());
                } else {
                    jaccardValue = JaccardSimilarity.computeJaccard(entry1.getValue(), entry2.getValue());
                }
                jaccardEntries.add(new SimilarityEntry(entry2.getKey(), jaccardValue));
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
            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, int[]> entry2 : data.entrySet()) {
                double jaccardValue = JaccardSimilarity.computeJaccardOnMinhashes(entry1.getValue(), entry2.getValue());
                jaccardEntries.add(new SimilarityEntry(entry2.getKey(), jaccardValue));
            }
        }
        return similarityMatrix;
    }

    public static SimilarityMatrix createMatrixFromMultisets(Map<Integer, int[]> data) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, int[]> entry1 : data.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            for (Map.Entry<Integer, int[]> entry2 : data.entrySet()) {
                double jaccardValue = JaccardSimilarity.computeJaccardOnMultisets(entry1.getValue(), entry2.getValue());
                jaccardEntries.add(new SimilarityEntry(entry2.getKey(), jaccardValue));
            }
        }
        return similarityMatrix;
    }

    public static SimilarityMatrix createMatrixFromOverlayData(Map<Integer, List<int[]>> overlayData, int matchingsRequired, boolean isSet) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, List<int[]>> entry1 : overlayData.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            if (isSet) {
                for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
                    double value = setSimilarityOfOverlayedRecordings(entry1.getValue(), entry2.getValue(), matchingsRequired);
                    jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
                }
            } else {
                for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
                    double value = multisetSimilarityOfOverlayedRecordings(entry1.getValue(), entry2.getValue(), matchingsRequired);
                    jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
                }
            }
        }
        return similarityMatrix;
    }

    public Map<Integer, List<SimilarityEntry>> getMatrix() {
        return matrix;
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
            if (matchFound) {++matchCount;}
            matchFound = false;
        }
        return ((double) matchCount)/(rec1.size() + rec2.size() - matchCount);
    }

    public enum MatrixType{GTSet, GTMultiset, TFIDF_TF, TFIDF_TFIDF, Set}

    public static SimilarityMatrix createMatrix(List<Sequence> sequences, MatrixType type) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (Sequence query : sequences) {
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (Sequence compareSequence : sequences) {
                double similarity;
                switch (type) {
                    case GTSet:  {
                        similarity = JaccardSimilarity.computeJaccard(query.getGroundTruthSet(), compareSequence.getGroundTruthSet());
                        break;
                    }
                    case GTMultiset: {
                        similarity = JaccardSimilarity.computeJaccardOnMultisets(query.toMultiset(), compareSequence.toMultiset());
                        break;
                    }
                    case TFIDF_TFIDF: {
                        similarity = CosineSimilarity.computeSimilarity(query.toTfIdfWeights(), compareSequence.toTfIdfWeights());
                        break;
                    }
                    case TFIDF_TF: {
                        similarity = CosineSimilarity.computeSimilarity(query.toTfIdfWeights(), compareSequence.toTfWeights());
                        break;
                    }
                    case Set: {
                        similarity = JaccardSimilarity.computeJaccard(query.toSet(), compareSequence.toSet());
                        break;
                    }
                    default: throw new IllegalStateException("This matrix type is not yet implemented!");
                }
                similarityEntries.add(new SimilarityEntry(compareSequence.getId(), similarity));
            }
        }
        return sm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, List<SimilarityEntry>> entry : matrix.entrySet()) {
            sb.append(entry.getKey()).append(": ");
            for (SimilarityEntry similarityEntry : entry.getValue()) {
                sb.append(similarityEntry.toString()).append(", ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
