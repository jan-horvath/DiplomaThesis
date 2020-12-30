package cz.muni.fi.thesis;

import cz.muni.fi.thesis.dataloader.MoCapData;
import cz.muni.fi.thesis.dataloader.MoCapDataLoader;
import cz.muni.fi.thesis.evaluation.KNN;
import cz.muni.fi.thesis.sequences.MomwEpisode;
import cz.muni.fi.thesis.sequences.HmwEpisode;
import cz.muni.fi.thesis.sequences.SequenceUtility;
import cz.muni.fi.thesis.similarity.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;

/**TODO
 * comment on TF (double) vs Multiset (int)
 * add documentation
 * replace MatrixType with similarity functions
 * remove asserts
 * make output of evaluation nicer
 * naming in SimilarityMatrix and Entries
 * rename sequences to episodes everywhere
 */

public class Main {

    /**
     * The Experiment enum refers to experiments from individual chapters of the thesis Information Retrieval Techniques
     * for 3D Human Motion Data
     */
    private enum Experiment {
        CHAPTER_4,
        CHAPTER_5,
        CHAPTER_6,
        CHAPTER_7,
        CHAPTER_8,
        CHAPTER_9
    }

    private static Experiment experiment = Experiment.CHAPTER_4;

    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final int OF_K = 10;
    private static final int OS_K = 5;
    private static final int REFINE_K = 0;

    private static void evaluateMatrix(SimilarityMatrix matrix, Map<Integer, Integer> variableK, Map<Integer, String> scenarios, int maxK) {
        for (int K = 1; K <= maxK+1; ++K) {
            Map<Integer, int[]> motionWordsKNN;
            if (K == maxK+1) {
                motionWordsKNN = KNN.bulkExtractVariableKNNIndices(matrix, variableK);
            } else {
                motionWordsKNN = KNN.bulkExtractKNNIndices(matrix, K);
            }
            System.out.println(df.format(100*evaluate(scenarios, motionWordsKNN)));
        }
    }

    private static void evaluateMatrix(SimilarityMatrix matrix, Map<Integer, Integer> variableK, Map<Integer, String> scenarios, int maxK, String string) {
        System.out.println(string);
        evaluateMatrix(matrix, variableK, scenarios, maxK);
    }

    public static double evaluate(Map<Integer, String> scenarios, Map<Integer, int[]> motionWordsKnn) {
        double result = 0.0;

        for (Map.Entry<Integer, int[]> entry : motionWordsKnn.entrySet()) {
            Integer queryId = entry.getKey();
            int sequencesWithMatchingScenario = 0;
            for (int compareSequenceId : entry.getValue()) {
                if (scenarios.get(queryId).equals(scenarios.get(compareSequenceId))) {
                    sequencesWithMatchingScenario += 1.0;
                }
            }

            int K = entry.getValue().length;
            result += ((double) sequencesWithMatchingScenario)/K;
        }
        return result/motionWordsKnn.size();
    }

