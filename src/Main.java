import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth_test.txt";
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    static public final int MIN_CLASS = 22;
    static public final int MAX_CLASS = 152;

    public static void main(String[] args) throws IOException {
        Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);

        Map<Integer, int[]> groundTruthShingles = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : groundTruth.entrySet()) {
            int[] shingles = Shingles.createSetOfShingles(entry.getValue(), 100, 114, 1);
            System.out.println(entry.getValue());
            System.out.println(Arrays.toString(shingles));
            groundTruthShingles.put(entry.getKey(), shingles);
        }


        List<List<Double>> confusionMatrix = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 1; j <= 4 ; j++) {
                double jaccard = Jaccard.computeJaccardOnMultisets(groundTruthShingles.get(i), groundTruthShingles.get(j));
                System.out.println(i + " & " + j + ": " + jaccard*100 + "%");
                row.add(jaccard);
            }
            confusionMatrix.add(row);
        }

        ConfusionMatrixImage image = new ConfusionMatrixImage(confusionMatrix, 100, 0.5);
        image.saveImage("confusionMatrixOnTestData.jpg");
    }
}