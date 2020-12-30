package cz.muni.fi.thesis.episode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.*;

/**
 * Contains information about an episode of hard motion words as well as its shingle representation, such as episode
 * (sequence) ID, scenario, list of HMWs and the term frequency of shingles.
 * Also contains static information about the dataset such as IDF for each existing shingle, bidirectional map of
 * shingle IDs and shingles.
 */
public class HmwEpisode {
    private static int minShingleK, maxShingleK;
    private static BiMap<Shingle, Integer> shingleIds;
    private static Map<Integer, Double> idf;
    private static int nextId = 0;
    private static int nextShingleID() {return nextId++;}

    public static Map<Integer, Double> getIdf() {
        return idf;
    }

    private final int id;
    private final String scenario;
    private final List<Integer> hmwSequence;
    private Map<Integer, Integer> term_frequency;

    public int getId() {
        return id;
    }
    public String getScenario() {
        return scenario;
    }

    private boolean[] set;
    private double[] bag;
    private double[] tfidf;


    //--------------------------------------------Static functions------------------------------------------------------

    /**
     * Sets up the static part of the class by precomputing IDF and ID for each existing shingle in the {@code data} of
     * size between {@code minK} and {@code maxK} including.
     * @param data dataset of HMW episodes
     */
    public static void setUp(Map<Integer, List<Integer>> data, int minK, int maxK) {
        HmwEpisode.minShingleK = minK;
        HmwEpisode.maxShingleK = maxK;
        nextId = 0;
        createShingleIds(data, minK, maxK);
        computeIdf(data, minK, maxK);
    }

    public static void setUp(Map<Integer, List<Integer>> data) {
        setUp(data, 1, 1);
    }

    private static void createShingleIds(Map<Integer, List<Integer>> data, int minK, int maxK) {
        shingleIds = HashBiMap.create();
        for (List<Integer> episode : data.values()) {
            for (int K = minK; K <= maxK; ++K) {
                for (int i = 0; i < episode.size() - K + 1; ++i) {
                    Shingle newShingle = new Shingle(episode.subList(i, i + K));
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
            List<Integer> episode = entry.getValue();
            Set<Shingle> shinglesFound = getShinglesFromEpisode(episode, minK, maxK);
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

    private static Set<Shingle> getShinglesFromEpisode(List<Integer> episode, int minK, int maxK) {
        Set<Shingle> shinglesFound = new HashSet<>();
        for (int K = minK; K <= maxK; ++K) {
            for (int i = 0; i < episode.size() - K + 1; ++i) {
                Shingle shingle = new Shingle(episode.subList(i, i + K));
                shinglesFound.add(shingle);
            }
        }
        return shinglesFound;
    }

    //------------------------------------------Non-static functions----------------------------------------------------

    HmwEpisode(int id, String scenario, List<Integer> motionWords) {
        this.id = id;
        this.scenario = scenario;
        this.hmwSequence = motionWords;
        computeTf(motionWords);
    }

    public List<Integer> getHmwSequence() {
        return hmwSequence;
    }

    private void computeTf(List<Integer> motionWords) {
        term_frequency = new HashMap<>();
        for (int id : shingleIds.values()) {
            term_frequency.put(id, 0);
        }

        for (int K = minShingleK; K <= maxShingleK; ++K) {
            for (int i = 0; i < motionWords.size() - K + 1; ++i) {
                Integer shingleIndex = shingleIds.get(new Shingle(motionWords.subList(i, i + K)));
                term_frequency.put(shingleIndex, term_frequency.get(shingleIndex) + 1);
            }
        }
    }

    /**
     * Converts the episode to a set
     */
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

    /**
     * Converts the episode to a bag (multiset) which is the same as a vector of TF weights
     */
    public double[] toBag() {
       if (bag == null) {
            bag = new double[term_frequency.size()];
            for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                bag[entry.getKey()] = entry.getValue();
            }
        }
        return bag;
    }

    /**
     * Converts the episode to a vector of IDF weights
     */
    public double[] toIdfWeights() {
        double[] weights = new double[term_frequency.size()];
        for (Map.Entry<Integer, Double> entry : idf.entrySet()) {
            if (term_frequency.get(entry.getKey()) > 0) {
                weights[entry.getKey()] = entry.getValue();
            }
        }
        return weights;
    }

    /**
     * Converts the episode to a vector of TFIDF weights
     */
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
