package cz.muni.fi.thesis.evaluation;

import cz.muni.fi.thesis.Sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioKNN {

    private static String scenarioName;

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

    public static Map<Integer, Integer> getVariableK(Map<Integer, String> scenarios) {
        Map<Integer, Integer> variableK = new HashMap<>();
        Map<String, Integer> scenarioCount = new HashMap<>();

        for (Map.Entry<Integer, String> scenario : scenarios.entrySet()) {
            scenarioName = scenario.getValue();
            if (!scenarioCount.containsKey(scenarioName)) {
                scenarioCount.put(scenarioName, 1);
            } else {
                scenarioCount.put(scenarioName, scenarioCount.get(scenarioName) + 1);
            }
        }

        for (Map.Entry<Integer, String> scenario : scenarios.entrySet()) {
            variableK.put(scenario.getKey(), scenarioCount.get(scenario.getValue()));
        }
        return variableK;
    }
}
