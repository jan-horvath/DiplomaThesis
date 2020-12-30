package cz.muni.fi.thesis.similarity;

import cz.muni.fi.thesis.episode.HmwEpisode;
import cz.muni.fi.thesis.episode.MomwEpisode;

import java.util.*;
import java.util.function.BiFunction;

/**
 * A matrix represented using a map of lists, which stores the information about similarity between any two episodes
 */
public class SimilarityMatrix {
    private Map<Integer, List<SimilarityEntry>> matrix = new HashMap<>();

    public static class SimilarityEntry implements Comparable<SimilarityEntry> {
        final int recordID;
        final double similarityValue;

        SimilarityEntry(int recordID, double similarityValue) {
            this.recordID = recordID;
            this.similarityValue = similarityValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimilarityEntry entry = (SimilarityEntry) o;
            return Double.compare(entry.similarityValue, similarityValue) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(similarityValue);
        }

        @Override
        public int compareTo(SimilarityEntry o) {
            return Double.compare(similarityValue, o.similarityValue);
        }

        @Override
        public String toString() {
            return "(" + recordID + ", " + similarityValue + ")";
        }
    }

    Map<Integer, List<SimilarityEntry>> getMatrix() {
        return matrix;
    }

    /**
     * Computes similarity matrix from HmwEpisodes
     * @param episodes episodes of hard motion words
     * @param simFunc similarity function which should be used for comparison
     * @return similarity matrix
     */
    public static SimilarityMatrix createMatrixHMW(List<HmwEpisode> episodes, BiFunction<HmwEpisode, HmwEpisode, Double> simFunc) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (HmwEpisode query : episodes) {
            if (query.getScenario().equals("01-04") || query.getScenario().equals("01-04S")) {
                continue;
            }
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (HmwEpisode compareEpisode : episodes) {
                double similarity = simFunc.apply(query, compareEpisode);
                similarityEntries.add(new SimilarityEntry(compareEpisode.getId(), similarity));
            }
        }
        return sm;
    }

    /**
     * Computes similarity matrix from MomwEpisodes
     * @param episodes episodes of multi-overlay motion words
     * @param simFunc similarity function which should be used for comparison
     * @return similarity matrix
     */
    public static SimilarityMatrix createMatrixMOMW(List<MomwEpisode> episodes, BiFunction<MomwEpisode, MomwEpisode, Double> simFunc) {
        SimilarityMatrix sm = new SimilarityMatrix();
        for (MomwEpisode query : episodes) {
            if (query.getScenario().equals("01-04") || query.getScenario().equals("01-04S")) {
                continue;
            }
            sm.matrix.put(query.getId(), new ArrayList<>());
            List<SimilarityEntry> similarityEntries = sm.matrix.get(query.getId());
            for (MomwEpisode compareEpisode : episodes) {
                double similarity = simFunc.apply(query, compareEpisode);
                similarityEntries.add(new SimilarityEntry(compareEpisode.getId(), similarity));
            }
        }
        return sm;
    }

    /**
     * Create an incomplete matrix of only filtered pairs of episodes from {@code filteredEpisodes}.
     * @param filteredEpisodes A map of arrays of episode IDs, each array has an episode ID as a key. The function
     *                         computes similarity between the key episode and all episodes in the array associated with
     *                         the key
     * @param momwEpisodes map of MomwEpisodes with the key being their ID for faster look up
     * @param simFunc similarity function which should be used for comparison
     * @return similarity matrix
     */
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
