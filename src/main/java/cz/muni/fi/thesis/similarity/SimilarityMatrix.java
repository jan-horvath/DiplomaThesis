package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.sequences.HmwEpisode;
import cz.muni.fi.thesis.sequences.MomwEpisode;

import java.util.*;
import java.util.function.BiFunction;

public class SimilarityMatrix {
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

    public Map<Integer, List<SimilarityEntry>> getMatrix() {
        return matrix;
    }

    public static SimilarityMatrix createMatrixHMW(List<HmwEpisode> sequences, BiFunction<HmwEpisode, HmwEpisode, Double> simFunc) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (HmwEpisode query : sequences) {
            if (query.getScenario().equals("01-04") || query.getScenario().equals("01-04S")) {
                continue;
            }
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (HmwEpisode compareSequence : sequences) {
                double similarity = simFunc.apply(query, compareSequence);
                similarityEntries.add(new SimilarityEntry(compareSequence.getId(), similarity));
            }
        }
        return sm;
    }

    public static SimilarityMatrix createMatrixMOMW(List<MomwEpisode> sequences, BiFunction<MomwEpisode, MomwEpisode, Double> simFunc) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (MomwEpisode query : sequences) {
            if (query.getScenario().equals("01-04") || query.getScenario().equals("01-04S")) {
                continue;
            }
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (MomwEpisode compareSequence : sequences) {
                double similarity = simFunc.apply(query, compareSequence);
                similarityEntries.add(new SimilarityEntry(compareSequence.getId(), similarity));
            }
        }
        return sm;
    }

    public static SimilarityMatrix refineMatrix(
            Map<Integer, int[]> filteredEpisodes,
            Map<Integer, MomwEpisode> momwEpisodes,
            BiFunction<MomwEpisode, MomwEpisode, Double> simFunc) {
        SimilarityMatrix refinedMatrix = new SimilarityMatrix();

        for (Map.Entry<Integer, int[]> entry : filteredEpisodes.entrySet()) {
            List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
            MomwEpisode queryEpisode = momwEpisodes.get(entry.getKey());
            for (int id : entry.getValue()) {
                double similarity = simFunc.apply(queryEpisode, momwEpisodes.get(id));
                similarityEntries.add(new SimilarityMatrix.SimilarityEntry(id, similarity));
            }
            refinedMatrix.getMatrix().put(entry.getKey(), similarityEntries);
        }
        return refinedMatrix;
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
