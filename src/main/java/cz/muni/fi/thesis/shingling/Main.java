package cz.muni.fi.thesis.shingling;

import com.google.common.collect.BiMap;
import cz.muni.fi.thesis.shingling.evaluation.KNN;
import cz.muni.fi.thesis.shingling.evaluation.ThresholdKNN;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


public class Main {
    private enum Interpretation {
        SET, MULTISET, MINHASH, STRIDED_SET, STRIDED_MULTISET, STRIDED_MINHASH, OVERLAYS_SET, OVERLAYS_MULTISET
    }

    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt";
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    static private String overlayDataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350.data";

    //testing
    //static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\test_data_file.txt";
    //static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth_test.txt";

    private static final int DATA_SHINGLE_SIZE = 1;
    private static final int GT_SHINGLE_SIZE = 1; //ALWAYS 1
    private static final Interpretation GT_Interpretation = Interpretation.SET; //ALWAYS SET
    private static final Interpretation Data_Interpretation = Interpretation.SET;

    private static final int minK = 2;
    private static final int maxK = 3;

    private static double thresholdKNN(double threshold, int K, int TFIDF_percentage) throws IOException {
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        ShingleUtility.bulkAddToMap(groundTruth, 1);
        Map<Integer, boolean[]> GTShingles = ShingleUtility.createSetsOfShingles(groundTruth, 1, 1);
        ShingleUtility.resetMap();

        Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
        ShingleUtility.computeInverseDocumentFrequencyForShingles(data, minK, maxK, true);
        Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfShinglesUsingTFIDF(data, minK, maxK, TFIDF_percentage);
        //Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, minK, maxK);

        assert(GTShingles.size() == dataShingles.size());
        SimilarityMatrix dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles, false, true);
        Map<Integer, int[]> data_KNN = KNN.bulkExtractKNNIndices(dataMatrix, K);

        double result = 0.0;

