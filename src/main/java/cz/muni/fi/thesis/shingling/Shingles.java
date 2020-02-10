package cz.muni.fi.thesis.shingling;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.text.DecimalFormat;
import java.util.*;

public class Shingles {

    //State of this class
    private static Map<Integer, Double> IDF = new HashMap<>();
    private static BiMap<Shingle, Integer> shingleIDs = HashBiMap.create();
    private static Set<Integer> maxIDFShingles = new HashSet<>();

    public static Set<Integer> getMaxIDFShingles() {
        return maxIDFShingles;
    }

    public static BiMap<Shingle, Integer> getShingleIDs() {
        return shingleIDs;
    }

    public static int getNextID() {
        return nextID;
    }

    public static Map<Integer, Double> getIDF() {
        return IDF;
    }

    private static int nextID = 0;

    private static int nextShingleID() {
        return nextID++;
    }

    private static void resetNextID() {
        nextID = 0;
    }

    public static void resetMap() {
        resetNextID();
        shingleIDs = HashBiMap.create();
    }

    public static int getMapSize() {
        return shingleIDs.size();
    }

    public static Shingle getShingleFromID(int ID) {
        Shingle shingle = shingleIDs.inverse().get(ID);
        if (shingle != null) {
            return shingle;
        }
        throw new IllegalStateException("ID not found");
    }

    /**
     * Adds new k-shingles to the map
     * @param sequence Sequence of integers from which the shingles should be extracted
     * @param k Size of the shingles
     */
    public static void addToMap(List<Integer> sequence, int k) {
        for (int i = 0; i < sequence.size() - k + 1; ++i) {
            Shingle newShingle = new Shingle(sequence.subList(i, i+k));
            if (!shingleIDs.containsKey(newShingle)) {
                shingleIDs.put(newShingle, nextShingleID());
            }
        }
    }

    public static void bulkAddToMap(Map<Integer, List<Integer>> data, int k) {
        for (List<Integer> list : data.values()) {
            Shingles.addToMap(list, k);
        }
    }

    public static Set<Shingle> getShinglesFromSequence(List<Integer> sequence, int minK, int maxK) {
        Set<Shingle> shinglesFound = new HashSet<>();
        for (int K = minK; K <= maxK; ++K) {
            for (int i = 0; i < sequence.size() - K + 1; ++i) {
                Shingle shingle = new Shingle(sequence.subList(i, i + K));
                shinglesFound.add(shingle);
            }
        }
        return shinglesFound;
    }

    public static void computeInverseDocumentFrequencyForShingles(Map<Integer, List<Integer>> data, int minK, int maxK, boolean ignoreMaxIDF) {
        Map<Shingle, Integer> shingleOccurrences = new HashMap<>();
        for (List<Integer> sequence : data.values()) {
            Set<Shingle> shinglesFound = getShinglesFromSequence(sequence, minK, maxK);
            for (Shingle shingle : shinglesFound) {
                if (shingleOccurrences.containsKey(shingle)) {
                    Integer i = shingleOccurrences.get(shingle);
                    shingleOccurrences.put(shingle, i+1);
                } else {
                    shingleOccurrences.put(shingle, 1);
                }
            }
        }

        for (int K = minK; K <= maxK; ++K) {
            bulkAddToMap(data, K);
        }

        double size = data.size();

        for (Map.Entry<Shingle, Integer> entry : shingleOccurrences.entrySet()) {
            if (ignoreMaxIDF && entry.getValue() == 1) {
                IDF.put(shingleIDs.get(entry.getKey()), 0.0);
                maxIDFShingles.add(shingleIDs.get(entry.getKey()));
            } else {
                IDF.put(shingleIDs.get(entry.getKey()), Math.log(size / entry.getValue()));
            }
        }

        /*
        Len vypisy
        double min1 = 10.0, min2 = 10.0, min3 = 10.0;
        double max1 = 0.0, max2 = 0.0, max3 = 0.0;

        for (Map.Entry<Integer, Double> entry : IDF.entrySet()) {
            if (entry.getValue() > Math.log(241) - 0.003) {continue;}
            switch (shingleIDs.inverse().get(entry.getKey()).getSize()) {
                case 1 : {
                    min1 = Math.min(min1, entry.getValue());
                    max1 = Math.max(max1, entry.getValue());
                    break;
                }
                case 2 : {
                    min2 = Math.min(min2, entry.getValue());
                    max2 = Math.max(max2, entry.getValue());
                    break;
                }
                case 3: {
                    min3 = Math.min(min3, entry.getValue());
                    max3 = Math.max(max3, entry.getValue());
                    break;
                }
            }
        }
        System.out.println("MIN/MAX weights for 1-shingles: " + min1 + "\\" + max1);
        System.out.println("MIN/MAX weights for 2-shingles: " + min2 + "\\" + max2);
        System.out.println("MIN/MAX weights for 3-shingles: " + min3 + "\\" + max3);*/
    }

