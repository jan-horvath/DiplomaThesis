package cz.muni.fi.thesis.shingling;

import cz.muni.fi.thesis.shingling.similarity.OverlayJaccardSimilarity;
import cz.muni.fi.thesis.shingling.similarity.SimilarityMatrix;

import java.util.*;

public class SequenceUtility {

    private static Double computeWeightForMotionWord(int[] motionWord, Collection<List<int[]>> sequences) {
        int count = 0;
        for (List<int[]> sequence : sequences) {
            for (int[] otherMW : sequence) {
                if (OverlayJaccardSimilarity.overlayMotionWordsMatch(motionWord, otherMW, 1)) {
                    ++count;
                    break;
                }
            }
        }

        return Math.log(sequences.size() / count);
    }

    private static List<Double> computeWeights(List<int[]> sequence, Collection<List<int[]>> sequences) {
        List<Double> weights = new ArrayList<>();

        for (int i = 0; i < sequence.size(); ++i) {
            int[] motionWord = sequence.get(i);
            double weight = computeWeightForMotionWord(motionWord, sequences);
            weights.add(weight);
        }
        return weights;
    }

    public static Map<Integer, OverlaySequence> createOverlaySequences(Map<Integer, List<int[]>> overlayData) {
        Map<Integer, OverlaySequence> sequences = new HashMap<>();
        for (Map.Entry<Integer, List<int[]>> entry : overlayData.entrySet()) {
            List<Double> weights = computeWeights(entry.getValue(), overlayData.values());
            sequences.put(entry.getKey(), new OverlaySequence(entry.getValue(), weights));
        }
        return sequences;
    }

    public static List<Sequence> createSequences(Map<Integer, List<Integer>> groundTruth,
                                                 Map<Integer, List<Integer>> motionWords,
                                                 Map<Integer, String> scenarios) {
        List<Sequence> sequences = new ArrayList<>();
        Map<String, Integer> scenarioCount = new HashMap<>();

        for (Integer seqID : groundTruth.keySet()) {
            assert(motionWords.containsKey(seqID));
            assert(scenarios.containsKey(seqID));

            String scenario = scenarios.get(seqID);
            if (!scenarioCount.containsKey(scenario)) {
                scenarioCount.put(scenario, 1);
            } else {
                scenarioCount.put(scenario, scenarioCount.get(scenario) + 1);
            }
            sequences.add(new Sequence(seqID, scenario, groundTruth.get(seqID), motionWords.get(seqID)));
        }

        /*for (Map.Entry<String, Integer> entry : scenarioCount.entrySet()) {
            System.out.println(entry);
        }*/

        return sequences;
    }

    public static void removeSparseScenarios(SimilarityMatrix sm, List<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            String scenario = sequence.getScenario();
            if (scenario.equals("01-04")) {
                sm.getMatrix().remove(sequence.getId());
            }
        }
    }
}
