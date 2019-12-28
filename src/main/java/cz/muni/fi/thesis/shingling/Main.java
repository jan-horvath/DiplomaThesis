package cz.muni.fi.thesis.shingling;

import java.io.*;
import java.util.*;


public class Main {
    private enum Interpretation {
        SET, MULTISET, MINHASH
    }

    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt";
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";

    private static final int DATA_SHINGLE_SIZE = 2;
    private static final int GT_SHINGLE_SIZE = 1; //ALWAYS 1
    private static final Interpretation GT_Interpretation = Interpretation.SET;
    private static final Interpretation Data_Interpretation = Interpretation.SET;

    public static void main(String[] args) throws IOException {
        //Ground truth
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        JaccardMatrix GTMatrix;

        int HASH_FUNCTION_COUNT = 100;
        int K_NEAREST_COUNT = 12;

        for (int i = 0; i < 1; ++i) {

            Shingles.resetMap();
            for (List<Integer> list : groundTruth.values()) {
                Shingles.addToMap(list, GT_SHINGLE_SIZE);
            }

            switch (GT_Interpretation) {
                case SET: {
                    Map<Integer, boolean[]> GTShingles = Shingles.createSetsOfShingles(groundTruth, GT_SHINGLE_SIZE);
                    GTMatrix = JaccardMatrix.createMatrixFromSets(GTShingles);
                    break;
                }
                case MULTISET: {
                    Map<Integer, int[]> GTShingles = Shingles.createMultisetsOfShingles(groundTruth, GT_SHINGLE_SIZE);
                    GTMatrix = JaccardMatrix.createMatrixFromMultisets(GTShingles);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + GT_Interpretation);
            }

            //Map<Integer, int[]> GT_KNN = KNN.bulkExtractKNNIndices(GTMatrix, K_NEAREST_COUNT);

            //Map<Integer, Integer> hundredPercentEntries = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 1.0);
            //Map<Integer, int[]> GT_variableKNN_hundred = KNN.bulkExtractVariableKNNIndices(GTMatrix, hundredPercentEntries);

            Map<Integer, Integer> aboveZeroPercentEntries = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.005);
            Map<Integer, int[]> GT_variableKNN_aboveZero = KNN.bulkExtractVariableKNNIndices(GTMatrix, aboveZeroPercentEntries);


            //Data
            Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
            Shingles.resetMap();
            for (List<Integer> list : data.values()) {
                Shingles.addToMap(list, DATA_SHINGLE_SIZE);
            }
            JaccardMatrix dataMatrix = null;

            MinHashCreator mhc = new MinHashCreator(Shingles.getMapSize(), HASH_FUNCTION_COUNT);

            switch (Data_Interpretation) {
                case SET: {
                    Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, DATA_SHINGLE_SIZE);
                    dataMatrix = JaccardMatrix.createMatrixFromSets(dataShingles);
                    break;
                }
                case MULTISET: {
                    Map<Integer, int[]> dataShingles = Shingles.createMultisetsOfShingles(data, DATA_SHINGLE_SIZE);
                    dataMatrix = JaccardMatrix.createMatrixFromMultisets(dataShingles);
                    break;
                }
                case MINHASH: {
                    Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, DATA_SHINGLE_SIZE);
                    Map<Integer, int[]> dataMinhashes = mhc.createMinHashes(dataShingles);
                    dataMatrix = JaccardMatrix.createMatrixFromMinhashes(dataMinhashes);
                    break;
                }
            }

            //Map<Integer, int[]> data_KNN = KNN.bulkExtractKNNIndices(dataMatrix, K_NEAREST_COUNT);
            //System.out.println("Final evaluation: " + KNN.bulkEvaluateKNN(GT_KNN, data_KNN) * 100 + "%");

            //Map<Integer, int[]> data_variableKNN_hundred = KNN.bulkExtractVariableKNNIndices(dataMatrix, hundredPercentEntries);
            //System.out.println("Final evaluation: " + KNN.bulkEvaluateKNN(GT_variableKNN_hundred, data_variableKNN_hundred) * 100 + "%");

            Map<Integer, int[]> data_variableKNN_aboveZero = KNN.bulkExtractVariableKNNIndices(dataMatrix, aboveZeroPercentEntries);
            System.out.println("Final evaluation: " + KNN.bulkEvaluateKNN(GT_variableKNN_aboveZero, data_variableKNN_aboveZero) * 100 + "%");



        }
    }
}