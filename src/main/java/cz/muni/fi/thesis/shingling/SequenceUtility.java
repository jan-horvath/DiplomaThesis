package cz.muni.fi.thesis.shingling;

import cz.muni.fi.thesis.shingling.similarity.OverlaySimilarity;
import cz.muni.fi.thesis.shingling.similarity.SimilarityMatrix;

import java.util.*;

public class SequenceUtility {

    private static Double computeWeightForMotionWord(int[] motionWord, Map<Integer, List<int[]>> overlayData) {
        int count = 0;
        for (Map.Entry<Integer, List<int[]>> sequenceEntry : overlayData.entrySet()) {
            for (int[] otherMW : sequenceEntry.getValue()) {
                if (OverlaySimilarity.overlayMotionWordsMatch(motionWord, otherMW, 1)) {
                    ++count;
                    break;
                }
            }
        }

        return Math.log(((double) overlayData.size()) / count);
    }

    private static List<Double> computeWeights(List<int[]> sequence, Map<Integer, List<int[]>> overlayData, boolean ignoreMaxIdf) {
        List<Double> weights = new ArrayList<>();
        double maxIdf = Math.log(overlayData.size());

        for (int i = 0; i < sequence.size(); ++i) {
            int[] motionWord = sequence.get(i);
            double weight = computeWeightForMotionWord(motionWord, overlayData);
            if (ignoreMaxIdf && (Math.abs(weight - maxIdf) < 0.001)) {
                weight = 0.0;
            }
            weights.add(weight);
        }
        return weights;
    }

    /**
     * This function computes weights for each MOMW in every sequence. This takes a long time
     */
    /*TODO use sets to make this more efficient*/
    public static Map<Integer, OverlaySequence> createOverlaySequences(Map<Integer, List<int[]>> overlayData, boolean ignoreMaxIdf) {
        Map<Integer, OverlaySequence> sequences = new HashMap<>();
        for (Map.Entry<Integer, List<int[]>> entry : overlayData.entrySet()) {
            List<Double> weights = computeWeights(entry.getValue(), overlayData, ignoreMaxIdf);
            sequences.put(entry.getKey(), new OverlaySequence(entry.getValue(), weights));
        }
        return sequences;
    }

    public static List<Sequence> createSequences(Map<Integer, List<Integer>> groundTruth,
                                                 Map<Integer, List<Integer>> motionWords,
                                                 Map<Integer, String> scenarios) {
        List<Sequence> sequences = new ArrayList<>();
        //Map<String, Integer> scenarioCount = new HashMap<>();

        for (Integer seqID : groundTruth.keySet()) {
            assert(motionWords.containsKey(seqID));
            assert(scenarios.containsKey(seqID));

            String scenario = scenarios.get(seqID);
            sequences.add(new Sequence(seqID, scenario, groundTruth.get(seqID), motionWords.get(seqID)));
            /*if (!scenarioCount.containsKey(scenario)) {
                scenarioCount.put(scenario, 1);
            } else {
                scenarioCount.put(scenario, scenarioCount.get(scenario) + 1);
            }*/
        }

        /*for (Map.Entry<String, Integer> entry : scenarioCount.entrySet()) {
            System.out.println(entry);
        }*/

        return sequences;
    }

    /**
     * Removes scenarios "01-04" and "01-04S" (01-04 but switched), i.e. the ones that have only single sequence
     */
    public static void removeSparseScenarios(SimilarityMatrix sm, List<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            String scenario = sequence.getScenario();
            if (scenario.equals("01-04") || scenario.equals("01-04S")) {
                sm.getMatrix().remove(sequence.getId());
            }
        }
    }
}
