package cz.muni.fi.thesis.shingling;

import java.io.*;
import java.util.*;


public class Main {
    private enum Interpretation {
        SET, MULTISET, MINHASH
    }

    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt";
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    private static final int MIN_CLASS = 22;
    private static final int MAX_CLASS = 152;
    private static final int MIN_MOTIONWORD = 0;
    private static final int MAX_MOTIONWORD = 349;

    private static final int DATA_SHINGLE_SIZE = 2;
    private static final int GT_SHINGLE_SIZE = 1; //ALWAYS 1
    private static final Interpretation GT_Interpretation = Interpretation.MULTISET;
    private static final Interpretation Data_Interpretation = Interpretation.MINHASH;

    public static void main(String[] args) throws IOException {
        //Ground truth
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        JaccardMatrix GTMatrix = null;

        int HASH_FUNCTION_COUNT = 10;
        int K_NEAREST_COUNT = 0;

        for (int i = 0; i < 6; ++i) {
            K_NEAREST_COUNT += 3;

            switch (GT_Interpretation) {
                case SET: {
                    Map<Integer, boolean[]> GTShingles = Shingles.createSetsOfShingles(groundTruth, MIN_CLASS, MAX_CLASS, GT_SHINGLE_SIZE);
                    GTMatrix = JaccardMatrix.createMatrixFromSets(GTShingles);
                    break;
                }
                case MULTISET: {
                    Map<Integer, int[]> GTShingles = Shingles.createMultisetsOfShingles(groundTruth, MIN_CLASS, MAX_CLASS, GT_SHINGLE_SIZE);
                    GTMatrix = JaccardMatrix.createMatrixFromMultisets(GTShingles);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + GT_Interpretation);
            }

            Map<Integer, int[]> GT_KNN = KNN.bulkExtractIndicesOfKHighest(GTMatrix, K_NEAREST_COUNT);


            //Data
            Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
            JaccardMatrix dataMatrix = null;

            MinHashCreator mhc = new MinHashCreator(MAX_MOTIONWORD - MIN_MOTIONWORD + 1, HASH_FUNCTION_COUNT);

            switch (Data_Interpretation) {
                case SET: {
                    Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, MIN_MOTIONWORD, MAX_MOTIONWORD, DATA_SHINGLE_SIZE);
                    dataMatrix = JaccardMatrix.createMatrixFromSets(dataShingles);
                    break;
                }
                case MULTISET: {
                    Map<Integer, int[]> dataShingles = Shingles.createMultisetsOfShingles(data, MIN_MOTIONWORD, MAX_MOTIONWORD, DATA_SHINGLE_SIZE);
                    dataMatrix = JaccardMatrix.createMatrixFromMultisets(dataShingles);
                    break;
                }
                case MINHASH: {
                    Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, MIN_MOTIONWORD, MAX_MOTIONWORD, DATA_SHINGLE_SIZE);
                    Map<Integer, int[]> dataMinhashes = mhc.createMinHashes(dataShingles);
                    dataMatrix = JaccardMatrix.createMatrixFromMinhashes(dataMinhashes);
                    break;
                }
            }

            Map<Integer, int[]> data_KNN = KNN.bulkExtractIndicesOfKHighest(dataMatrix, K_NEAREST_COUNT);


            //Final evaluation
            /*System.out.println(GTMatrix);
            System.out.println("_____________________________________________________________");
            System.out.println(dataMatrix);

            System.out.println("_____________________________________________________________");
            System.out.println("_____________________________________________________________");

            for (Map.Entry<Integer, int[]> entry : GT_KNN.entrySet()) {
                System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
            }
            System.out.println("_____________________________________________________________");
            for (Map.Entry<Integer, int[]> entry : data_KNN.entrySet()) {
                System.out.println(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
            }*/

            System.out.println("Final evaluation: " + KNN.bulkEvaluateKNN(GT_KNN, data_KNN) * 100 + "%");
        }
    }
}