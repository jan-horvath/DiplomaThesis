package cz.muni.fi.thesis.shingling;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.*;

public class Sequence {
    private static int minK, maxK, minAction, maxAction;
    private static BiMap<Shingle, Integer> shingleIds;
    private static Map<Integer, Double> idf;
    private static double maxIdf;
    private static int nextId = 0;
    private static int nextShingleID() {return nextId++;}

    private final int id;
    private Map<Integer, Integer> tf;

    public int getId() {
        return id;
    }

    private boolean[] groundTruthSet;
    private int[] groundTruthMultiset;

    //--------------------------------------------Static functions------------------------------------------------------

    public static void setUp(Map<Integer, List<Integer>> data, int minK, int maxK, int minAction, int maxAction) {
        Sequence.minK = minK;
        Sequence.maxK = maxK;
        Sequence.minAction = minAction;
        Sequence.maxAction = maxAction;
        createShingleIds(data, minK, maxK);
        computeIdf(data, minK, maxK);
        maxIdf = Math.log(data.size());
    }

    private static void createShingleIds(Map<Integer, List<Integer>> data, int minK, int maxK) {
        shingleIds = HashBiMap.create();
        for (List<Integer> sequence : data.values()) {
            for (int K = minK; K <= maxK; ++K) {
                for (int i = 0; i < sequence.size() - K + 1; ++i) {
                    Shingle newShingle = new Shingle(sequence.subList(i, i + K));
                    if (!shingleIds.containsKey(newShingle)) {
                        shingleIds.put(newShingle, nextShingleID());
                    }
                }
            }
        }
    }

    private static void computeIdf(Map<Integer, List<Integer>> data, int minK, int maxK) {
        idf = new HashMap<>();
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

        double size = data.size();
        for (Map.Entry<Shingle, Integer> entry : shingleOccurrences.entrySet()) {
            idf.put(shingleIds.get(entry.getKey()), Math.log(size / entry.getValue()));
        }
    }

    private static Set<Shingle> getShinglesFromSequence(List<Integer> sequence, int minK, int maxK) {
        Set<Shingle> shinglesFound = new HashSet<>();
        for (int K = minK; K <= maxK; ++K) {
            for (int i = 0; i < sequence.size() - K + 1; ++i) {
                Shingle shingle = new Shingle(sequence.subList(i, i + K));
                shinglesFound.add(shingle);
            }
        }
        return shinglesFound;
    }

    //------------------------------------------Non-static functions----------------------------------------------------

    public Sequence(int id, List<Integer> groundTruth, List<Integer> motionWords) {
        this.id = id;
        computeGroundTruth(groundTruth);
        computeTf(motionWords);
    }

    private void computeGroundTruth(List<Integer> groundTruth) {
        groundTruthSet = new boolean[maxAction - minAction + 1];
        groundTruthMultiset = new int[maxAction - minAction + 1];
        for (Integer motionWord : groundTruth) {
            int index = motionWord - minAction;
            groundTruthSet[index] = true;
            ++groundTruthMultiset[index];
        }
    }

    private void computeTf(List<Integer> motionWords) {
        tf = new HashMap<>();
        for (int id : shingleIds.values()) {
            tf.put(id, 0);
        }

        for (int K = minK; K <= maxK; ++K) {
            for (int i = 0; i < motionWords.size() - K + 1; ++i) {
                Integer shingleIndex = shingleIds.get(new Shingle(motionWords.subList(i, i + K)));
                assert(shingleIndex != null);
                tf.put(shingleIndex, tf.get(shingleIndex) + 1);
            }
        }
    }

    public boolean[] getGroundTruthSet() {
        return groundTruthSet;
    }

    public int[] getGroundTruthMultiset() {
        return groundTruthMultiset;
    }

    public boolean[] toSet() {
        boolean[] set = new boolean[tf.size()];
        for (Map.Entry<Integer, Integer> entry : tf.entrySet()) {
            if (entry.getValue() > 0) {set[entry.getKey()] = true;}
        }
        return set;
    }

    public int[] toMultiset() {
        int[] multiset = new int[tf.size()];
        for (Map.Entry<Integer, Integer> entry : tf.entrySet()) {
            multiset[entry.getKey()] = entry.getValue();
        }
        return multiset;
    }

    public double[] toTfWeights() {
        double[] weights = new double[tf.size()];
        for (Map.Entry<Integer, Integer> entry : tf.entrySet()) {
            weights[entry.getKey()] = entry.getValue();
        }
        return weights;
    }

    public double[] toIdfWeights() {
        double[] weights = new double[tf.size()];
        for (Map.Entry<Integer, Double> entry : idf.entrySet()) {
            weights[entry.getKey()] = entry.getValue();
        }
        return weights;
    }

    public double[] toTfIdfWeights() {
        double[] weights = new double[tf.size()];
        for (Map.Entry<Integer, Integer> entry : tf.entrySet()) {
            weights[entry.getKey()] = entry.getValue() * idf.get(entry.getKey());
        }
        return weights;
    }
}
