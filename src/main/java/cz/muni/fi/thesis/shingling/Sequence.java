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

    private boolean[] groundTruthSet;
    private int[] groundTruthMultiset;

    private boolean[] set;
    private boolean[] set_ignore;
    private int[] multiset;
    private int[] multiset_ignore;
    private double[] tfidf;
    private double[] tfidf_ignore;
    private double[] tf;
    private double[] tf_ignore;


    //--------------------------------------------Static functions------------------------------------------------------

    public static void setUp(Map<Integer, List<Integer>> data, int minK, int maxK, int minAction, int maxAction) {
        Sequence.minK = minK;
        Sequence.maxK = maxK;
        Sequence.minAction = minAction;
        Sequence.maxAction = maxAction;
        nextId = 0;
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

    public Sequence(int id, String scenario, List<Integer> groundTruth, List<Integer> motionWords) {
        this.id = id;
        this.scenario = scenario;
        this.sequence = motionWords;
        computeGroundTruth(groundTruth);
        computeTf(motionWords);
    }

    public List<Integer> getSequence() {
        return sequence;
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

    public boolean[] getGroundTruthSet() {
        return groundTruthSet;
    }

    public int[] getGroundTruthMultiset() {
        return groundTruthMultiset;
    }

    public boolean[] toSet(boolean ignoreMaxIdf) {
        if (ignoreMaxIdf) {
            if (set_ignore == null) {
                set_ignore = new boolean[term_frequency.size()];
                for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                    if (entry.getValue() > 0) {set_ignore[entry.getKey()] = true;}
                }
                ignoreMaxIdf(set_ignore);
            }
            return set_ignore;
        } else {
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
    }

    public int[] toMultiset(boolean ignoreMaxIdf) {
        if (ignoreMaxIdf) {
            if (multiset_ignore == null) {
                multiset_ignore = new int[term_frequency.size()];
                for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                    multiset_ignore[entry.getKey()] = entry.getValue();
                }
                ignoreMaxIdf(multiset_ignore);
            }
            return multiset_ignore;
        } else {
            if (multiset == null) {
                multiset = new int[term_frequency.size()];
                for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                    multiset[entry.getKey()] = entry.getValue();
                }
            }
            return multiset;
        }
    }

    public double[] toTfWeights(boolean ignoreMaxIdf) {
        if (ignoreMaxIdf) {
            if (tf_ignore == null) {
                tf_ignore = new double[term_frequency.size()];
                for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                    tf_ignore[entry.getKey()] = entry.getValue();
                }
                ignoreMaxIdf(tf_ignore);
            }
            return tf_ignore;
        } else {
            if (tf == null) {
                tf = new double[term_frequency.size()];
                for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                    tf[entry.getKey()] = entry.getValue();
                }
            }
            return tf;
        }
    }

    public double[] toIdfWeights(boolean ignoreMaxIdf) {
        double[] weights = new double[term_frequency.size()];
        for (Map.Entry<Integer, Double> entry : idf.entrySet()) {
            if (term_frequency.get(entry.getKey()) > 0) {
                weights[entry.getKey()] = entry.getValue();
            }
        }
        return ignoreMaxIdf ? ignoreMaxIdf(weights) : weights;
    }

    public double[] toTfIdfWeights(boolean ignoreMaxIdf) {
        if (ignoreMaxIdf) {
            if (tfidf_ignore == null) {
                tfidf_ignore = new double[term_frequency.size()];
                for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                    tfidf_ignore[entry.getKey()] = entry.getValue() * idf.get(entry.getKey());
                }
                ignoreMaxIdf(tfidf_ignore);
            }
            return tfidf_ignore;
        } else {
            if (tfidf == null) {
                tfidf = new double[term_frequency.size()];
                for (Map.Entry<Integer, Integer> entry : term_frequency.entrySet()) {
                    tfidf[entry.getKey()] = entry.getValue() * idf.get(entry.getKey());
                }
            }
            return tfidf;
        }
    }

    //------------------------------------------------Ignore max IDF----------------------------------------------------

    private boolean[] ignoreMaxIdf(boolean[] set) {
        for (Map.Entry<Integer, Double> entry : idf.entrySet()) {
            if (Math.abs(entry.getValue() - maxIdf) < 0.001) {
                set[entry.getKey()] = false;
            }
        }
        return set;
    }

    private int[] ignoreMaxIdf(int[] multiset) {
        for (Map.Entry<Integer, Double> entry : idf.entrySet()) {
            if (Math.abs(entry.getValue() - maxIdf) < 0.001) {
                multiset[entry.getKey()] = 0;
            }
        }
        return multiset;
    }

    private double[] ignoreMaxIdf(double[] weights) {
        for (Map.Entry<Integer, Double> entry : idf.entrySet()) {
            if (Math.abs(entry.getValue() - maxIdf) < 0.001) {
                weights[entry.getKey()] = 0.0;
            }
        }
        return weights;
    }
}
