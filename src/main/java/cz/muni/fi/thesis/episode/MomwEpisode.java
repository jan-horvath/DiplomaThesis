package cz.muni.fi.thesis.episode;

import java.util.*;

/**
 * Contains information about an episode of multi-overlay motion words, such as episode (sequence) ID, list of MOMWs,
 * set of MOMWs and the scenario
 */
public class MomwEpisode {

    private int id;
    private List<MOMW> momwSequence;
    private Set<MOMW> set;
    private String scenario;

    MomwEpisode(int id, String scenario, List<int[]> momwSequence, List<Double> weights) {
        assert(momwSequence.size() == weights.size());
        this.id = id;
        this.scenario = scenario;
        this.momwSequence = new ArrayList<>();
        this.set = new HashSet<>();
        for (int i = 0; i < momwSequence.size(); ++i) {
            MOMW momw = new MOMW(momwSequence.get(i), weights.get(i));
            this.momwSequence.add(momw);
            this.set.add(momw);
        }
    }

    public int getId() {
        return id;
    }

    public List<MOMW> getMomwSequence() {
        return momwSequence;
    }

    public Set<MOMW> getSet() {
        return set;
    }

    public String getScenario() {
        return scenario;
    }

    public static class MOMW {
        private int[] motionWord;
        double IDF;

        MOMW(int[] motionWord, double IDF) {
            this.motionWord = motionWord;
            this.IDF = IDF;
        }

        public int[] getMW() {
            return motionWord;
        }

        public double getIDF() {
            return IDF;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MOMW momw = (MOMW) o;
            return Arrays.equals(motionWord, momw.motionWord);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(motionWord);
        }
    }
}
