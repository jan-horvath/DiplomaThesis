package cz.muni.fi.thesis;

import cz.muni.fi.thesis.dataloader.MoCapData;
import cz.muni.fi.thesis.dataloader.MoCapDataLoader;
import cz.muni.fi.thesis.similarity.KNN;
import cz.muni.fi.thesis.episode.MomwEpisode;
import cz.muni.fi.thesis.episode.HmwEpisode;
import cz.muni.fi.thesis.episode.EpisodeUtility;
import cz.muni.fi.thesis.similarity.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;

/**TODO
 * add documentation
 * remove unnecessary data files from directory
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

    /**
     * Choose experiments from a concrete chapter here
     */
    private static Experiment experiments_to_run = Experiment.CHAPTER_8;

    private static final DecimalFormat df = new DecimalFormat("0.00");
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
            System.out.print(" " + df.format(100*evaluate(scenarios, motionWordsKNN)) + " ");
        }
    }

    private static void evaluateMatrix(SimilarityMatrix matrix, Map<Integer, Integer> variableK, Map<Integer, String> scenarios, int maxK, String string) {
        System.out.println(string);
        System.out.print("K   = ");
        for (int K = 1; K <= maxK; ++K) {
            System.out.print("   " + K + "   ");
        }
        System.out.print("   k*\nAcc = ");
        evaluateMatrix(matrix, variableK, scenarios, maxK);
        System.out.println();
        System.out.println();
    }

    private static double evaluate(Map<Integer, String> scenarios, Map<Integer, int[]> motionWordsKnn) {
        double result = 0.0;

        for (Map.Entry<Integer, int[]> entry : motionWordsKnn.entrySet()) {
            Integer queryId = entry.getKey();
            int episodesWithMatchingScenario = 0;
            for (int compareEpisodeId : entry.getValue()) {
                if (scenarios.get(queryId).equals(scenarios.get(compareEpisodeId))) {
                    episodesWithMatchingScenario += 1.0;
                }
            }

            int K = entry.getValue().length;
            result += ((double) episodesWithMatchingScenario)/K;
        }
        return result/motionWordsKnn.size();
    }

    public static void main(String[] args) throws IOException {
        MoCapData data = MoCapDataLoader.loadData();

        switch (experiments_to_run) {
            case CHAPTER_4: {
                HmwEpisode.setUp(data.getHMWs());
                List<HmwEpisode> hmwEpisodes = EpisodeUtility.createHmwEpisodes(data.getHMWs(), data.getOFScenarios());
                List<MomwEpisode> momwEpisodes = EpisodeUtility.createMomwEpisodes(data.getMOMWs(), data.getOFScenarios());

                SimilarityMatrix hmwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::DTW);
                evaluateMatrix(hmwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "HMW + DTW");

                SimilarityMatrix momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::dtwSimilarity);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "MOMW + DTW");
                break;
            }
            case CHAPTER_5: {
                HmwEpisode.setUp(data.getHMWs());
                List<HmwEpisode> hmwEpisodes = EpisodeUtility.createHmwEpisodes(data.getHMWs(), data.getOFScenarios());

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
                    SimilarityMatrix matrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, similarityFunctions.get(i));
                    evaluateMatrix(matrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, functionNames.get(i));
                }
                break;
            }
            case CHAPTER_6: {
                List<MomwEpisode> momwEpisodes = EpisodeUtility.createMomwEpisodes(data.getMOMWs(), data.getOFScenarios());
                SimilarityMatrix momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnSets);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "MOMW");

                momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnTF);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "MOMW + TF");

                momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnIdf);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "MOMW + IDF");

                momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnTfIdf);
                evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "MOMW + TFIDF");
                break;
            }
            case CHAPTER_7: {
                //Order-free
                HmwEpisode.setUp(data.getMixedHMWs());
                List<HmwEpisode> hmwEpisodes = EpisodeUtility.createHmwEpisodes(data.getMixedHMWs(), data.getOFScenarios());
                List<MomwEpisode> momwEpisodes = EpisodeUtility.createMomwEpisodes(data.getMixedMOMWs(), data.getOFScenarios());

                SimilarityMatrix hmwDtwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::DTW);
                evaluateMatrix(hmwDtwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "Order-free, HMW + DTW");

                SimilarityMatrix hmwIdfMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);
                evaluateMatrix(hmwIdfMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "Order-free, HMW + IDF");

                SimilarityMatrix momwDtwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::dtwSimilarity);
                evaluateMatrix(momwDtwMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "Order-free, MOMW + DTW");

                SimilarityMatrix momwIdfMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnIdf);
                evaluateMatrix(momwIdfMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, "Order-free, MOMW + IDF");

                //Order-sensitive
                hmwEpisodes = EpisodeUtility.createHmwEpisodes(data.getMixedHMWs(), data.getOSScenarios());
                momwEpisodes = EpisodeUtility.createMomwEpisodes(data.getMixedMOMWs(), data.getOSScenarios());

                hmwDtwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::DTW);
                evaluateMatrix(hmwDtwMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K, "Order-sensitive, HMW + DTW");

                hmwIdfMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);
                evaluateMatrix(hmwIdfMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K, "Order-sensitive, HMW + IDF");

                momwDtwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::dtwSimilarity);
                evaluateMatrix(momwDtwMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K, "Order-sensitive, MOMW + DTW");

                momwIdfMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::jaccardOnIdf);
                evaluateMatrix(momwIdfMatrix, data.getOSVariableK(), data.getOSScenarios(), OS_K, "Order-sensitive, MOMW + IDF");
                break;
            }
            case CHAPTER_8: {
                //Order-free
                HmwEpisode.setUp(data.getHMWs());
                List<HmwEpisode> hmwEpisodes = EpisodeUtility.createHmwEpisodes(data.getMixedHMWs(), data.getOFScenarios());
                Map<Integer, MomwEpisode> momwEpisodesAsMap = EpisodeUtility.createMomwEpisodesAsMap(data.getMixedMOMWs(), data.getOFScenarios());
                SimilarityMatrix hmwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);

                for (double f = 1.0; f < 4.1; f += 0.5) {
                    Map<Integer, int[]> filteredEpisodes = KNN.bulkExtractVariableKNNIndices(hmwMatrix, data.getOFVariableKForFiltering(f));
                    SimilarityMatrix momwMatrix = SimilarityMatrix.refineMatrix(filteredEpisodes, momwEpisodesAsMap, MomwSimilarity::jaccardOnIdf);
                    evaluateMatrix(momwMatrix, data.getOFVariableK(), data.getOFScenarios(), REFINE_K, "Order-free two phase model, f = " + df.format(f));
                }

                //Order-sensitive
                HmwEpisode.setUp(data.getMixedHMWs());
                hmwEpisodes = EpisodeUtility.createHmwEpisodes(data.getMixedHMWs(), data.getOSScenarios());
                momwEpisodesAsMap = EpisodeUtility.createMomwEpisodesAsMap(data.getMixedMOMWs(), data.getOSScenarios());
                hmwMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);


                for (double f = 1.0; f < 4.1; f += 0.5) {
                    Map<Integer, int[]> filteredEpisodes = KNN.bulkExtractVariableKNNIndices(hmwMatrix, data.getOSVariableKForFiltering(f));
                    SimilarityMatrix momwMatrix = SimilarityMatrix.refineMatrix(filteredEpisodes, momwEpisodesAsMap, MomwSimilarity::dtwSimilarity);
                    evaluateMatrix(momwMatrix, data.getOSVariableK(), data.getOSScenarios(), REFINE_K, "Order-sensitive two phase model, f = " + df.format(f));
                }
                break;
            }
            case CHAPTER_9: {
                for (int K = 1; K <= 5; ++K) {
                    HmwEpisode.setUp(data.getHMWs(), K, K);
                    List<HmwEpisode> hmwEpisodes = EpisodeUtility.createHmwEpisodes(data.getHMWs(), data.getOFScenarios());
                    SimilarityMatrix shingleIdfMatrix = SimilarityMatrix.createMatrixHMW(hmwEpisodes, HmwShingleSimilarity::cosineOnIdf);
                    evaluateMatrix(shingleIdfMatrix, data.getOFVariableK(), data.getOFScenarios(), OF_K, K + "-shingles + IDF");
                }
                break;
            }
        }
    }
}