    private static List<List<Integer>> convertToSequencesWithStride(List<Integer> sequence, int stride) {
        List<List<Integer>> sequences = new ArrayList<>();
        for (int i = 0; i < stride; ++i) {
            sequences.add(new ArrayList<>());
        }

        for (int i = 0; i < sequence.size(); ++i) {
            sequences.get(i % stride).add(sequence.get(i));
        }

        return sequences;
    }

    public static void addToMapWithStride(List<Integer> sequence, int k) {
        List<List<Integer>> sequences = convertToSequencesWithStride(sequence, 5);
        for (List<Integer> stridedSequence : sequences) {
            addToMap(stridedSequence, k);
        }
    }

    public static void bulkAddToMapWithStride(Map<Integer, List<Integer>> data, int k) {
        for (List<Integer> list : data.values()) {
            Shingles.addToMapWithStride(list, k);
        }
    }


    public static Map<Integer, int[]> createMultisetsOfShingles(Map<Integer, List<Integer>> data, int shingleSize) {
        Map<Integer, int[]> multisetsOfShingles = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : data.entrySet()) {
            int[] multiset = Shingles.createMultisetOfShingles(entry.getValue(), shingleSize);
            multisetsOfShingles.put(entry.getKey(), multiset);
        }
        return multisetsOfShingles;
    }

    public static Map<Integer, boolean[]> createSetsOfShingles(Map<Integer, List<Integer>> data, int minK, int maxK) {
        Map<Integer, boolean[]> setsOfShingles = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : data.entrySet()) {
            boolean[] set = Shingles.createSetOfShingles(entry.getValue(), minK, maxK);
            setsOfShingles.put(entry.getKey(), set);
        }
        return setsOfShingles;
    }

    public static Map<Integer, int[]> createMultisetsOfStridedShingles(Map<Integer, List<Integer>> data, int shingleSize) {
        Map<Integer, int[]> multisetsOfStridedShingles = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : data.entrySet()) {
            List<List<Integer>> sequences = convertToSequencesWithStride(entry.getValue(), 5);
            int[] multiset = Shingles.createMultisetOfStridedShingles(sequences, shingleSize);
            multisetsOfStridedShingles.put(entry.getKey(), multiset);
        }
        return multisetsOfStridedShingles;
    }

    public static Map<Integer, boolean[]> createSetsOfStridedShingles(Map<Integer, List<Integer>> data, int shingleSize) {
        Map<Integer, boolean[]> setsOfStridedShingles = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : data.entrySet()) {
            List<List<Integer>> sequences = convertToSequencesWithStride(entry.getValue(), 5);
            boolean[] set = Shingles.createSetOfStridedShingles(sequences, shingleSize);
            setsOfStridedShingles.put(entry.getKey(), set);
        }
        return setsOfStridedShingles;
    }

    /**
     * Creates multiset of shingles from the input list.
     * @param list input
     * @param shingleSize how big "sliding window" should be considered
     * @return array of int where
     * (output[i] = k) means the shingle with index i appeared in the input list k times
     */
    public static int[] createMultisetOfShingles(List<Integer> list, int shingleSize) {
        int[] shingles = new int[shingleIDs.size()];

        for (int i = 0; i < list.size() - shingleSize + 1; ++i) {
            int shingleIndex = shingleIDs.get(new Shingle(list.subList(i, i+shingleSize)));
            ++shingles[shingleIndex];
        }
        return shingles;
    }

    public static int[] createMultisetOfStridedShingles(List<List<Integer>> sequences, int shingleSize) {
        int[] shingles = new int[shingleIDs.size()];

        for (List<Integer> sequence : sequences) {
            for (int i = 0; i < sequence.size() - shingleSize + 1; ++i) {
                int shingleIndex = shingleIDs.get(new Shingle(sequence.subList(i, i+shingleSize)));
                ++shingles[shingleIndex];
            }
        }


        return shingles;
    }

    /**
     * Creates set of shingles from the input list.
     * @param list input
     * @param minK minimum size of "sliding window"
     * @param maxK maximum size of "sliding window"
     * @return array of booleans where
     * (output[i] = true) means the shingle with index i appeared somewhere in the input
     */
    public static boolean[] createSetOfShingles(List<Integer> list, int minK, int maxK) {
        boolean[] shingles = new boolean[shingleIDs.size()];

        for (int K = minK; K <= maxK; ++K) {
            for (int i = 0; i < list.size() - K + 1; ++i) {
                int shingleIndex = shingleIDs.get(new Shingle(list.subList(i, i + K)));
                shingles[shingleIndex] = true;
            }
        }
        return shingles;
    }

    public static boolean[] createSetOfStridedShingles(List<List<Integer>> sequences, int shingleSize) {
        boolean[] shingles = new boolean[shingleIDs.size()];

        for (List<Integer> sequence : sequences) {
            for (int i = 0; i < sequence.size() - shingleSize + 1; ++i) {
                int shingleIndex = shingleIDs.get(new Shingle(sequence.subList(i, i+shingleSize)));
                shingles[shingleIndex] = true;
            }
        }
        return shingles;
    }
}
