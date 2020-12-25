package cz.muni.fi.thesis.sequences;

import cz.muni.fi.thesis.similarity.MomwSimilarity;
import cz.muni.fi.thesis.similarity.OverlaySimilarity;
import cz.muni.fi.thesis.similarity.SimilarityMatrix;

import java.util.*;

public class SequenceUtility {

    private static Double computeWeightForMotionWord(int[] motionWord, Map<Integer, List<int[]>> overlayData) {
        int count = 0;
        for (Map.Entry<Integer, List<int[]>> sequenceEntry : overlayData.entrySet()) {
            for (int[] otherMW : sequenceEntry.getValue()) {
                if (MomwSimilarity.overlayMotionWordsMatch(motionWord, otherMW, 1)) {
                    ++count;
                    break;
                }
            }
        }

        return Math.log(((double) overlayData.size()) / count);
    }

    private static List<Double> computeWeights(List<int[]> sequence, Map<Integer, List<int[]>> overlayData) {
        List<Double> weights = new ArrayList<>();

        for (int i = 0; i < sequence.size(); ++i) {
            int[] motionWord = sequence.get(i);
            double weight = computeWeightForMotionWord(motionWord, overlayData);
            weights.add(weight);
        }
        return weights;
    }

    /**
     * This function computes weights for each MOMW in every sequence. This takes a long time
     */
    /*TODO use sets to make this more efficient and rename this*/
    public static List<MomwEpisode> createOverlaySequences(Map<Integer, List<int[]>> motionWords,
                                                           Map<Integer, String> scenarios) {
       List<MomwEpisode> sequences = new ArrayList<>();
        for (Integer seqId : motionWords.keySet()) {
            List<Double> weights = computeWeights(motionWords.get(seqId), motionWords);
            sequences.add(new MomwEpisode(seqId, scenarios.get(seqId), motionWords.get(seqId), weights));
        }
        return sequences;
    }

    public static List<HmwEpisode> createSequences(Map<Integer, List<Integer>> motionWords,
                                                   Map<Integer, String> scenarios) {
        List<HmwEpisode> sequences = new ArrayList<>();

        for (Integer seqID : motionWords.keySet()) {
            assert(motionWords.containsKey(seqID));
            assert(scenarios.containsKey(seqID));

            String scenario = scenarios.get(seqID);
            sequences.add(new HmwEpisode(seqID, scenario, motionWords.get(seqID)));
        }

        return sequences;
    }

    /**
     * Removes scenarios "01-04" and "01-04S" (01-04 but switched), i.e. the ones that have only single sequence
     */
    public static void removeSingularEpisode(SimilarityMatrix sm, List<HmwEpisode> sequences) {
        for (HmwEpisode sequence : sequences) {
            String scenario = sequence.getScenario();
            if (scenario.equals("01-04") || scenario.equals("01-04S")) {
                sm.getMatrix().remove(sequence.getId());
            }
        }
    }
}
