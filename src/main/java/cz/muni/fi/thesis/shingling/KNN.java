package cz.muni.fi.thesis.shingling;

import java.util.*;

public class KNN {

    private static int getNumberOfEntriesWithValueAtLeastN(List<JaccardMatrix.JaccardEntry> entries, double n) {
        int count = 0;
        for (JaccardMatrix.JaccardEntry entry : entries) {
            if (entry.jaccardValue > n - 0.001) {
                ++count;
            }
        }
        if (count == 0) {
            throw new IllegalStateException();
        }
        return count;
    }

    public static Map<Integer, Integer> getNumberOfEntriesWithValueAtLeastNForEachRow(JaccardMatrix matrix, double n) {
        Map<Integer, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, List<JaccardMatrix.JaccardEntry>> entry : matrix.getMatrix().entrySet()) {
            map.put(entry.getKey(), getNumberOfEntriesWithValueAtLeastN(entry.getValue(), n));
        }
        return map;
    }

    public static Map<Integer, int[]> bulkExtractVariableKNNIndices(JaccardMatrix matrix, Map<Integer, Integer> variableK) {
        Map<Integer, int[]> result = new HashMap<>();

        for (Map.Entry<Integer, List<JaccardMatrix.JaccardEntry>> e : matrix.getMatrix().entrySet()) {
            result.put(e.getKey(), extractKNNIndices(e.getValue(), variableK.get(e.getKey())-1, e.getKey()));
        }
        return result;
    }

    private static int[] extractKNNIndices(List<JaccardMatrix.JaccardEntry> entries, int k, int skipIndex) {

        entries.removeIf(e -> e.recordID == skipIndex);
        Collections.sort(entries);
        Collections.reverse(entries);

        int[] kHighest = new int[k];
        for (int i = 0; i < k; ++i) {
            kHighest[i] = entries.get(i).recordID;
        }

        return kHighest;
    }

    public static Map<Integer, int[]> bulkExtractKNNIndices(JaccardMatrix matrix, int k) {
        Map<Integer, int[]> result = new HashMap<>();

        for (Map.Entry<Integer, List<JaccardMatrix.JaccardEntry>> entry : matrix.getMatrix().entrySet()) {
            result.put(entry.getKey(), extractKNNIndices(entry.getValue(), k, entry.getKey()));
        }
        return result;
    }

    private static double evaluateKNN(int[] groundTruth, int[] data) {
        assert(groundTruth.length == data.length);
        
        int k = groundTruth.length;
        int matching = 0;
        
        for (int g = 0; g < k; ++g) {
            for (int d = 0; d < k; ++d) {
                if (groundTruth[g] == data[d]) {
                    ++matching;
                    break;
                }
            }
        }
        return ((double)matching)/k;
    }

    public static double bulkEvaluateKNN(Map<Integer, int[]> groundTruth,  Map<Integer, int[]> data) {
        double average = 0.0;
        int recordingCount = 0;

        for (Integer recordingIndex : groundTruth.keySet()) {
            assert(data.containsKey(recordingIndex));
            if (groundTruth.get(recordingIndex).length != 0) {
                average += evaluateKNN(groundTruth.get(recordingIndex), data.get(recordingIndex));
                ++recordingCount;
            }
        }

        return average/recordingCount;
    }
}
