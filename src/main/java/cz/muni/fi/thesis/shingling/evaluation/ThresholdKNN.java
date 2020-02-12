package cz.muni.fi.thesis.shingling.evaluation;

import cz.muni.fi.thesis.shingling.Jaccard;

import java.util.Map;

public class ThresholdKNN {

    public static double evaluate(Map<Integer, boolean[]> GTShingles, Map<Integer, int[]> data_KNN, double threshold) {
        double result = 0.0;
        int K_in_KNN = data_KNN.values().iterator().next().length;

        for (Map.Entry<Integer, int[]> entry : data_KNN.entrySet()) {
            Integer recordID = entry.getKey();
            for (int otherRecordID : entry.getValue()) {
                double weighedJaccard = Jaccard.computeJaccard(GTShingles.get(recordID), GTShingles.get(otherRecordID));
                if (weighedJaccard > threshold) {
                    result += 1.0;
                }
            }
        }
        return result/(K_in_KNN * GTShingles.size());
    }
}