    public static void main(String[] args) throws IOException {
        MoCapData data = MoCapDataLoader.loadData();

        switch (experiment) {
            case CHAPTER_4: {
                HmwEpisode.setUp(data.getHMWs());
                List<HmwEpisode> hmwEpisodes = SequenceUtility.createSequences(data.getHMWs(), data.getOFScenarios());
                List<MomwEpisode> momwEpisodes = SequenceUtility.createMomwEpisodes(data.getMOMWs(), data.getOFScenarios());

                SimilarityMatrix hmwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::DTW);
                evaluateMatrix(hmwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                SimilarityMatrix momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::dtwSimilarity);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);
                break;
            }
            case CHAPTER_5: {
                HmwEpisode.setUp(data.getHMWs());
                List<HmwEpisode> hmwEpisodes = SequenceUtility.createSequences(data.getHMWs(), data.getOFScenarios());

                List<BiFunction<HmwEpisode, HmwEpisode, Double>> similarityFunctions = Arrays.asList(
                        HmwShingleSimilarity::jaccardOnSet,
                        HmwShingleSimilarity::cosineOnSet,
                        HmwShingleSimilarity::jaccardOnBag,
                        HmwShingleSimilarity::cosineOnBag,
                        HmwShingleSimilarity::jaccardOnIdf,
                        HmwShingleSimilarity::cosineOnIdf,
                        HmwShingleSimilarity::cosineOnTfIdf);
                List<String> functionNames = Arrays.asList("Jaccard on set", "Cosine on set", "Jaccard on bag",
                        "Cosine on bag", "Jaccard on IDF", "Cosine on IDF", "Cosine on TFIDF");

                for (int i = 0; i < functionNames.size(); ++i) {
                    System.out.println(functionNames.get(i));
                    SimilarityMatrix matrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, similarityFunctions.get(i));
                    evaluateMatrix(matrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);
                }
                break;
            }
            case CHAPTER_6: {
                List<MomwEpisode> momwEpisodes = SequenceUtility.createMomwEpisodes(data.getMOMWs(), data.getOFScenarios());
                SimilarityMatrix momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnSets);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnTF);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnIdf);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnTfIdf);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);
                break;
            }
            case CHAPTER_7: {
                //Order-free
                HmwEpisode.setUp(data.getMixedHMWs());
                List<HmwEpisode> hmwEpisodes = SequenceUtility.createSequences(data.getMixedHMWs(), data.getOFScenarios());
                List<MomwEpisode> momwEpisodes = SequenceUtility.createMomwEpisodes(data.getMixedMOMWs(), data.getOFScenarios());

                SimilarityMatrix hmwDtwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::DTW);
                evaluateMatrix(hmwDtwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                SimilarityMatrix hmwIdfMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);
                evaluateMatrix(hmwIdfMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                SimilarityMatrix momwDtwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::dtwSimilarity);
                evaluateMatrix(momwDtwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                SimilarityMatrix momwIdfMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnIdf);
                evaluateMatrix(momwIdfMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);

                //Order-sensitive
                hmwEpisodes = SequenceUtility.createSequences(data.getMixedHMWs(), data.getOSScenarios());
                momwEpisodes = SequenceUtility.createMomwEpisodes(data.getMixedMOMWs(), data.getOSScenarios());

                hmwDtwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::DTW);
                evaluateMatrix(hmwDtwMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K);

                hmwIdfMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);
                evaluateMatrix(hmwIdfMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K);

                momwDtwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::dtwSimilarity);
                evaluateMatrix(momwDtwMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K);

                momwIdfMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnIdf);
                evaluateMatrix(momwIdfMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K);
                break;
            }
            case CHAPTER_8: {
                //Order-free
                HmwEpisode.setUp(data.getHMWs());
                List<HmwEpisode> hmwEpisodes = SequenceUtility.createSequences(data.getMixedHMWs(), data.getOFScenarios());
                Map<Integer, MomwEpisode> momwEpisodesAsMap = SequenceUtility.createMomwEpisodesAsMap(data.getMixedMOMWs(), data.getOFScenarios());
                SimilarityMatrix hmwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);

                for (double f = 1.0; f < 4.1; f += 0.5) {
                    Map<Integer, int[]> filteredEpisodes = KNN.bulkExtractVariableKNNIndices(hmwMatrix, data.getOFVariableKForFiltering(f));
                    SimilarityMatrix momwMatrix = SimilarityMatrix.refineMatrix(filteredEpisodes, momwEpisodesAsMap, MomwSimilarity::jaccardOnIdf);
                    evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), REFINE_K);
                }

                //Order-sensitive
                HmwEpisode.setUp(data.getMixedHMWs());
                hmwEpisodes = SequenceUtility.createSequences(data.getMixedHMWs(), data.getOSScenarios());
                momwEpisodesAsMap = SequenceUtility.createMomwEpisodesAsMap(data.getMixedMOMWs(), data.getOSScenarios());
                hmwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);


                for (double f = 1.0; f < 4.1; f += 0.5) {
                    Map<Integer, int[]> filteredEpisodes = KNN.bulkExtractVariableKNNIndices(hmwMatrix, data.getOSVariableKForFiltering(f));
                    SimilarityMatrix momwMatrix = SimilarityMatrix.refineMatrix(filteredEpisodes, momwEpisodesAsMap, MomwSimilarity::dtwSimilarity);
                    evaluateMatrix(momwMatrix, data.getOSVariableK(), data.getOSScenarios(), REFINE_K);
                }
                break;
            }
            case CHAPTER_9: {
                for (int K = 1; K <= 5; ++K) {
                    HmwEpisode.setUp(data.getHMWs(), K, K);
                    List<HmwEpisode> hmwEpisodes = SequenceUtility.createSequences(data.getHMWs(), data.getOFScenarios());
                    SimilarityMatrix shingleIdfMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);
                    evaluateMatrix(shingleIdfMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K);
                }
                break;
            }
        }

        //(DTW or Jaccard) + mutlioverlay MWs (no filtering,...)
        /*long start = System.nanoTime();
        data = MoCapDataLoader.loadData();
        System.out.println("Loading: " + (System.nanoTime() - start)/1000000 + "ms");

        Map<Integer, MomwEpisode> overlaySequences = SequenceUtility.createOverlaySequences(data.getMOMWs());
        HmwEpisode.setUp(data.getHMWs());
        List<HmwEpisode> sequences = SequenceUtility.createSequences(data.getHMWs(), data.getOFScenarios());
        Map<Integer, Integer> K = ScenarioKNN.getVariableK(sequences);
        System.out.println("Setup: " + (System.nanoTime() - start)/1000000 + "ms");

        SimilarityMatrix matrix = new SimilarityMatrix();

        // DTW and non-weighed overlay Jaccard
//        for (Map.Entry<Integer, List<int[]>> query : data.getMomwDataset().entrySet()) {
//            List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
//            List<int[]> queryMWs = query.getValue();
//            for (Map.Entry<Integer, List<int[]>> compareSequence : data.getMomwDataset().entrySet()) {
//                double similarity = OverlaySimilarity.dtwSimilarity(queryMWs, compareSequence.getValue(), 1);
//                //double similarity = OverlaySimilarity.overlayJaccard3(queryMWs, compareSequence.getValue(), 1);
//                similarityEntries.add(new SimilarityMatrix.SimilarityEntry(compareSequence.getKey(), similarity));
//            }
//            matrix.getMatrix().put(query.getKey(), similarityEntries);
//        }

        //weighed overlay Jaccard
        for (Map.Entry<Integer, MomwEpisode> queryEntry : overlaySequences.entrySet()) {
            List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
            for (Map.Entry<Integer, MomwEpisode> compareEntry : overlaySequences.entrySet()) {
                double similarity = OverlaySimilarity.weighedOverlayJaccard3(queryEntry.getValue(), compareEntry.getValue(), 1);
                similarityEntries.add(new SimilarityMatrix.SimilarityEntry(compareEntry.getKey(), similarity));
            }
            matrix.getMatrix().put(queryEntry.getKey(), similarityEntries);
        }
        //------------------------

        SequenceUtility.removeSingularEpisode(matrix, sequences);

        for (int i = 1; i <= 11; ++i) {
            Map<Integer, int[]> finalKnn;
            if (i == 11) {
                finalKnn = KNN.bulkExtractVariableKNNIndices(matrix, K);
            } else {
                finalKnn = KNN.bulkExtractKNNIndices(matrix, i);
            }
            System.out.println(100 * ScenarioKNN.evaluate(sequences, finalKnn));
        }

        System.out.println("Computation: " + (System.nanoTime() - start)/1000000 + "ms");*/

        //Filtering + DTW/Jaccard on MOMWs
        /*long start;
        start = System.nanoTime();
        MoCapData data = MoCapDataLoader.loadData();
        System.out.println("Loading: " + (System.nanoTime() - start)/1000000 + "ms");

        start = System.nanoTime();
        Map<Integer, MomwEpisode> overlaySequences = SequenceUtility.createOverlaySequences(data.getMomwDataset());
        System.out.println("MOMW IDF computation: " + (System.nanoTime() - start)/1000000 + "ms");

        start = System.nanoTime();
        HmwEpisode.setUp(data.getHmwDataset(), 1,1, MIN_ACTION, MAX_ACTION);
        List<HmwEpisode> sequences = SequenceUtility.createSequences(data.getHmwDataset(), data.getOrderFreeScenarios());
        SimilarityMatrix hardMwsMatrix = SimilarityMatrix.createMatrix(sequences, MatrixType.IDF);
        SequenceUtility.removeSparseScenarios(hardMwsMatrix, sequences);

        Map<Integer, Integer> variableK = ScenarioKNN.getVariableK(sequences);
        System.out.println("Setup: " + (System.nanoTime() - start)/1000000 + "ms\n");

        for (double MULTIPLIER = 1.0; MULTIPLIER < 4.1; MULTIPLIER += 0.5) {
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
//                    List<int[]> query = data.getMomwDataset().get(entry.getKey());
//                    for (int id : entry.getValue()) {
//                        double similarity = OverlaySimilarity.overlayJaccard3(query, data.getMomwDataset().get(id), 1);
//                        //double similarity = OverlaySimilarity.dtwSimilarity(query, data.getMomwDataset().get(id), 1);
//                        similarityEntries.add(new SimilarityMatrix.SimilarityEntry(id, similarity));
//                    }
//                    refineMatrix.getMatrix().put(entry.getKey(), similarityEntries);
//                }

                //weighed overlay Jaccard
                for (Map.Entry<Integer, int[]> entry : filteredKnn.entrySet()) {
                    List<SimilarityMatrix.SimilarityEntry> similarityEntries = new ArrayList<>();
                    MomwEpisode overlaySequence = overlaySequences.get(entry.getKey());
                    for (int id : entry.getValue()) {
                        double similarity = OverlaySimilarity.weighedOverlayJaccard3(overlaySequence, overlaySequences.get(id), 1);
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
                System.out.println("(M = " + MULTIPLIER + ") " + (System.nanoTime() - start) / 1000000 + "ms"
                 + " [" + ScenarioKNN.evaluate(sequences, finalKnn) + "]");
            }
        }*/

        // Experiments - Hard MWs
