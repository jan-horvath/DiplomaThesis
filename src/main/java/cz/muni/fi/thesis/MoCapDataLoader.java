package cz.muni.fi.thesis;

import org.w3c.dom.ls.LSParserFilter;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MoCapDataLoader {

    private static final String HMW_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    private static final String HMW_MIXED_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350-halfCategorySwitched.data";
    private static final String MOMW_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350.data";
    private static final String MOMW_MIXED_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350-halfCategorySwitched.data";
    private static final String ORDER_FREE_SCENARIOS_PATH = System.getProperty("user.dir") + "\\MW_database\\hdm05-scenarios.txt";
    private static final String ORDER_SENSITIVE_SCENARIOS_PATH = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\ground_truth-sequence_scenarios-halfCategorySwitched-respectOrdering.txt";

    private static final int OVERLAYS_COUNT = 5;

    public static MoCapData loadData() throws IOException {
        MoCapData moCapData = new MoCapData();
        moCapData.setHmwDataset(parseHmwDataFile(HMW_DATASET_PATH));
        moCapData.setHmwMixedDataset(parseHmwDataFile(HMW_MIXED_DATASET_PATH));
        moCapData.setMomwDataset(parseMomwDataFile(MOMW_DATASET_PATH));
        moCapData.setMomwMixedDataset(parseMomwDataFile(MOMW_MIXED_DATASET_PATH));
        moCapData.setOrderFreeScenarios(parseScenarioFile(ORDER_FREE_SCENARIOS_PATH, false));
        moCapData.setOrderSensitiveScenarios(parseScenarioFile(ORDER_SENSITIVE_SCENARIOS_PATH, true));
        return moCapData;
    }


    static private Pattern numberPattern = Pattern.compile("\\d+");



    /**
     * This function expects the data to contain individual MoCap recording represented by hard motion words.
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
     *
     * Access level: package private for testing
     */
    static Map<Integer, List<Integer>> parseHmwDataFile(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, List<Integer>> motions = new HashMap<>();

        String line;
        //Pattern sequenceIdPattern = Pattern.compile("ObjectKey (\\d+?)_");
        Pattern sequenceIdPattern = Pattern.compile("[ -](\\d+?)_");
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
     * @return map, which assigns a list of motionwords (including duplicates) to every sequenceId
     * @throws IOException for non existing file
     *
     * Access level: package private for testing
     */
    static Map<Integer, List<int[]>> parseMomwDataFile(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, List<int[]>> motions = new HashMap<>();

        String line;
        Pattern sequenceIdPattern = Pattern.compile("[ -](\\d+?)_");
        Pattern motionWordsCountPattern = Pattern.compile("^(\\d+?);");

        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("#objectKey")) {

                Integer sequenceId = Integer.parseInt(matchFirstRegex(sequenceIdPattern, line));
                line = bufferedReader.readLine();
                int motionWordsCount = Integer.parseInt(matchFirstRegex(motionWordsCountPattern, line));

                List<int[]> motionWords = new ArrayList<>();
                for (int i = 0; i < motionWordsCount; ++i) {
                    line = bufferedReader.readLine();
                    motionWords.add(matchCommaSeparatedNumbers(line, OVERLAYS_COUNT));
                }

                motions.put(sequenceId, motionWords);
            }
        }
        return motions;
    }

    static private Map<Integer, String> parseScenarioFile(String filename, boolean halvesSwitched) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, String> motions = new HashMap<>();

        if (!halvesSwitched) {
            String line = bufferedReader.readLine();
            assert (line.equals("seqId;HDM_seqId"));

            Pattern sequenceIdPattern = Pattern.compile("(\\d+?);");
            Pattern scenarioPattern = Pattern.compile("(\\d\\d-\\d\\d)");

            while ((line = bufferedReader.readLine()) != null) {
                Integer sequenceId = Integer.parseInt(matchFirstRegex(sequenceIdPattern, line));
                String scenario = matchFirstRegex(scenarioPattern, line);
                motions.put(sequenceId, scenario);
            }
        } else {
            String line;
            Pattern sequenceIdPattern = Pattern.compile("(\\d+?)_");
            Pattern scenarioPattern = Pattern.compile("(\\d\\d-\\d\\d)");

            while ((line = bufferedReader.readLine()) != null) {
                Integer sequenceId = Integer.parseInt(matchFirstRegex(sequenceIdPattern, line));
                String scenario = matchFirstRegex(scenarioPattern, line);
                if (line.contains("SwitchedHalf")) {
                    motions.put(sequenceId, scenario + "S");
                } else {
                    motions.put(sequenceId, scenario);
                }
            }
        }

        return motions;
    }

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
}
