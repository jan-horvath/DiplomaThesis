package cz.muni.fi.thesis.dataloader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MoCapDataLoader {

    private static final String HMW_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350.data";
    private static final String HMW_MIXED_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-kmedoids350-halfCategorySwitched.data";
    private static final String MOMW_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350.data";
    private static final String MOMW_MIXED_DATASET_PATH = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\hdm05-sequences_annotations_specific-segment80_shift16-coords_normPOS-fps12-quantized-overlays5-kmedoids350-halfCategorySwitched.data";
    private static final String ORDER_FREE_SCENARIOS_PATH = System.getProperty("user.dir") + "\\MW_database\\hdm05-scenarios.txt";
    private static final String ORDER_SENSITIVE_SCENARIOS_PATH = System.getProperty("user.dir") + "\\MW_database\\Halfswitched\\ground_truth-sequence_scenarios-halfCategorySwitched-respectOrdering.txt";

    private static final int OVERLAYS_COUNT = 5;

    /**
     * Loads the data into the MoCapData class from files from predefined paths
     * @return Loaded MoCap data
     * @throws IOException if any file is not found
     */
    public static MoCapData loadData() throws IOException {
        MoCapData moCapData = new MoCapData();
        moCapData.setHMWs(parseHmwDataFile(HMW_DATASET_PATH));
        moCapData.setMixedHMWs(parseHmwDataFile(HMW_MIXED_DATASET_PATH));
        moCapData.setMOMWs(parseMomwDataFile(MOMW_DATASET_PATH));
        moCapData.setMixedMOMWs(parseMomwDataFile(MOMW_MIXED_DATASET_PATH));

        Map<Integer, String> OFScenarios = parseScenarioFile(ORDER_FREE_SCENARIOS_PATH, false);
        moCapData.setOFScenarios(OFScenarios);
        moCapData.setOFVariableK(computeVariableK(OFScenarios));

        Map<Integer, String> OSScenarios = parseScenarioFile(ORDER_SENSITIVE_SCENARIOS_PATH, true);
        moCapData.setOSScenarios(OSScenarios);
        moCapData.setOSVariableK(computeVariableK(OSScenarios));

        return moCapData;
    }

    static private Pattern numberPattern = Pattern.compile("\\d+");

    /**
     * This function expects the data to contain individual MoCap recordings represented by hard motion words.
     * Every recording should start with the following two lines:
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
    private static Map<Integer, List<Integer>> parseHmwDataFile(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filename)));
        Map<Integer, List<Integer>> motions = new HashMap<>();

        String line;
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
     * This function expects the data to contain individual MoCap recordings represented by multi-overlay motion words.
     * Every recording should start with the following two lines:
     *
     * #objectKey messif.objects.keys.AbstractObjectKey <sequenceId>_0_0_0
     * <number_of_motionwords>;mcdr.objects.impl.ObjectMotionWord
     *
     * followed by <number_of_motionwords> lines. Each line should contain OVERLAYS_COUNT comma separated motion
     * words (integers)
     *
     * @param filename - filename
     * @return map, which assigns a list of motionwords (including duplicates) to every sequenceId
     * @throws IOException for non existing file
     */
    private static Map<Integer, List<int[]>> parseMomwDataFile(String filename) throws IOException {
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

    /**
     * The function expects a file containing scenarios for each episode.
     *
     * If the halvesSwitched parameter is false then the format of the file should start with line:
     * seqId;HDM_seqId
     * followed by lines of the format <sequence id>;<2-letter ID of the actor>_<scenario id>_<take number>
     *     example: 3156;bd_02-03_04
     *
     * If the the halvesSwitched parameter is true then the format of each line is either:
     * <sequence id>_<scenario id> (example: 3439_01-06)
     *     or
     * SwitchedHalf-<sequence id>_<scenario id>-switched (example: SwitchedHalf-3433_01-06-switched)
     * @param filename - filename
     * @param halvesSwitched - explained above
     * @return map, which assigns a String scenario identifier to the sequence ID
     * @throws IOException
     */
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

    private static Map<Integer, Integer> computeVariableK(Map<Integer, String> scenarios) {
        Map<Integer, Integer> variableK = new HashMap<>();
        Map<String, Integer> scenarioCount = new HashMap<>();

        for (Map.Entry<Integer, String> scenario : scenarios.entrySet()) {
            String scenarioName = scenario.getValue();
            if (!scenarioCount.containsKey(scenarioName)) {
                scenarioCount.put(scenarioName, 1);
            } else {
                scenarioCount.put(scenarioName, scenarioCount.get(scenarioName) + 1);
            }
        }

        for (Map.Entry<Integer, String> scenario : scenarios.entrySet()) {
            variableK.put(scenario.getKey(), scenarioCount.get(scenario.getValue()));
        }
        return variableK;
    }
}
