package cz.muni.fi.thesis.shingling;

import cz.muni.fi.thesis.shingling.evaluation.KNN;
import cz.muni.fi.thesis.shingling.evaluation.ScenarioKNN;
import cz.muni.fi.thesis.shingling.evaluation.ScenarioSimilarityAnalysis;
import cz.muni.fi.thesis.shingling.similarity.JaccardSimilarity;
import cz.muni.fi.thesis.shingling.similarity.MatrixType;
import cz.muni.fi.thesis.shingling.similarity.OverlayJaccardSimilarity;
import cz.muni.fi.thesis.shingling.similarity.SimilarityMatrix;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


public class Main {
    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt";
    static private String shortSequencesDataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment20_shift20-coords_normPOS-fps12-quantized-pivots-kmedoids-350.txt";

    // Original data
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    // Data with switched halves
    //static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350-halfCategorySwitched.data";

    // Original data
    static private String overlayDataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350.data";
    // Data with switched halves
    //static private String overlayDataFile = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350-halfCategorySwitched.data";

    // Original scenarion
    static private String scenarioFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-scenarios.txt";
    // Switched halves scenarios
    //static private String scenarioFile = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\ground_truth-sequence_scenarios-halfCategorySwitched-respectOrdering.txt";

    //testing
    //static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\test_data_file.txt";
    //static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth_test.txt";

    private static final int MIN_SHINGLE = 1;
    private static final int MAX_SHINGLE = 1;
    public static final int MIN_ACTION = 22;
    public static final int MAX_ACTION = 152;

    public static double getSumOfWeights(Sequence sequence) {
        double sum = 0.0;
        double[] weights = sequence.toIdfWeights(true);
        for (int i = 0; i < weights.length; ++i) {
            sum += weights[i];
        }
        return sum;
    }

    public static int getCountOfDistinctShingles(Sequence sequence) {
        int count = 0;
        boolean[] set = sequence.toSet(true);
        for (int i = 0; i < set.length; ++i) {
            if (set[i]) {
                ++count;
            }
        }
        return count;
    }

    public static int getCountOfShingles(Sequence sequence) {
        int count = 0;
        int[] multiset = sequence.toMultiset(false);
        for (int i = 0; i < multiset.length; ++i) {
            count += multiset[i];
        }
        return count;
    }

    public static double getComputedDistanceForId(List<SimilarityMatrix.SimilarityEntry> list, int id) {
        for (SimilarityMatrix.SimilarityEntry entry : list) {
            if (entry.recordID == id) {
                return entry.jaccardValue;
            }
        }
        throw new IllegalStateException("Entry not found in the list.");
    }

