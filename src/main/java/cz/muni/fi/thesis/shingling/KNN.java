package cz.muni.fi.thesis.shingling;

import java.util.*;

public class KNN {

    public static int[] extractIndicesOfKHighest(List<JaccardMatrix.JaccardEntry> entries, int k, int skipIndex) {

        entries.removeIf(e -> e.recordID == skipIndex);
        Collections.sort(entries);
        Collections.reverse(entries);

        int[] kHighest = new int[k];
        for (int i = 0; i < k; ++i) {
            kHighest[i] = entries.get(i).recordID;
            if (kHighest[i] == 0) {
                System.err.println("Warning: " + i + "th highest is zero!");
            }
        }

        return kHighest;
    }

    public static Map<Integer, int[]> bulkExtractIndicesOfKHighest(JaccardMatrix matrix, int k) {
        Map<Integer, int[]> result = new HashMap<>();

        for (Map.Entry<Integer, List<JaccardMatrix.JaccardEntry>> entry : matrix.getMatrix().entrySet()) {
            result.put(entry.getKey(), extractIndicesOfKHighest(entry.getValue(), k, entry.getKey()));
        }
        return result;
    }

    public static double evaluateKNN(int[] groundTruth, int[] data) {
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
            average += evaluateKNN(groundTruth.get(recordingIndex), data.get(recordingIndex));
            ++recordingCount;
        }

        return average/recordingCount;
    }
}