        for (Map.Entry<Integer, int[]> entry : data_KNN.entrySet()) {
            Integer recordID = entry.getKey();
            for (int otherRecordID : entry.getValue()) {
                double weighedJaccard = Jaccard.computeJaccard(GTShingles.get(recordID), GTShingles.get(otherRecordID));
                if (weighedJaccard > threshold) {
                    result += 1.0;
                }
            }
        }
        return result/(K * GTShingles.size());
    }

    private static int countKShingles(boolean[] shingles, int K) {
        int counter = 0;
        BiMap<Shingle, Integer> shingleIDs = ShingleUtility.getShingleIDs();
        for (int i = 0; i < shingles.length; ++i) {
            if (shingles[i] && (shingleIDs.inverse().get(i).getSize() == K)) {
                ++counter;
            }
        }
        return counter;
    }

    private static double countKShinglesWeights(boolean[] shingles, int K) {
        double weightsSum = 0.0;
        BiMap<Shingle, Integer> shingleIDs = ShingleUtility.getShingleIDs();
        Map<Integer, Double> shingleWeights = ShingleUtility.getIDF();
        for (int i = 0; i < shingles.length; ++i) {
            if (shingles[i] && (shingleIDs.inverse().get(i).getSize() == K)) {
                weightsSum += shingleWeights.get(i);
            }
        }
        return weightsSum;
    }

    private static double countWeightsTotal(boolean[] shingles) {
        double weightsSum = 0.0;
        Map<Integer, Double> shingleWeights = ShingleUtility.getIDF();
        for (int i = 0; i < shingles.length; ++i) {
            if (shingles[i]) {
                weightsSum += shingleWeights.get(i);
            }
        }
        return weightsSum;
    }

    public static int countMatches(boolean[] set1, boolean[] set2, int K) {
        int intersection = 0;
        BiMap<Shingle, Integer> shingleIDs = ShingleUtility.getShingleIDs();

        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] && set2[i] && (shingleIDs.inverse().get(i).getSize() == K)) {
                ++intersection;
            }
        }
        return intersection;
    }

    public static double countWeightsOfMatches(boolean[] set1, boolean[] set2, int K) {
        double intersection = 0;
        BiMap<Shingle, Integer> shingleIDs = ShingleUtility.getShingleIDs();
        Map<Integer, Double> weights = ShingleUtility.getIDF();

        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] && set2[i] && (shingleIDs.inverse().get(i).getSize() == K)) {
                intersection += weights.get(i);
            }
        }
        return intersection;
    }

    private static SimilarityMatrix getMatrixFromFilteredData(Map<Integer, int[]> filtered_data, Map<Integer, boolean[]> dataShingles) {
        SimilarityMatrix filtered_matrix = new SimilarityMatrix();
        for (Map.Entry<Integer, int[]> entry : filtered_data.entrySet()) {
            filtered_matrix.getMatrix().put(entry.getKey(), new ArrayList<>());
            List<SimilarityMatrix.JaccardEntry> jaccardEntries = filtered_matrix.getMatrix().get(entry.getKey());
            for (int ID : entry.getValue()) {
                boolean[] set1 = dataShingles.get(entry.getKey());
                boolean[] set2 = dataShingles.get(ID);
                double wJaccard = Jaccard.computeWeighedJaccard(set1, set2, ShingleUtility.getIDF());
                jaccardEntries.add(new SimilarityMatrix.JaccardEntry(ID, wJaccard));
            }
        }
        return filtered_matrix;
    }

    public static void main(String[] args) throws IOException {

        //KNN with prior filtering
        /*for (int K = 2; K <= 18; K+=2) {
            Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
            Shingles.bulkAddToMap(groundTruth, 1);
            //Set ground truth
            Map<Integer, boolean[]> GTShingles = Shingles.createSetsOfShingles(groundTruth, 1, 1);
            SimilarityMatrix GTMatrix = SimilarityMatrix.createMatrixFromSets(GTShingles, false);
            //Multiset ground truth
            //Map<Integer, int[]> GTShingles = Shingles.createMultisetsOfShingles(groundTruth, 1);
            //SimilarityMatrix GTMatrix = SimilarityMatrix.createMatrixFromMultisets(GTShingles);

            Shingles.resetMap();

            Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
            Shingles.computeInverseDocumentFrequencyForShingles(data, 1, 3);
            Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, 1, 1);
            SimilarityMatrix dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles, true);
            dataShingles = Shingles.createSetsOfShingles(data, 2,3);

            Map<Integer, int[]> GT_KNN, data_KNN_from_filtered, data_KNN;

            if (K == 2) {
                Map<Integer, Integer> anySimilarity = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.001);
                Map<Integer, Integer> anySimilarity2 = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.001);
                for (Map.Entry<Integer, Integer> entry : anySimilarity2.entrySet()) {
                    entry.setValue(entry.getValue()*2);
                }

                Map<Integer, int[]> filtered_data = KNN.bulkExtractVariableKNNIndices(dataMatrix, anySimilarity2);
                SimilarityMatrix filtered_matrix = getMatrixFromFilteredData(filtered_data, dataShingles);
                GT_KNN = KNN.bulkExtractVariableKNNIndices(GTMatrix, anySimilarity);
                data_KNN_from_filtered = KNN.bulkExtractVariableKNNIndices(filtered_matrix, anySimilarity);
                data_KNN = KNN.bulkExtractVariableKNNIndices(dataMatrix, anySimilarity);
                System.out.println("Any similarity");
            } else if (K ==4) {
                Map<Integer, Integer> maxSimilarity = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.5);
                Map<Integer, Integer> maxSimilarity2 = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.5);
                for (Map.Entry<Integer, Integer> entry : maxSimilarity2.entrySet()) {
                    entry.setValue(entry.getValue()*2);
                }

                Map<Integer, int[]> filtered_data = KNN.bulkExtractVariableKNNIndices(dataMatrix, maxSimilarity2);
                SimilarityMatrix filtered_matrix = getMatrixFromFilteredData(filtered_data, dataShingles);
                GT_KNN = KNN.bulkExtractVariableKNNIndices(GTMatrix, maxSimilarity);
                data_KNN_from_filtered = KNN.bulkExtractVariableKNNIndices(filtered_matrix, maxSimilarity);
                data_KNN = KNN.bulkExtractVariableKNNIndices(dataMatrix, maxSimilarity);
                System.out.println("Max similarity");
            } else {
                Map<Integer, int[]> filtered_data = KNN.bulkExtractKNNIndices(dataMatrix, 2 * K);
                SimilarityMatrix filtered_matrix = getMatrixFromFilteredData(filtered_data, dataShingles);
                GT_KNN = KNN.bulkExtractKNNIndices(GTMatrix, K);
                data_KNN_from_filtered = KNN.bulkExtractKNNIndices(filtered_matrix, K);
                data_KNN = KNN.bulkExtractKNNIndices(dataMatrix, K);
                System.out.println(K);
            }

            System.out.println(KNN.bulkEvaluateKNN(GT_KNN, data_KNN_from_filtered));
            System.out.println(KNN.bulkEvaluateKNN(GT_KNN, data_KNN));
        }*/

        //Statistics of sequences
        /*FileWriter fw = new FileWriter(new File("sequence_statistics2.txt"));
        DecimalFormat df2 = new DecimalFormat("#.##");
        DecimalFormat df4 = new DecimalFormat("#.####");
        final int NEAREST = 10;

        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        Shingles.bulkAddToMap(groundTruth, 1);
        Map<Integer, boolean[]> GTShingles = Shingles.createSetsOfShingles(groundTruth, 1, 1);
        SimilarityMatrix GTMatrix = SimilarityMatrix.createMatrixFromSets(GTShingles, false);

        Shingles.resetMap();

        Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
        Shingles.computeInverseDocumentFrequencyForShingles(data, minK, maxK);
        Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, minK, maxK);
        SimilarityMatrix dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles, true);

        Map<Integer, int[]> filtered_matrix = KNN.bulkExtractKNNIndices(dataMatrix, NEAREST);

        for (Map.Entry<Integer, int[]> entry : filtered_matrix.entrySet()) {
            int sequenceID = entry.getKey();
            fw.write("Query sequence: " + sequenceID + ", countAllMWs=" + data.get(sequenceID).size());
            fw.write(", totalSumOfWeights=" + df2.format(countWeightsTotal(dataShingles.get(sequenceID))) + "\n");
            for (int shingleSize = minK; shingleSize <= maxK; ++shingleSize) {
                int shingleCount = countKShingles(dataShingles.get(sequenceID), shingleSize);
                double shinglesWeights = countKShinglesWeights(dataShingles.get(sequenceID), shingleSize);
                fw.write(shingleSize + "-shingles: ");
                fw.write("countDistinct=" + shingleCount + ", ");
                fw.write("sumOfWeights=" + df2.format(shinglesWeights) + "\n");
            }

            List<SimilarityMatrix.JaccardEntry> similarityDistances = dataMatrix.getMatrix().get(sequenceID);
            List<SimilarityMatrix.JaccardEntry> GTSimilarityDistances = GTMatrix.getMatrix().get(sequenceID);

            for (int N = 1; N <= NEAREST;  ++N) {
                SimilarityMatrix.JaccardEntry nearest = similarityDistances.get(N-1);
                SimilarityMatrix.JaccardEntry GTNearest = GTSimilarityDistances.stream()
                        .filter(jEntry -> jEntry.recordID == nearest.recordID)
                        .findFirst().orElse(null);

                fw.write("    " + N + ". nearest neighbor: " + nearest.recordID);
                fw.write(", mwDistance=" + df4.format(nearest.jaccardValue));
                fw.write(", gtDistance=" + df2.format(GTNearest.jaccardValue) + "\n");

                for (int shingleSize = minK; shingleSize <= maxK; ++shingleSize) {
                    int shingleCount = countKShingles(dataShingles.get(nearest.recordID), shingleSize);
                    double shinglesWeights = countKShinglesWeights(dataShingles.get(nearest.recordID), shingleSize);
                    fw.write("        " + shingleSize + "-shingles: ");
                    fw.write("countDistinct=" + shingleCount + ", ");
                    fw.write("sumOfWeights=" + df2.format(shinglesWeights) + "\n");

                    int matchCount = countMatches(dataShingles.get(sequenceID), dataShingles.get(nearest.recordID), shingleSize);
                    double matchWeights = countWeightsOfMatches(dataShingles.get(sequenceID), dataShingles.get(nearest.recordID), shingleSize);
                    fw.write("        matching " + shingleSize + "-shingles: ");
                    fw.write("countOfMatches=" + matchCount);
                    fw.write(", sumOfMatchWeights=" + df2.format(matchWeights) + "\n");
                }
            }
            fw.write("\n");
        }

        fw.flush();
        fw.close();*/

        //#4 - one shingle influence calculation
        /*
        Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
        Shingles.computeInverseDocumentFrequencyForShingles(data, minK, maxK);
        Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, minK, maxK);
        SimilarityMatrix.createMatrixFromSets(dataShingles, true);
        System.out.println(Jaccard.oneShingleInfluenceAverage);
        System.out.println(Jaccard.nonEmptyIntersections);
        System.out.println(Jaccard.oneShingleInfluenceAverage/Jaccard.nonEmptyIntersections);
        System.out.println(Jaccard.oneShingleInfluenceAverage/58081);*/

        // #3 - Threshold KNN
        DecimalFormat df3 = new DecimalFormat("#.##");
        for (int topXpercent = 10; topXpercent <= 100; topXpercent += 10) {
            System.out.println("\nX = " + topXpercent);
            for (int K = 1; K <= 15; ++K) {
                Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
                ShingleUtility.bulkAddToMap(groundTruth, 1);
                Map<Integer, boolean[]> GTShingles = ShingleUtility.createSetsOfShingles(groundTruth, 1, 1);
                ShingleUtility.resetMap();

                Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
                ShingleUtility.computeInverseDocumentFrequencyForShingles(data, minK, maxK, true);
                Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfShinglesUsingTFIDF(data, minK, maxK, topXpercent);
                //Map<Integer, boolean[]> dataShingles = Shingles.createSetsOfShingles(data, minK, maxK);

                assert(GTShingles.size() == dataShingles.size());
                SimilarityMatrix dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles, false, true);
                Map<Integer, int[]> data_KNN = KNN.bulkExtractKNNIndices(dataMatrix, K);

                System.out.println(df3.format(100*ThresholdKNN.evaluate(GTShingles, data_KNN, 0.0)));
            }
        }

        // #2 - weighed jaccard
        /*for (int i = 4; i <= 18; i+=2) {
            //GT similarity
            Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
            ShingleUtility.bulkAddToMap(groundTruth, 1);
            //Map<Integer, boolean[]> GTShingles = ShingleUtility.createSetsOfShingles(groundTruth, 1, 1);
            //SimilarityMatrix GTMatrix = SimilarityMatrix.createMatrixFromSets(GTShingles, false);
            Map<Integer, int[]> GTShingles = ShingleUtility.createMultisetsOfShingles(groundTruth,1);
            SimilarityMatrix GTMatrix = SimilarityMatrix.createMatrixFromMultisets(GTShingles);

            ShingleUtility.resetMap();

            //Data similarity
            Map<Integer, List<Integer>> data = DataLoader.parseDataFile(dataFile);
            ShingleUtility.computeInverseDocumentFrequencyForShingles(data, minK, maxK, false);
            Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfShingles(data, minK, maxK);
            SimilarityMatrix dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles, false);

            //Evaluation
            if (i == 4) {
                Map<Integer, Integer> maxSimilarK = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.5);
                Map<Integer, int[]> GT_KNN_max = KNN.bulkExtractVariableKNNIndices(GTMatrix, maxSimilarK);
                Map<Integer, int[]> data_KNN_max = KNN.bulkExtractVariableKNNIndices(dataMatrix, maxSimilarK);
                System.out.println("Max similarity KNN: " + KNN.bulkEvaluateKNN(GT_KNN_max, data_KNN_max));
            }

            if (i == 6) {
                Map<Integer, Integer> anySimilarKX = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(GTMatrix, 0.01);
                Map<Integer, int[]> GT_KNN_anyX = KNN.bulkExtractVariableKNNIndices(GTMatrix, anySimilarKX);
                Map<Integer, int[]> data_KNN_anyX = KNN.bulkExtractVariableKNNIndices(dataMatrix, anySimilarKX);
                System.out.println("Any similarity KNN: " + KNN.bulkEvaluateKNN(GT_KNN_anyX, data_KNN_anyX));
            }

            if (i >= 8) {
                Map<Integer, int[]> GT_KNN = KNN.bulkExtractKNNIndices(GTMatrix, i);
                Map<Integer, int[]> filtered_matrix = KNN.bulkExtractKNNIndices(dataMatrix, i);
                System.out.println(i + "-NN: " + KNN.bulkEvaluateKNN(GT_KNN, filtered_matrix));
            }
        }

        // #1
        /*
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

                ShingleUtility.resetMap();
                for (List<Integer> list : groundTruth.values()) {
                    ShingleUtility.addToMap(list, GT_SHINGLE_SIZE);
                }

                switch (GT_Interpretation) {
                    case SET: {
                        Map<Integer, boolean[]> GTShingles = ShingleUtility.createSetsOfShingles(groundTruth, GT_SHINGLE_SIZE, 1);
                        GTMatrix = SimilarityMatrix.createMatrixFromSets(GTShingles, false);
                        break;
                    }
                    case MULTISET: {
                        Map<Integer, int[]> GTShingles = ShingleUtility.createMultisetsOfShingles(groundTruth, GT_SHINGLE_SIZE);
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
                ShingleUtility.resetMap();
                SimilarityMatrix dataMatrix = null;

                MinHashCreator mhc = null;

                switch (Data_Interpretation) {
                    case SET: {
                        ShingleUtility.bulkAddToMap(data, DATA_SHINGLE_SIZE);
                        Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfShingles(data, DATA_SHINGLE_SIZE, 1);
                        dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles, false);
                        break;
                    }
                    case MULTISET: {
                        ShingleUtility.bulkAddToMap(data, DATA_SHINGLE_SIZE);
                        Map<Integer, int[]> dataShingles = ShingleUtility.createMultisetsOfShingles(data, DATA_SHINGLE_SIZE);
                        dataMatrix = SimilarityMatrix.createMatrixFromMultisets(dataShingles);
                        break;
                    }
                    case MINHASH: {
                        ShingleUtility.bulkAddToMap(data, DATA_SHINGLE_SIZE);
                        mhc = new MinHashCreator(ShingleUtility.getShingleIDs().size(), HASH_FUNCTION_COUNT);
                        Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfShingles(data, DATA_SHINGLE_SIZE, 1);
                        Map<Integer, int[]> dataMinhashes = mhc.createMinHashes(dataShingles);
                        dataMatrix = SimilarityMatrix.createMatrixFromMinhashes(dataMinhashes);
                        break;
                    }
                    case STRIDED_SET: {
                        ShingleUtility.bulkAddToMapWithStride(data, DATA_SHINGLE_SIZE);
                        Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfStridedShingles(data, DATA_SHINGLE_SIZE);
                        dataMatrix = SimilarityMatrix.createMatrixFromSets(dataShingles, false);
                        break;
                    }
                    case STRIDED_MULTISET: {
                        ShingleUtility.bulkAddToMapWithStride(data, DATA_SHINGLE_SIZE);
                        Map<Integer, int[]> dataShingles = ShingleUtility.createMultisetsOfStridedShingles(data, DATA_SHINGLE_SIZE);
                        dataMatrix = SimilarityMatrix.createMatrixFromMultisets(dataShingles);
                        break;
                    }
                    case STRIDED_MINHASH: {
                        ShingleUtility.bulkAddToMapWithStride(data, DATA_SHINGLE_SIZE);
                        mhc = new MinHashCreator(ShingleUtility.getShingleIDs().size(), HASH_FUNCTION_COUNT);
                        Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfStridedShingles(data, DATA_SHINGLE_SIZE);
                        Map<Integer, int[]> dataMinhashes = mhc.createMinHashes(dataShingles);
                        dataMatrix = SimilarityMatrix.createMatrixFromMinhashes(dataMinhashes);
                        break;
                    }
                    case OVERLAYS_SET: {
                        dataMatrix = SimilarityMatrix.createMatrixFromOverlayData(overlayData, 1, true);
                    }
                    case OVERLAYS_MULTISET: {
                        dataMatrix = SimilarityMatrix.createMatrixFromOverlayData(overlayData, 1, false);
                    }
                }

                if ((i == 0) && (R == 0)) System.out.println("# of shingles: " + ShingleUtility.getShingleIDs().size());

                Map<Integer, int[]> filtered_matrix = null;
                Map<Integer, int[]> data_variableKNN_hundred = null;
                Map<Integer, int[]> data_variableKNN_aboveZero = null;



                switch (i) {
                    case 0: {
                        filtered_matrix = KNN.bulkExtractKNNIndices(dataMatrix, 12);
                        sum_eval_knn12 += KNN.bulkEvaluateKNN(GT_KNN, filtered_matrix);

                        break;
                    }
                    case 1: {
                        filtered_matrix = KNN.bulkExtractKNNIndices(dataMatrix, 15);
                        sum_eval_knn15 += KNN.bulkEvaluateKNN(GT_KNN, filtered_matrix);
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
        System.out.println("Final evaluation (partial): " + sum_eval_partial/repeat * 100 + "%");*/
    }
}