    public static int countOfMatches(Sequence seq1, Sequence seq2) {
        int count = 0;
        boolean[] set1 = seq1.toSet(true);
        boolean[] set2 = seq2.toSet(true);
        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] && set2[i]) {
                ++count;
            }
        }
        return count;
    }

    public static double sumOfMatchWeights(Sequence seq1, Sequence seq2) {
        double count = 0;
        boolean[] set1 = seq1.toSet(true);
        boolean[] set2 = seq2.toSet(true);
        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] && set2[i]) {
                count += Sequence.getIdf().get(i);
            }
        }
        return count;
    }

    public static String listMatchings(Sequence seq1, Sequence seq2) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat df2 = new DecimalFormat("#.##");
        boolean[] set1 = seq1.toSet(true);
        boolean[] set2 = seq2.toSet(true);
        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] && set2[i]) {
                sb.append(Sequence.getShingleIds().inverse().get(i));
                sb.append("(").append(df2.format(Sequence.getIdf().get(i))).append(")");
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {

        //(DTW or Jaccard) + mutlioverlay MWs (no filtering,...)
        long start = System.nanoTime();
        Map<Integer, List<int[]>> overlayData = DataLoader.parseOverlayDataFile(overlayDataFile, 5);
        Map<Integer, String> scenarios = DataLoader.parseScenarioFile(scenarioFile, false);
        Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        Map<Integer, OverlaySequence> overlaySequences = SequenceUtility.createOverlaySequences(overlayData, true);
        System.out.println("Loading: " + (System.nanoTime() - start)/1000000 + "ms");

        Sequence.setUp(data, 1,1, MIN_ACTION, MAX_ACTION);
        List<Sequence> sequences = SequenceUtility.createSequences(groundTruth, data, scenarios);
        Map<Integer, Integer> K = ScenarioKNN.getVariableK(sequences);
        System.out.println("Setup: " + (System.nanoTime() - start)/1000000 + "ms");

        SimilarityMatrix matrix = new SimilarityMatrix();

        // DTW and non-weighed overlay Jaccard
        for (Map.Entry<Integer, List<int[]>> query : overlayData.entrySet()) {
            List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
            List<int[]> queryMWs = query.getValue();
            for (Map.Entry<Integer, List<int[]>> compareSequence : overlayData.entrySet()) {
                double similarity = OverlayJaccardSimilarity.dtwSimilarity(queryMWs, compareSequence.getValue(), 1);
                //double similarity = OverlayJaccardSimilarity.overlayJaccard3(queryMWs, compareSequence.getValue(), 1);
                similarityEntries.add(new SimilarityMatrix.SimilarityEntry(compareSequence.getKey(), similarity));
            }
            matrix.getMatrix().put(query.getKey(), similarityEntries);
        }

        //weighed overlay Jaccard
//        for (Map.Entry<Integer, OverlaySequence> queryEntry : overlaySequences.entrySet()) {
//            List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
//            for (Map.Entry<Integer, OverlaySequence> compareEntry : overlaySequences.entrySet()) {
//                double similarity = OverlayJaccardSimilarity.weighedOverlayJaccard3(queryEntry.getValue(), compareEntry.getValue(), 1);
//                similarityEntries.add(new SimilarityMatrix.SimilarityEntry(compareEntry.getKey(), similarity));
//            }
//            matrix.getMatrix().put(queryEntry.getKey(), similarityEntries);
//        }

        SequenceUtility.removeSparseScenarios(matrix, sequences);

        for (int i = 1; i <= 11; ++i) {
            Map<Integer, int[]> finalKnn;
            if (i == 11) {
                finalKnn = KNN.bulkExtractVariableKNNIndices(matrix, K);
            } else {
                finalKnn = KNN.bulkExtractKNNIndices(matrix, i);
            }

//            for (Map.Entry<Integer, List<SimilarityMatrix.SimilarityEntry>> entry : matrix.getMatrix().entrySet()) {
//                System.out.println("Sequence ID: " + entry.getKey() + " (" + finalKnn.get(entry.getKey()).length + ")");
//                for (int C = 0; C < finalKnn.get(entry.getKey()).length; ++C) {
//                    SimilarityMatrix.SimilarityEntry simEntry = entry.getValue().get(C);
//                    System.out.println((C+1) + ". NN = " + simEntry.recordID + " (" + simEntry.jaccardValue + ")");
//                }
//            }

            System.out.println(100 * ScenarioKNN.evaluate(sequences, finalKnn));
        }

        System.out.println("Computation: " + (System.nanoTime() - start)/1000000 + "ms");

        //Filtering + DTW/Jaccard on MOMWs
        /*long start;
        start = System.nanoTime();
        Map<Integer, List<int[]>> overlayData = DataLoader.parseOverlayDataFile(overlayDataFile, 5);
        Map<Integer, String> scenarios = DataLoader.parseScenarioFile(scenarioFile, false);
        Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        System.out.println("Loading: " + (System.nanoTime() - start)/1000000 + "ms");

        start = System.nanoTime();
        Map<Integer, OverlaySequence> overlaySequences = SequenceUtility.createOverlaySequences(overlayData, true);
        System.out.println("MOMW IDF computation: " + (System.nanoTime() - start)/1000000 + "ms");

        start = System.nanoTime();
        Sequence.setUp(data, 1,1, MIN_ACTION, MAX_ACTION);
        List<Sequence> sequences = SequenceUtility.createSequences(groundTruth, data, scenarios);
        SimilarityMatrix hardMwsMatrix = SimilarityMatrix.createMatrix(sequences, MatrixType.IDF_IGNORE);
        SequenceUtility.removeSparseScenarios(hardMwsMatrix, sequences);

        Map<Integer, Integer> variableK = ScenarioKNN.getVariableK(sequences);
        System.out.println("Setup: " + (System.nanoTime() - start)/1000000 + "ms\n");

        for (double MULTIPLIER = 1.5; MULTIPLIER < 5.1; MULTIPLIER += 0.5) {
            start = System.nanoTime();
            Map<Integer, int[]> filteredKnn;
            for (int K = 11; K <= 11; ++K) {
                if (K == 11) {
                    Map<Integer, Integer> variableKForFiltering = new HashMap<>();
                    for (Map.Entry<Integer, Integer> entry : variableK.entrySet()) {
                        variableKForFiltering.put(entry.getKey(), (int) (entry.getValue() * MULTIPLIER));
                    }
                    filteredKnn = KNN.bulkExtractVariableKNNIndices(hardMwsMatrix, variableKForFiltering);
                } else {
                    filteredKnn = KNN.bulkExtractKNNIndices(hardMwsMatrix, (int) Math.round(K * MULTIPLIER));
                }
                //System.out.println("Filtering (" + MULTIPLIER + "): " + (System.nanoTime() - start)/1000000 + "ms");

                start = System.nanoTime();
                SimilarityMatrix refineMatrix = new SimilarityMatrix();

                // DTW and non-weighed overlay Jaccard
//                for (Map.Entry<Integer, int[]> entry : filteredKnn.entrySet()) {
//                    List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
//                    List<int[]> query = overlayData.get(entry.getKey());
//                    for (int id : entry.getValue()) {
//                        //double similarity = OverlayJaccardSimilarity.overlayJaccard3(query, overlayData.get(id), 1);
//                        double similarity = OverlayJaccardSimilarity.dtwSimilarity(query, overlayData.get(id), 1);
//                        similarityEntries.add(new SimilarityMatrix.SimilarityEntry(id, similarity));
//                    }
//                    refineMatrix.getMatrix().put(entry.getKey(), similarityEntries);
//                }

                //weighed overlay Jaccard
                for (Map.Entry<Integer, int[]> entry : filteredKnn.entrySet()) {
                    List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
                    OverlaySequence overlaySequence = overlaySequences.get(entry.getKey());
                    for (int id : entry.getValue()) {
                        double similarity = OverlayJaccardSimilarity.weighedOverlayJaccard3(overlaySequence, overlaySequences.get(id), 1);
                        similarityEntries.add(new SimilarityMatrix.SimilarityEntry(id, similarity));
                    }
                    refineMatrix.getMatrix().put(entry.getKey(), similarityEntries);
                }

                Map<Integer, int[]> finalKnn;
                if (K == 11) {
                    finalKnn = KNN.bulkExtractVariableKNNIndices(refineMatrix, variableK);
                } else {
                    finalKnn = KNN.bulkExtractKNNIndices(refineMatrix, K);
                }
                //System.out.println(100 * ScenarioKNN.evaluate(sequences, finalKnn));
                System.out.println("(M = " + MULTIPLIER + ") " + (System.nanoTime() - start) / 1000000 + "ms");
            }
        }*/

        // Same scenario vs different scenario analysis
        /*Map<Integer, String> scenarios = DataLoader.parseScenarioFile(scenarioFile);

        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        Map<Integer, List<Integer>> motionWords = DataLoader.parseDataFile(dataFile);

        Sequence.setUp(motionWords, 5, 5, MIN_ACTION, MAX_ACTION);
        List<Sequence> sequences = SequenceUtility.createSequences(groundTruth, motionWords, scenarios);
        SimilarityMatrix matrix = SimilarityMatrix.createMatrix(sequences, MatrixType.IDF_IGNORE);
        List<ScenarioSimilarityAnalysis.Result> results = ScenarioSimilarityAnalysis.analyzeScenarioSimilarity(sequences, matrix);

        DecimalFormat df2 = new DecimalFormat("#.##");

        System.out.println("Same scenario average similarity:");
        for (ScenarioSimilarityAnalysis.Result result : results) {
            System.out.println(df2.format(100*result.sameScenarioAverageSim));
        }

        System.out.println("Different scenario average similarity:");
        for (ScenarioSimilarityAnalysis.Result result : results) {
            System.out.println(df2.format(100*result.differentScenarioAverageSim));
        }

        System.out.println("Same scenario min similarity:");
        for (ScenarioSimilarityAnalysis.Result result : results) {
            System.out.println(df2.format(100*result.sameScenarioMinSim));
        }

        System.out.println("Different scenario max similarity:");
        for (ScenarioSimilarityAnalysis.Result result : results) {
            System.out.println(df2.format(100*result.differentScenarioMaxSim));
        }*/

        // Experiments - Hard MWs
        /*long start;
        start = System.nanoTime();
        Map<Integer, String> scenarios = DataLoader.parseScenarioFile(scenarioFile, true);
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        Map<Integer, List<Integer>> motionWords = DataLoader.parseDataFile(dataFile);
        System.out.println("Data loading: " + (System.nanoTime() - start)/1000000 + "ms");

        int SHINGLE_SIZE = 1;

        MatrixType[] matrixTypes = new MatrixType[]{MatrixType.IDF_IGNORE, MatrixType.DTW};
        for (MatrixType mType : matrixTypes) {
            start = System.nanoTime();
            Sequence.setUp(motionWords, 1, 1, MIN_ACTION, MAX_ACTION);
            List<Sequence> sequences = SequenceUtility.createSequences(groundTruth, motionWords, scenarios);

            DecimalFormat df2 = new DecimalFormat("#.##");
            System.out.println();
            System.out.println(mType);
            System.out.println("Shingle size = " + SHINGLE_SIZE);

            SimilarityMatrix matrix = SimilarityMatrix.createMatrix(sequences, mType);
            SequenceUtility.removeSparseScenarios(matrix, sequences);
            System.out.println("Distance computation between all pairs: " + (System.nanoTime() - start)/1000000 + "ms");

            for (int K = 1; K <= 11; ++K) {
                start = System.nanoTime();
                Map<Integer, int[]> motionWordsKNN;
                if (K == 11) {
                    Map<Integer, Integer> variableK = ScenarioKNN.getVariableK(sequences);
                    motionWordsKNN = KNN.bulkExtractVariableKNNIndices(matrix, variableK);
                } else {
                    motionWordsKNN = KNN.bulkExtractKNNIndices(matrix, K);
                }
//                FileWriter fw = new FileWriter(new File("statistics_with_scenarios" + shingleSize + ".txt"));
//
//                for (Sequence seq : sequences) {
//                    fw.append("Query sequence: ").append(String.valueOf(seq.getId()))
//                            .append(", countAllShingles=").append(String.valueOf(getCountOfShingles(seq)))
//                            .append("\n");
//
//                    fw.append(shingleSize + "-shingles: ")
//                            .append("countDistinct= ").append(String.valueOf(getCountOfDistinctShingles(seq)))
//                            .append(" sumOfIdfWeights=").append(df2.format(getSumOfWeights(seq)))
//                            .append("\n");
//                    int[] KNN = motionWordsKNN.get(seq.getId());
//                    if (KNN == null) {
//                        fw.append("This sequence was skipped when calculating KNN because of its low occurring scenario.\n");
//                        continue;
//                    }
//                    for (int i = 1; i <= KNN.length; ++i) {
//                        List<SimilarityMatrix.SimilarityEntry> computedDistances = matrix.getMatrix().get(seq.getId());
//                        int nnSeqId = KNN[i - 1];
//                        Sequence nnSeq = sequences.stream().filter(s -> s.getId() == nnSeqId).findFirst().orElse(null);
//                        fw.append("    " + i + ". nearest neighbor: " + nnSeqId
//                                + ", mwDistance=" + df2.format(getComputedDistanceForId(computedDistances, nnSeqId))
//                                + " commonScenario=" + seq.getScenario().equals(nnSeq.getScenario())
//                                + "\n");
//                        fw.append("        " + shingleSize + "-shingles:"
//                                + " countDistinct=" + getCountOfDistinctShingles(nnSeq)
//                                + ", sumOfWeights=" + df2.format(getSumOfWeights(nnSeq))
//                                + "\n");
//                        fw.append("        matching " + shingleSize + "-shingles:"
//                                + " countOfMatches=" + countOfMatches(seq, nnSeq)
//                                + ", sumOfMatchWeights=" + df2.format(sumOfMatchWeights(seq, nnSeq))
//                                + "\n");
//                        fw.append("        matchingShingles:" + listMatchings(seq, nnSeq)
//                                + "\n");
//                    }
//                    fw.append("\n");
//                }

                System.out.println(df2.format(100*ScenarioKNN.evaluate(sequences, motionWordsKNN)));
            }
        }*/
    }
}