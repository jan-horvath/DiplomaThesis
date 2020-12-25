package cz.muni.fi.thesis;

import cz.muni.fi.thesis.dataloader.MoCapData;
import cz.muni.fi.thesis.dataloader.MoCapDataLoader;
import cz.muni.fi.thesis.evaluation.KNN;
import cz.muni.fi.thesis.evaluation.ScenarioKNN;
import cz.muni.fi.thesis.sequences.MomwEpisode;
import cz.muni.fi.thesis.sequences.HmwEpisode;
import cz.muni.fi.thesis.sequences.SequenceUtility;
import cz.muni.fi.thesis.similarity.HmwShingleSimilarity;
import cz.muni.fi.thesis.similarity.MatrixType;
import cz.muni.fi.thesis.similarity.MomwSimilarity;
import cz.muni.fi.thesis.similarity.SimilarityMatrix;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**TODO
 * comment on TF (double) vs Multiset (int)
 * add documentation
 * document Experiment enum
 * replace MatrixType with similarity functions
 * remove asserts
 */

public class Main {

    private enum Experiment {
        CHAPTER_4,
        CHAPTER_5,
        CHAPTER_6,
        CHAPTER_7,
        CHAPTER_8,
        CHAPTER_9
    }

    private static Experiment experiment = Experiment.CHAPTER_4;

    private static final int MIN_SHINGLE = 1;
    private static final int MAX_SHINGLE = 1;

    public static void main(String[] args) throws IOException {

        MoCapData data = MoCapDataLoader.loadData();
        DecimalFormat df2 = new DecimalFormat("#.##");

        switch (experiment) {
            case CHAPTER_4: {
                HmwEpisode.setUp(data.getHMWs());
                List<HmwEpisode> sequences = SequenceUtility.createSequences(data.getHMWs(), data.getOFScenarios());
                List<MomwEpisode> momwEpisodes = SequenceUtility.createOverlaySequences(data.getMOMWs(), data.getOFScenarios());

                SimilarityMatrix hmwMatrix = SimilarityMatrix.createMatrixHMW(sequences, HmwShingleSimilarity::dtwSimilarity);
                SequenceUtility.removeSingularEpisode(hmwMatrix, sequences);

                for (int K = 1; K <= 11; ++K) {
                    Map<Integer, int[]> motionWordsKNN;
                    if (K == 11) {
                        Map<Integer, Integer> variableK = ScenarioKNN.getVariableK(data.getOFScenarios());
                        motionWordsKNN = KNN.bulkExtractVariableKNNIndices(hmwMatrix, variableK);
                    } else {
                        motionWordsKNN = KNN.bulkExtractKNNIndices(hmwMatrix, K);
                    }
                    System.out.println(df2.format(100*ScenarioKNN.evaluate(sequences, motionWordsKNN)));
                }

                SimilarityMatrix momwMatrix = SimilarityMatrix.createMatrixMOMW(momwEpisodes, MomwSimilarity::dtwSimilarity);
                SequenceUtility.removeSingularEpisode(momwMatrix, sequences);

                for (int K = 1; K <= 11; ++K) {
                    Map<Integer, int[]> motionWordsKNN;
                    if (K == 11) {
                        Map<Integer, Integer> variableK = ScenarioKNN.getVariableK(data.getOFScenarios());
                        motionWordsKNN = KNN.bulkExtractVariableKNNIndices(momwMatrix, variableK);
                    } else {
                        motionWordsKNN = KNN.bulkExtractKNNIndices(momwMatrix, K);
                    }
                    System.out.println(df2.format(100*ScenarioKNN.evaluate(sequences, motionWordsKNN)));
                }
            }
        }

        //(DTW or Jaccard) + mutlioverlay MWs (no filtering,...)
        /*long start = System.nanoTime();
        MoCapData data = MoCapDataLoader.loadData();
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
        /*long start = System.nanoTime();
        MoCapData data = MoCapDataLoader.loadData();
        System.out.println("Data loading: " + (System.nanoTime() - start)/1000000 + "ms");

        start = System.nanoTime();
        int minK = 1;
        int maxK = 1;
        HmwEpisode.setUp(data.getHmwDataset(), minK, maxK, MIN_ACTION, MAX_ACTION);
        List<HmwEpisode> sequences = SequenceUtility.createSequences(data.getHmwDataset(), data.getOrderFreeScenarios());
        System.out.println("Setup: " + (System.nanoTime() - start)/1000000 + "ms");

        MatrixType[] matrixTypes = new MatrixType[]{MatrixType.DTW, MatrixType.IDF};
        DecimalFormat df2 = new DecimalFormat("#.##");
        for (MatrixType mType : matrixTypes) {
            start = System.nanoTime();

            System.out.println();
            System.out.println(mType);
            System.out.println("Shingle size: " + minK + " - " + maxK);

            SimilarityMatrix matrix = SimilarityMatrix.createMatrix(sequences, mType);
            SequenceUtility.removeSparseScenarios(matrix, sequences);
            System.out.println("Distance computation between all pairs (matrix creation): " + (System.nanoTime() - start)/1000000 + "ms");

            start = System.nanoTime();
            for (int K = 1; K <= 11; ++K) {
                Map<Integer, int[]> motionWordsKNN;
                if (K == 11) {
                    Map<Integer, Integer> variableK = ScenarioKNN.getVariableK(sequences);
                    motionWordsKNN = KNN.bulkExtractVariableKNNIndices(matrix, variableK);
                } else {
                    motionWordsKNN = KNN.bulkExtractKNNIndices(matrix, K);
                }
                System.out.println(df2.format(100*ScenarioKNN.evaluate(sequences, motionWordsKNN)));
            }
            System.out.println("Evaluation for all k = {1,...,10,k*}: " + (System.nanoTime() - start)/1000000 + "ms");
        }*/
    }
}