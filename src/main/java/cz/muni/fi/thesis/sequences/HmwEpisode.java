package cz.muni.fi.thesis.sequences;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import cz.muni.fi.thesis.Shingle;

import java.util.*;

public class HmwEpisode {
    private static int minK, maxK;
    private static BiMap<Shingle, Integer> shingleIds;
    private static Map<Integer, Double> idf;
    private static int nextId = 0;
    private static int nextShingleID() {return nextId++;}

    public static BiMap<Shingle, Integer> getShingleIds() {
        return shingleIds;
    }
    public static Map<Integer, Double> getIdf() {
        return idf;
    }

    private final int id;
    private final String scenario;
    private final List<Integer> sequence;
    private Map<Integer, Integer> term_frequency;

    public int getId() {
        return id;
    }
    public String getScenario() {
        return scenario;
    }

    private boolean[] set;
    private int[] bag;
    private double[] tfidf;
    private double[] tf;


    //--------------------------------------------Static functions------------------------------------------------------

    public static void setUp(Map<Integer, List<Integer>> data, int minK, int maxK) {
        HmwEpisode.minK = minK;
        HmwEpisode.maxK = maxK;
        nextId = 0;
        createShingleIds(data, minK, maxK);
        computeIdf(data, minK, maxK);
    }

    public static void setUp(Map<Integer, List<Integer>> data) {
        setUp(data, 1, 1);
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
        for (Map.Entry<Integer, List<Integer>> entry : data.entrySet()) {
            List<Integer> sequence = entry.getValue();
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

    public HmwEpisode(int id, String scenario, List<Integer> motionWords) {
        this.id = id;
        this.scenario = scenario;
        this.sequence = motionWords;
        computeTf(motionWords);
    }

    public List<Integer> getSequence() {
        return sequence;
    }

    private void computeTf(List<Integer> motionWords) {
        term_frequency = new HashMap<>();
        for (int id : shingleIds.values()) {
            term_frequency.put(id, 0);
        }

        for (int K = minK; K <= maxK; ++K) {
            for (int i = 0; i < motionWords.size() - K + 1; ++i) {
                Integer shingleIndex = shingleIds.get(new Shingle(motionWords.subList(i, i + K)));
                assert(shingleIndex != null);
                term_frequency.put(shingleIndex, term_frequency.get(shingleIndex) + 1);
            }
        }
    }

    public boolean[] toSet() {
        if (set == null) {
            set = new boolean[term_frequency.size()];
            for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                if (entry.getValue() > 0) {
                    set[entry.getKey()] = true;
                }
            }
        }
        return set;
    }

    public int[] toBag() {
       if (bag == null) {
            bag = new int[term_frequency.size()];
            for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                bag[entry.getKey()] = entry.getValue();
            }
        }
        return bag;
    }

    public double[] toTfWeights() {
        if (tf == null) {
            tf = new double[term_frequency.size()];
            for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                tf[entry.getKey()] = entry.getValue();
            }
        }
        return tf;
    }

    public double[] toIdfWeights() {
        double[] weights = new double[term_frequency.size()];
        for (Map.Entry<Integer, Double> entry : idf.entrySet()) {
            if (term_frequency.get(entry.getKey()) > 0) {
                weights[entry.getKey()] = entry.getValue();
            }
        }
        return weights;
    }

    public double[] toTfIdfWeights() {
        if (tfidf == null) {
            tfidf = new double[term_frequency.size()];
            for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                tfidf[entry.getKey()] = entry.getValue() * idf.get(entry.getKey());
            }
        }
        return tfidf;
    }
}