//        long start = System.nanoTime();
//        data = MoCapDataLoader.loadData();
//        System.out.println("Data loading: " + (System.nanoTime() - start)/1000000 + "ms");
//
//        start = System.nanoTime();
//        int minK = 1;
//        int maxK = 1;
//        HmwEpisode.setUp(data.getHMWs());
//        List<HmwEpisode> sequences = SequenceUtility.createSequences(data.getHMWs(), data.getOFScenarios());
//        System.out.println("Setup: " + (System.nanoTime() - start)/1000000 + "ms");
//
//        MatrixType[] matrixTypes = new MatrixType[]{MatrixType.SET};
//        DecimalFormat df2 = new DecimalFormat("#.##");
//        for (MatrixType mType : matrixTypes) {
//            start = System.nanoTime();
//
//            System.out.println();
//            System.out.println(mType);
//            System.out.println("Shingle size: " + minK + " - " + maxK);
//
//            SimilarityMatrix matrix = SimilarityMatrix.createMatrix(sequences, mType);
//            SequenceUtility.removeSingularEpisode(matrix, sequences);
//            System.out.println("Distance computation between all pairs (matrix creation): " + (System.nanoTime() - start)/1000000 + "ms");
//
//            start = System.nanoTime();
//            for (int K = 1; K <= 11; ++K) {
//                Map<Integer, int[]> motionWordsKNN;
//                if (K == 11) {
//                    Map<Integer, Integer> variableK = data.getOFVariableK();
//                    motionWordsKNN = KNN.bulkExtractVariableKNNIndices(matrix, variableK);
//                } else {
//                    motionWordsKNN = KNN.bulkExtractKNNIndices(matrix, K);
//                }
//                System.out.println(df2.format(100* ScenarioKNN.evaluate(sequences, motionWordsKNN)));
//            }
//            System.out.println("Evaluation for all k = {1,...,10,k*}: " + (System.nanoTime() - start)/1000000 + "ms");
//        }
    }
}