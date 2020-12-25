package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.sequences.OverlaySequence;
import cz.muni.fi.thesis.sequences.Sequence;

import java.util.*;
import java.util.function.BiFunction;

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

//    public static SimilarityMatrix createMatrixFromSets(Map<Integer, boolean[]> data, boolean weightedJaccard) {
//        SimilarityMatrix similarityMatrix = new SimilarityMatrix();
//
//        for (Map.Entry<Integer, boolean[]> entry1 : data.entrySet()) {
//            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
//            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());
//
//            for (Map.Entry<Integer, boolean[]> entry2 : data.entrySet()) {
//                double jaccardValue;
//                if (weightedJaccard) {
//                    jaccardValue = JaccardSimilarity.computeWeighedJaccard(entry1.getValue(), entry2.getValue(), ShingleUtility.getIDF());
//                } else {
//                    jaccardValue = JaccardSimilarity.computeJaccard(entry1.getValue(), entry2.getValue());
//                }
//                jaccardEntries.add(new SimilarityEntry(entry2.getKey(), jaccardValue));
//            }
//        }
//        return similarityMatrix;
//    }
//
//    public static SimilarityMatrix createMatrixFromMultisets(Map<Integer, int[]> data) {
//        SimilarityMatrix similarityMatrix = new SimilarityMatrix();
//
//        for (Map.Entry<Integer, int[]> entry1 : data.entrySet()) {
//            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
//            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());
//
//            for (Map.Entry<Integer, int[]> entry2 : data.entrySet()) {
//                double jaccardValue = JaccardSimilarity.computeJaccardOnMultisets(entry1.getValue(), entry2.getValue());
//                jaccardEntries.add(new SimilarityEntry(entry2.getKey(), jaccardValue));
//            }
//        }
//        return similarityMatrix;
//    }
//
//    public static SimilarityMatrix createMatrixFromOverlayData(Map<Integer, List<int[]>> overlayData, int matchingsRequired, int overlayJaccardNumber) {
//        SimilarityMatrix similarityMatrix = new SimilarityMatrix();
//
//        for (Map.Entry<Integer, List<int[]>> entry1 : overlayData.entrySet()) {
//            similarityMatrix.matrix.put(entry1.getKey(), new ArrayList<>());
//            List<SimilarityEntry> jaccardEntries = similarityMatrix.matrix.get(entry1.getKey());
//
//            switch (overlayJaccardNumber) {
//                case OverlaySimilarity.SET_EQUIVALENT : {
//                    for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
//                        double value = OverlaySimilarity.overlayJaccard1(entry1.getValue(), entry2.getValue(), matchingsRequired);
//                        jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
//                    }
//                    break;
//                }
//                case OverlaySimilarity.COUNT_EACH_ONCE : {
//                    for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
//                        double value = OverlaySimilarity.overlayJaccard3(entry1.getValue(), entry2.getValue(), matchingsRequired);
//                        jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
//                    }
//                    break;
//                }
//                case OverlaySimilarity.MULTISET_EQUIVALENT : {
//                    for (Map.Entry<Integer, List<int[]>> entry2 : overlayData.entrySet()) {
//                        double value = OverlaySimilarity.overlayJaccard2(entry1.getValue(), entry2.getValue(), matchingsRequired);
//                        jaccardEntries.add(new SimilarityEntry(entry2.getKey(), value));
//                    }
//                    break;
//                }
//            }
//        }
//        return similarityMatrix;
//    }

    public Map<Integer, List<SimilarityEntry>> getMatrix() {
        return matrix;
    }



    public static SimilarityMatrix createMatrix(List<Sequence> sequences, MatrixType type) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (Sequence query : sequences) {
            //TODO skip singular episode here (if sequence.getscenario == "Scenario Name" then continue)
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (Sequence compareSequence : sequences) {
                double similarity;
                switch (type) { //make this switch into a private function
                    case SET: {
                        similarity = JaccardSimilarity.computeJaccard(query.toSet(), compareSequence.toSet());
                        //similarity = NonJaccardSimilarity.cosineSimilarity(query.toSet(), compareSequence.toSet());
                        break;
                    }
                    case MULTISET: {
                        similarity = JaccardSimilarity.computeJaccardOnMultisets(query.toMultiset(), compareSequence.toMultiset());
                        //similarity = NonJaccardSimilarity.cosineSimilarity(query.toMultiset(), compareSequence.toMultiset());
                        break;
                    }
                    case TF: {
                        similarity = JaccardSimilarity.weighedJaccard3(query.toTfWeights(), compareSequence.toTfWeights());
                        //similarity = NonJaccardSimilarity.cosineSimilarity(query.toTfWeights(), compareSequence.toTfWeights());
                        break;
                    }
                    case IDF: {
                        //similarity = JaccardSimilarity.computeWeighedJaccard(query.toSet(), compareSequence.toSet(), Sequence.getIdf());
                        similarity = NonJaccardSimilarity.cosineSimilarity(query.toIdfWeights(), compareSequence.toIdfWeights());
                        break;
                    }
                    case TFIDF_TFIDF: {
                        similarity = NonJaccardSimilarity.cosineSimilarity(query.toTfIdfWeights(), compareSequence.toTfIdfWeights());
                        break;
                    }
                    case INTERSECTION: {
                        similarity = NonJaccardSimilarity.computeSimilarityNoWeights(query.toSet(), compareSequence.toSet());
                        break;
                    }
                    case INTERSECTION_IDF: {
                        similarity = NonJaccardSimilarity.computeSimilarityIdfWeights(query.toSet(), compareSequence.toSet());
                        break;
                    }
                    case DTW: {
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

    public static SimilarityMatrix createMatrixMOMW(List<OverlaySequence> sequences, MatrixType type) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (OverlaySequence query : sequences) {
            //TODO skip singular episode here (if sequence.getscenario == "Scenario Name" then continue)
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (OverlaySequence compareSequence : sequences) {
                double similarity;
                switch (type) { //make this switch into a private function
                    case DTW: {
                        similarity = OverlaySimilarity.dtwSimilarity(query.getMotionWords(), compareSequence.getMotionWords(), 1);
                        break;
                    }
                    default: throw new IllegalStateException("This matrix type is not yet implemented!");
                }
                similarityEntries.add(new SimilarityEntry(compareSequence.getId(), similarity));
            }
        }
        return sm;
    }

    public static SimilarityMatrix createMatrixHMW(List<Sequence> sequences, BiFunction<Sequence, Sequence, Double> simFunc) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (Sequence query : sequences) {
            //TODO skip singular episode here (if sequence.getscenario == "Scenario Name" then continue)
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (Sequence compareSequence : sequences) {
                double similarity = simFunc.apply(query, compareSequence);
                similarityEntries.add(new SimilarityEntry(compareSequence.getId(), similarity));
            }
        }
        return sm;
    }

    /*public static SimilarityMatrix createMatrixMOMW(List<OverlaySequence> sequences, BiFunction<OverlaySequence, OverlaySequence, Double> simFunc) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (OverlaySequence query : sequences) {
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (Sequence compareSequence : sequences) {
                double similarity = simFunc.apply(query, compareSequence);
                similarityEntries.add(new SimilarityEntry(compareSequence.getId(), similarity));
            }
        }
        return sm;
    }*/

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
