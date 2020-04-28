package cz.muni.fi.thesis.shingling.similarity;

import cz.muni.fi.thesis.shingling.Sequence;
import cz.muni.fi.thesis.shingling.ShingleUtility;

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

    public static SimilarityMatrix createMatrixFromOverlayData(Map<Integer, List<int[]>> overlayData, int matchingsRequired, int overlayJaccardNumber) {
        SimilarityMatrix similarityMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, List<int[]>> entry1 : overlayData.entrySet()) {
            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());

            switch (overlayJaccardNumber) {
                case OverlayJaccardSimilarity.SET_EQUIVALENT : {
                    for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
                        double value = OverlayJaccardSimilarity.overlayJaccard1(entry1.getValue(), entry2.getValue(), matchingsRequired);
                        jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
                    }
                    break;
                }
                case OverlayJaccardSimilarity.COUNT_EACH_ONCE : {
                    for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
                        double value = OverlayJaccardSimilarity.overlayJaccard3(entry1.getValue(), entry2.getValue(), matchingsRequired);
                        jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
                    }
                    break;
                }
                case OverlayJaccardSimilarity.MULTISET_EQUIVALENT : {
                    for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
                        double value = OverlayJaccardSimilarity.overlayJaccard2(entry1.getValue(), entry2.getValue(), matchingsRequired);
                        jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
                    }
                    break;
                }
            }
        }
        return similarityMatrix;
    }

    public Map<Integer, List<SimilarityEntry>> getMatrix() {
        return matrix;
    }



    public static SimilarityMatrix createMatrix(List<Sequence> sequences, MatrixType type) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (Sequence query : sequences) {
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (Sequence compareSequence : sequences) {
                double similarity;
                switch (type) { //make this switch into a private function
                    case SET: {
                        similarity = JaccardSimilarity.computeJaccard(query.toSet(false), compareSequence.toSet(false));
                        break;
                    }
                    case SET_IGNORE: {
                        similarity = JaccardSimilarity.computeJaccard(query.toSet(true), compareSequence.toSet(true));
                        break;
                    }
                    case MULTISET: {
                        similarity = JaccardSimilarity.computeJaccardOnMultisets(query.toMultiset(false), compareSequence.toMultiset(false));
                        break;
                    }
                    case MULTISET_IGNORE: {
                        similarity = JaccardSimilarity.computeJaccardOnMultisets(query.toMultiset(true), compareSequence.toMultiset(true));
                        break;
                    }
                    case IDF: {
                        similarity = JaccardSimilarity.computeWeighedJaccard(query.toSet(false), compareSequence.toSet(false), Sequence.getIdf());
                        break;
                    }
                    case IDF_IGNORE: {
                        similarity = JaccardSimilarity.computeWeighedJaccard(query.toSet(true), compareSequence.toSet(true), Sequence.getIdf());
                        break;
                    }
                    case TFIDF_TFIDF: {
                        similarity = NonJaccardSimilarity.computeSimilarity(query.toTfIdfWeights(false), compareSequence.toTfIdfWeights(false));
                        break;
                    }
                    case TFIDF_TFIDF_IGNORE: {
                        similarity = NonJaccardSimilarity.computeSimilarity(query.toTfIdfWeights(true), compareSequence.toTfIdfWeights(true));
                        break;
                    }
                    case TFIDF_TF_IGNORE: {
                        similarity = NonJaccardSimilarity.computeSimilarity(query.toTfIdfWeights(true), compareSequence.toTfWeights(true));
                        break;
                    }
                    case INTERSECTION: {
                        similarity = NonJaccardSimilarity.computeSimilarityNoWeights(query.toSet(false), compareSequence.toSet(false));
                        break;
                    } case INTERSECTION_IGNORE: {
                        similarity = NonJaccardSimilarity.computeSimilarityNoWeights(query.toSet(true), compareSequence.toSet(true));
                        break;
                    } case INTERSECTION_IDF: {
                        similarity = NonJaccardSimilarity.computeSimilarityIdfWeights(query.toSet(false), compareSequence.toSet(false));
                        break;
                    } case INTERSECTION_IDF_IGNORE: {
                        similarity = NonJaccardSimilarity.computeSimilarityIdfWeights(query.toSet(true), compareSequence.toSet(true));
                        break;
                    } case DTW: {
                        similarity = NonJaccardSimilarity.dtwSimilarity(query.getSequence(), compareSequence.getSequence());
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
