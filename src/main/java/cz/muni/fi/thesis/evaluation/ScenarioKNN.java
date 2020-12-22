package cz.muni.fi.thesis.evaluation;

import cz.muni.fi.thesis.Sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioKNN {

    public static double evaluate(List<Sequence> sequences, Map<Integer, int[]> motionWordsKnn) {
        double result = 0.0;

        Map<Integer, Sequence> sequenceMap = new HashMap<>();
        for (Sequence sequence : sequences) {
            sequenceMap.put(sequence.getId(), sequence);
        }

        for (Map.Entry<Integer, int[]> entry : motionWordsKnn.entrySet()) {
            Integer queryId = entry.getKey();
            int sequencesWithMatchingScenario = 0;
            for (int compareSequenceId : entry.getValue()) {
                String queryScenario = sequenceMap.get(queryId).getScenario();
                String compareSequenceScenario = sequenceMap.get(compareSequenceId).getScenario();
                if (queryScenario.equals(compareSequenceScenario)) {
                    sequencesWithMatchingScenario += 1.0;
                }
            }

            int K = entry.getValue().length;
            result += ((double) sequencesWithMatchingScenario)/K;
        }
        return result/motionWordsKnn.size();
    }

    public static Map<Integer, Integer> getVariableK(List<Sequence> sequences) {
        Map<Integer, Integer> variableK = new HashMap<>();
        Map<String, Integer> scenarioCount = new HashMap<>();

        for (Sequence sequence : sequences) {
            String scenario = sequence.getScenario();
            if (!scenarioCount.containsKey(scenario)) {
                scenarioCount.put(scenario, 1);
            } else {
                scenarioCount.put(scenario, scenarioCount.get(scenario) + 1);
            }
        }

        for (Sequence sequence : sequences) {
            variableK.put(sequence.getId(), scenarioCount.get(sequence.getScenario()));
        }
        return variableK;
    }
}
