package cz.muni.fi.thesis.shingling;

import java.io.*;
import java.util.*;


public class Main {
    private enum Interpretation {
        SET, MULTISET, MINHASH, STRIDED_SET, STRIDED_MULTISET, STRIDED_MINHASH, OVERLAYS
    }

    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt";
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    static private String overlayDataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350.data";

    private static final int DATA_SHINGLE_SIZE = 5;
    private static final int GT_SHINGLE_SIZE = 1; //ALWAYS 1
    private static final Interpretation GT_Interpretation = Interpretation.SET; //ALWAYS SET
    private static final Interpretation Data_Interpretation = Interpretation.OVERLAYS;

    public static void main(String[] args) throws IOException {
        //Ground truth
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        SimilarityMatrix GTMatrix;

        int HASH_FUNCTION_COUNT = 2786*2;

        int repeat = 1;
        if (Data_Interpretation == Interpretation.MINHASH || Data_Interpretation == Interpretation.STRIDED_MINHASH) {
            repeat = 10;
        }

        double sum_eval_knn12 = 0.0;
        double sum_eval_knn15 = 0.0;
        double sum_eval_nonpartial = 0.0;
        double sum_eval_partial = 0.0;

        for (int R = 0; R < repeat; ++R) {

            for (int i = 0; i < 4; ++i) {

                Shingles.resetMap();
                for (List<Integer> list : groundTruth.values()) {
                    Shingles.addToMap(list, GT_SHINGLE_SIZE);
                }

                switch (GT_Interpretation) {
                    case SET: {
                        Map<Integer, boolean[]> GTShingles = Shingles.createSetsOfShingles(groundTruth, GT_SHINGLE_SIZE);
                        GTMatrix = SimilarityMatrix.createMatrixFromSets(GTShingles);
                        break;
                    }
                    case MULTISET: {
                        Map<Integer, int[]> GTShingles = Shingles.createMultisetsOfShingles(groundTruth, GT_SHINGLE_SIZE);
                        GTMatrix = SimilarityMatrix.createMatrixFromMultisets(GTShingles);
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + GT_Interpretation);
                }

                Map<Integer, int[]> GT_KNN = null;
                Map<Integer, Integer> hundredPercentEntries = null;
                Map<Integer, int[]> GT_variableKNN_hundred = null;
                Map<Integer, Integer> aboveZeroPercentEntries = null;
                Map<Integer, int[]> GT_variableKNN_aboveZero = null;
                switch (i) {
                    case 0: {
                        GT_KNN = KNN.bulkExtractKNNIndices(GTMatrix, 12);
                        break;
                    }
                    case 1: {
                        GT_KNN = KNN.bulkExtractKNNIndices(GTMatrix, 15);
                        break;
                    }
                    case 2: {
                        hundredPercentEntries = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 1.0);
                        GT_variableKNN_hundred = KNN.bulkExtractVariableKNNIndices(GTMatrix, hundredPercentEntries);
                        break;
                    }
                    case 3: {
                        aboveZeroPercentEntries = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.005);
                        GT_variableKNN_aboveZero = KNN.bulkExtractVariableKNNIndices(GTMatrix, aboveZeroPercentEntries);
                        break;
                    }
                }

                //Data
                Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
                Map<Integer, List<int[]>> overlayData = DataLoader.parseOverlayDataFile(overlayDataFile,5);
                Shingles.resetMap();
                SimilarityMatrix dataMatrix = null;

                MinHashCreator mhc = null;

                switch (Data_Interpretation) {
                    case SET: {
                        Shingles.bulkAddToMap(data, DATA_SHINGLE_SIZE);
                        Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, DATA_SHINGLE_SIZE);
                        dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles);
                        break;
                    }
                    case MULTISET: {
                        Shingles.bulkAddToMap(data, DATA_SHINGLE_SIZE);
                        Map<Integer, int[]> dataShingles = Shingles.createMultisetsOfShingles(data, DATA_SHINGLE_SIZE);
                        dataMatrix = SimilarityMatrix.createMatrixFromMultisets(dataShingles);
                        break;
                    }
                    case MINHASH: {
                        Shingles.bulkAddToMap(data, DATA_SHINGLE_SIZE);
                        mhc = new MinHashCreator(Shingles.getMapSize(), HASH_FUNCTION_COUNT);
                        Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, DATA_SHINGLE_SIZE);
                        Map<Integer, int[]> dataMinhashes = mhc.createMinHashes(dataShingles);
                        dataMatrix = SimilarityMatrix.createMatrixFromMinhashes(dataMinhashes);
                        break;
                    }
                    case STRIDED_SET: {
                        Shingles.bulkAddToMapWithStride(data, DATA_SHINGLE_SIZE);
                        Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfStridedShingles(data, DATA_SHINGLE_SIZE);
                        dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles);
                        break;
                    }
                    case STRIDED_MULTISET: {
                        Shingles.bulkAddToMapWithStride(data, DATA_SHINGLE_SIZE);
                        Map<Integer, int[]> dataShingles = Shingles.createMultisetsOfStridedShingles(data, DATA_SHINGLE_SIZE);
                        dataMatrix = SimilarityMatrix.createMatrixFromMultisets(dataShingles);
                        break;
                    }
                    case STRIDED_MINHASH: {
                        Shingles.bulkAddToMapWithStride(data, DATA_SHINGLE_SIZE);
                        mhc = new MinHashCreator(Shingles.getMapSize(), HASH_FUNCTION_COUNT);
                        Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfStridedShingles(data, DATA_SHINGLE_SIZE);
                        Map<Integer, int[]> dataMinhashes = mhc.createMinHashes(dataShingles);
                        dataMatrix = SimilarityMatrix.createMatrixFromMinhashes(dataMinhashes);
                        break;
                    }
                    case OVERLAYS: {
                        dataMatrix = SimilarityMatrix.createMatrixFromOverlayData(overlayData, 2);
                    }
                }

                if ((i == 0) && (R == 0)) System.out.println("# of shingles: " + Shingles.getMapSize());

                Map<Integer, int[]> data_KNN = null;
                Map<Integer, int[]> data_variableKNN_hundred = null;
                Map<Integer, int[]> data_variableKNN_aboveZero = null;



                switch (i) {
                    case 0: {
                        data_KNN = KNN.bulkExtractKNNIndices(dataMatrix, 12);
                        sum_eval_knn12 += KNN.bulkEvaluateKNN(GT_KNN, data_KNN);

                        break;
                    }
                    case 1: {
                        data_KNN = KNN.bulkExtractKNNIndices(dataMatrix, 15);
                        sum_eval_knn15 += KNN.bulkEvaluateKNN(GT_KNN, data_KNN);
                        break;
                    }
                    case 2: {
                        data_variableKNN_hundred = KNN.bulkExtractVariableKNNIndices(dataMatrix, hundredPercentEntries);
                        sum_eval_nonpartial += KNN.bulkEvaluateKNN(GT_variableKNN_hundred, data_variableKNN_hundred);
                        break;
                    }
                    case 3: {
                        data_variableKNN_aboveZero = KNN.bulkExtractVariableKNNIndices(dataMatrix, aboveZeroPercentEntries);
                        sum_eval_partial += KNN.bulkEvaluateKNN(GT_variableKNN_aboveZero, data_variableKNN_aboveZero);
                        break;
                    }
                }
            }
        }
        System.out.println("Repeat: " + repeat);
        System.out.println("Final evaluation (K = 12): " + sum_eval_knn12/repeat * 100 + "%");
        System.out.println("Final evaluation (K = 15): " + sum_eval_knn15/repeat * 100 + "%");
        System.out.println("Final evaluation (non partial): " + sum_eval_nonpartial/repeat * 100 + "%");
        System.out.println("Final evaluation (partial): " + sum_eval_partial/repeat * 100 + "%");
    }
}