package cz.muni.fi.thesis.similarity;

import java.util.*;

public class KNN {
    public static Map<Integer, int[]> bulkExtractVariableKNNIndices(SimilarityMatrix matrix, Map<Integer, Integer> variableK) {
        Map<Integer, int[]> result = new HashMap<>();

        for (Map.Entry<Integer, List<SimilarityMatrix.SimilarityEntry>> e : matrix.getMatrix().entrySet()) {
            result.put(e.getKey(), extractKNNIndices(e.getValue(), variableK.get(e.getKey())-1, e.getKey()));
        }
        return result;
    }

    private static int[] extractKNNIndices(List<SimilarityMatrix.SimilarityEntry> entries, int k, int skipIndex) {

        entries.removeIf(e -> e.recordID == skipIndex);
        Collections.sort(entries);
        Collections.reverse(entries);

        int[] kHighest = new int[k];
        for (int i = 0; i < k; ++i) {
            kHighest[i] = entries.get(i).recordID;
        }

        return kHighest;
    }

    public static Map<Integer, int[]> bulkExtractKNNIndices(SimilarityMatrix matrix, int k) {
        Map<Integer, int[]> result = new HashMap<>();

        for (Map.Entry<Integer, List<SimilarityMatrix.SimilarityEntry>> entry : matrix.getMatrix().entrySet()) {
            result.put(entry.getKey(), extractKNNIndices(entry.getValue(), k, entry.getKey()));
        }
        return result;
    }
}
