package cz.muni.fi.thesis.evaluation;

import cz.muni.fi.thesis.Sequence;
import cz.muni.fi.thesis.similarity.SimilarityMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioSimilarityAnalysis {

    public static class Result {
        public final int sequenceId;
        public final double sameScenarioAverageSim;
        public final double differentScenarioAverageSim;
        public final double sameScenarioMinSim;
        public final double differentScenarioMaxSim;

        public Result(int sequenceId, double sameScenarioAverageSim, double differentScenarioAverageSim, double sameScenarioMinSim, double differentScenarioMaxSim) {
            this.sequenceId = sequenceId;
            this.sameScenarioAverageSim = sameScenarioAverageSim;
            this.differentScenarioAverageSim = differentScenarioAverageSim;
            this.sameScenarioMinSim = sameScenarioMinSim;
            this.differentScenarioMaxSim = differentScenarioMaxSim;
        }
    }

    public static List<Result> analyzeScenarioSimilarity(List<Sequence> sequences, SimilarityMatrix sm) {
        List<Result> results = new ArrayList<>();
        Map<Integer, List<SimilarityMatrix.SimilarityEntry>> matrix = sm.getMatrix();

        Map<Integer, String> scenarioMap = createScenarioMap(sequences);

        for (Map.Entry<Integer, List<SimilarityMatrix.SimilarityEntry>> entry : matrix.entrySet()) {
            results.add(analyzeSequence(entry.getKey(), scenarioMap, entry.getValue()));
        }

        return results;
    }

    private static Map<Integer, String> createScenarioMap(List<Sequence> sequences) {
        Map<Integer, String> scenarioMap = new HashMap<>();

        for (Sequence sequence : sequences) {
            scenarioMap.put(sequence.getId(), sequence.getScenario());
        }

        return scenarioMap;
    }

    private static Result analyzeSequence(int sequenceId, Map<Integer, String> scenarioMap, List<SimilarityMatrix.SimilarityEntry> entries) {
        double sameScenarioAverageSim = 0.0;
        double differentScenarioAverageSim = 0.0;
        double sameScenarioMinSim = 5000;
        double differentScenarioMaxSim = 0.0;
        String scenario = scenarioMap.get(sequenceId);

        int sameScenarioCount = 0;
        int differentScenarioCount = 0;

        for (SimilarityMatrix.SimilarityEntry entry : entries) {
            if (scenario.equals(scenarioMap.get(entry.recordID))) {
                sameScenarioAverageSim += entry.jaccardValue;
                sameScenarioMinSim = Math.min(sameScenarioMinSim, entry.jaccardValue);
                ++sameScenarioCount;
            } else {
                differentScenarioAverageSim += entry.jaccardValue;
                differentScenarioMaxSim = Math.max(differentScenarioMaxSim, entry.jaccardValue);
                ++differentScenarioCount;
            }
        }

        sameScenarioAverageSim = sameScenarioAverageSim/sameScenarioCount;
        differentScenarioAverageSim = differentScenarioAverageSim/differentScenarioCount;

        return new Result(sequenceId, sameScenarioAverageSim, differentScenarioAverageSim, sameScenarioMinSim, differentScenarioMaxSim);
    }
}
