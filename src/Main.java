import java.io.*;
import java.util.*;


public class Main {
    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt";
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    private static final int MIN_CLASS = 22;
    private static final int MAX_CLASS = 152;
    private static final int MIN_MOTIONWORD = 0;
    private static final int MAX_MOTIONWORD = 349;

    private static List<List<Double>> getGroundTruthMatrix() throws IOException {
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);

        Map<Integer, int[]> groundTruthShingles = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : groundTruth.entrySet()) {
            int[] shingles = Shingles.createMultisetOfShingles(entry.getValue(), MIN_CLASS, MAX_CLASS, 1);
            groundTruthShingles.put(entry.getKey(), shingles);
        }


        return getMultisetMatrix(groundTruthShingles);
    }

    private static List<List<Double>> getMultisetMotionMatrix(int shingleSize) throws IOException {
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseDataFile(dataFile);

        Map<Integer, int[]> groundTruthShingles = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : groundTruth.entrySet()) {
            int[] shingles = Shingles.createMultisetOfShingles(entry.getValue(), MIN_MOTIONWORD, MAX_MOTIONWORD, shingleSize);
            groundTruthShingles.put(entry.getKey(), shingles);
        }

        return getMultisetMatrix(groundTruthShingles);
    }

    private static List<List<Double>> getMinHashMotionMatrix(int shingleSize) throws IOException {
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseDataFile(dataFile);
        MinHashCreator minHashCreator = new MinHashCreator((int) Math.pow(MAX_MOTIONWORD - MIN_MOTIONWORD + 1, shingleSize), 1000);

        Map<Integer, int[]> minHashedData = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : groundTruth.entrySet()) {
            boolean[] shingles = Shingles.createSetOfShingles(entry.getValue(), MIN_MOTIONWORD, MAX_MOTIONWORD, shingleSize);
            int[] minHashShingles = minHashCreator.createMinHash(shingles);
            System.out.println(entry.getKey() + ": " + Arrays.toString(minHashShingles));
            minHashedData.put(entry.getKey(), minHashShingles);
        }

        List<List<Double>> confusionMatrix = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entry1 : minHashedData.entrySet()) {
            confusionMatrix.add(new ArrayList<>());
            for (Map.Entry<Integer, int[]> entry2 : minHashedData.entrySet()) {
                double jaccard = Jaccard.computeJaccardOnMinhashes(entry1.getValue(), entry2.getValue());
                confusionMatrix.get(confusionMatrix.size()-1).add(jaccard);
            }
        }

        return confusionMatrix;
    }

    private static List<List<Double>> getMultisetMatrix(Map<Integer, int[]> groundTruthShingles) {
        List<List<Double>> confusionMatrix = new ArrayList<>();
        for (Map.Entry<Integer, int[]> entry1 : groundTruthShingles.entrySet()) {
            confusionMatrix.add(new ArrayList<>());
            for (Map.Entry<Integer, int[]> entry2 : groundTruthShingles.entrySet()) {
                double jaccard = Jaccard.computeJaccardOnMultisets(entry1.getValue(), entry2.getValue());
                confusionMatrix.get(confusionMatrix.size()-1).add(jaccard);
            }
        }
        return confusionMatrix;
    }


    public static void main(String[] args) throws IOException {
        Map<Integer, List<Integer>> GTdata = DataLoader.parseGroundTruthFile(System.getProperty("user.dir") + "\\MW_database\\ground_truth_test2.txt");
        Map<Integer, boolean[]> setsOfShingles = Shingles.createSetsOfShingles(GTdata, 100, 110, 1);

        MinHashCreator mhc = new MinHashCreator(11, 1000000);
        Map<Integer, int[]> minHashes = mhc.createMinHashes(setsOfShingles);

        for (Map.Entry<Integer, int[]> entry : minHashes.entrySet()) {
            System.out.println(entry.getKey() + ": " +  Arrays.toString(entry.getValue()));
        }

        JaccardMatrix jaccardMatrixSets = JaccardMatrix.createMatrixFromSets(setsOfShingles);
        JaccardMatrix jaccardMatrixMinhashes = JaccardMatrix.createMatrixFromMinhashes(minHashes);

        System.out.println(jaccardMatrixSets);
        System.out.println("_____________________________________________________________");
        System.out.println(jaccardMatrixMinhashes);
    }
}