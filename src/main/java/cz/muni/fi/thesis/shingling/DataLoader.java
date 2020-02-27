package cz.muni.fi.thesis.shingling;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataLoader {
    static private Pattern numberPattern = Pattern.compile("\\d+");

    static private String matchFirstRegex(Pattern pattern, String string) {
        Matcher matcher = pattern.matcher(string);
        if (!matcher.find()) {
            throw new InputMismatchException("Pattern was not found in string.");
        }
        return matcher.group(1);
    }

    private static int[] matchCommaSeparatedNumbers(String string, int count) {
        Matcher matcher = numberPattern.matcher(string);
        if (!matcher.find()) {
            throw new InputMismatchException("Pattern was not found in string.");
        }

        int[] numbers = new int[count];
        for (int i = 0; i < count; ++i) {
            numbers[i] = Integer.parseInt(matcher.group(0));
            matcher.find();
        }
        return numbers;
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

    /**
     * This function expects the data to contain individual MoCap recording represented by motion words.
     * Every recording should start with:
     *
     * #objectKey messif.objects.keys.AbstractObjectKey <sequenceId>_0_0_0
     * <number_of_motionwords>;mcdr.objects.impl.ObjectMotionWord
     *
     * followed by <number_of_motionwords> lines. Each line should contain one motionword (integer)
     *
     * @param filename - filename
     * @return map, which assigns a list of motionwords (including duplicates) to every sequenceId
     * @throws IOException for non existing file
     */
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


    /**
     * This function expects the data to contain individual MoCap recording represented by motion words.
     * Every recording should start with:
     *
     * #objectKey messif.objects.keys.AbstractObjectKey <sequenceId>_0_0_0
     * <number_of_motionwords>;mcdr.objects.impl.ObjectMotionWord
     *
     * followed by <number_of_motionwords> lines. Each line should contain {@code overlaysCount} comma separated motion
     * words (integers)
     *
     * @param filename - filename
     * @param overlaysCount - number of overlays used to create the motion words
     * @return map, which assigns a list of motionwords (including duplicates) to every sequenceId
     * @throws IOException for non existing file
     */
    static public Map<Integer, List<int[]>> parseOverlayDataFile(String filename, int overlaysCount) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, List<int[]>> motions = new HashMap<>();

        String line;
        Pattern sequenceIdPattern = Pattern.compile("ObjectKey (\\d+?)_");
        Pattern motionWordsCountPattern = Pattern.compile("^(\\d+?);");

        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("#objectKey")) {

                Integer sequenceId = Integer.parseInt(matchFirstRegex(sequenceIdPattern, line));
                line = bufferedReader.readLine();
                int motionWordsCount = Integer.parseInt(matchFirstRegex(motionWordsCountPattern, line));

                List<int[]> motionWords = new ArrayList<>();
                for (int i = 0; i < motionWordsCount; ++i) {
                    line = bufferedReader.readLine();
                    motionWords.add(matchCommaSeparatedNumbers(line, overlaysCount));
                }

                motions.put(sequenceId, motionWords);
            }
        }
        return motions;
    }

    static public Map<Integer, String> parseScenarioFile(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, String> motions = new HashMap<>();

        String line = bufferedReader.readLine();
        assert(line.equals("seqId;HDM_seqId"));

        Pattern sequenceIdPattern = Pattern.compile("(\\d+?);");
        Pattern scenarioPattern = Pattern.compile("(\\d\\d-\\d\\d)");

        while ((line = bufferedReader.readLine()) != null) {
            Integer sequenceId = Integer.parseInt(matchFirstRegex(sequenceIdPattern, line));
            String scenario = matchFirstRegex(scenarioPattern, line);
            motions.put(sequenceId, scenario);
        }

        return motions;
    }
}
