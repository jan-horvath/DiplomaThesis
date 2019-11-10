import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    static private File groundTruthFile = new File(System.getProperty("user.dir") + "\\MW_database\\ground_truth_test.txt");
    //static private File groundTruthFile = new File(System.getProperty("user.dir") + "\\MW_database\\ground_truth-sequence_actions.txt");
    static private File dataFile = new File(System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data");
    static public final int MIN_CLASS = 22;
    static public final int MAX_CLASS = 152;

    static private Map<Integer, int[]> parseGroundTruthFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(groundTruthFile));
        Map<Integer, int[]> groundTruth = new HashMap<>();

        String line;
        Pattern sequenceIdPattern = Pattern.compile("^(\\d+?)_");
        Pattern classIdPattern = Pattern.compile("_(\\d+?)_");
        Matcher sequenceIdMatcher, classIdMatcher;
        while ((line = br.readLine()) != null) { //TODO refactor parsing
            sequenceIdMatcher = sequenceIdPattern.matcher(line);
            classIdMatcher = classIdPattern.matcher(line);
            if (!sequenceIdMatcher.find() || !classIdMatcher.find()) {
                throw new InputMismatchException("Incorrect input format. Expected: <sequenceId>_<classId>_<offset>_<length>");
            }

            Integer sequenceId = Integer.parseInt(sequenceIdMatcher.group(1));
            int classId = Integer.parseInt(classIdMatcher.group(1)) - MIN_CLASS;
            if (!groundTruth.containsKey(sequenceId)) {
                groundTruth.put(sequenceId, new int[MAX_CLASS - MIN_CLASS + 1]);
                ++groundTruth.get(sequenceId)[classId];
            } else {
                ++groundTruth.get(sequenceId)[classId];
            }
        }
        return groundTruth;
    }

    /*static private Map<Integer, int[]> parseDataFile() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dataFile));
        Map<Integer, int[]> groundTruth = new HashMap<>();

        String line;
        Pattern sequenceIdPattern = Pattern.compile("ObjectKey (\\d+?)_");
        Pattern motionWordsCountPattern = Pattern.compile("^(\\d+?);");
        Matcher sequenceIdMatcher, motionWordsCountMatcher;
        while ((line = br.readLine()) != null) {
            if (line.contains("#objectKey")) {//TODO refactor parsing

                sequenceIdMatcher = sequenceIdPattern.matcher(line);
                if (!sequenceIdMatcher.find()) {
                    throw new InputMismatchException("Incorrect input format. Expected: #objectKey messif.objects.keys.AbstractObjectKey <sequenceId>_0_0_0");
                }
                Integer sequenceId = Integer.parseInt(sequenceIdMatcher.group(1));

                line = br.readLine();
                motionWordsCountMatcher = motionWordsCountPattern.matcher(line);
                if (!motionWordsCountMatcher.find()) {
                    throw new InputMismatchException("Incorrect input format. Expected: <motionWordsCount>;mcdr.objects.impl.ObjectMotionWord");
                }
                int motionWordsCount = Integer.parseInt(motionWordsCountMatcher.group(1));

                for (int i = 0; i < motionWordsCount; ++i) {

                }
            }
        }
    }*/

    public static void main(String[] args) throws IOException {
        //TODO play with regexes
        Map<Integer, int[]> groundTruth = parseGroundTruthFile();
    }
}