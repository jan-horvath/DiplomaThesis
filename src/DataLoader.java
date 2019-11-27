import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataLoader {
    static private String matchFirstRegex(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            throw new InputMismatchException("Pattern was not found in string.");
        }
        return matcher.group(1);
    }

    /**
     * This function requires data to be in the following format: <sequenceId>_<classId>_<offset>_<length>
     *
     * @param filename - filename
     * @return map, which assigns a list of classIds (including duplicates) to every sequenceId
     * @throws IOException for non existing file
     */
    static public Map<Integer, List<Integer>> parseGroundTruthFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, List<Integer>> groundTruth = new HashMap<>();

        String line;
        Pattern sequenceIdPattern = Pattern.compile("^(\\d+?)_");
        Pattern classIdPattern = Pattern.compile("_(\\d+?)_");

        while ((line = br.readLine()) != null) {
            Integer sequenceId = Integer.parseInt(matchFirstRegex(sequenceIdPattern, line));
            int classId = Integer.parseInt(matchFirstRegex(classIdPattern, line));

            if (!groundTruth.containsKey(sequenceId)) {
                groundTruth.put(sequenceId, new ArrayList<>());
            }
            groundTruth.get(sequenceId).add(classId);
        }
        return groundTruth;
    }


    static public Map<Integer, List<Integer>> parseDataFile(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, List<Integer>> motions = new HashMap<>();

        String line;
        Pattern sequenceIdPattern = Pattern.compile("ObjectKey (\\d+?)_");
        Pattern motionWordsCountPattern = Pattern.compile("^(\\d+?);");

        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("#objectKey")) {

                Integer sequenceId = Integer.parseInt(matchFirstRegex(sequenceIdPattern, line));
                line = bufferedReader.readLine();
                int motionWordsCount = Integer.parseInt(matchFirstRegex(motionWordsCountPattern, line));

                List<Integer> motionWords = new ArrayList<>();
                for (int i = 0; i < motionWordsCount; ++i) {
                    line = bufferedReader.readLine();
                    motionWords.add(Integer.parseInt(line));
                }

                motions.put(sequenceId, motionWords);
            }
        }
        return motions;
    }
}
