import java.io.*;
import java.util.List;
import static java.util.Arrays.*;

public class Main {
    static private String groundTruthFile = System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt";
    static private String dataFile = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    static public final int MIN_CLASS = 22;
    static public final int MAX_CLASS = 152;

    public static void main(String[] args) throws IOException {
        /*Map<Integer, List<Integer>> groundTruth = DataLoader.parseGroundTruthFile(groundTruthFile);
        Map<Integer, List<Integer>> motionsData = DataLoader.parseDataFile(dataFile);

        for (Map.Entry<Integer, List<Integer>> entry : groundTruth.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();


        for (Map.Entry<Integer, List<Integer>> entry : motionsData.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }*/

        List<List<Double>> list = asList(
                asList( 0.1, 0.2, 0.3, -1.0 ),
                asList( 0.4, 0.5, 0.6, -1.0 ),
                asList(0.0, 0.0, 1.0, 0.0),
                asList( -0.6, -0.7, -0.8, 0.0 ) );
        ConfusionMatrixImage image = new ConfusionMatrixImage(list, 200);
        image.saveImage("testConfMatrix.jpg");
    }
}