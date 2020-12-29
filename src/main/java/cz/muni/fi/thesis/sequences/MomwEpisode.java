package cz.muni.fi.thesis.sequences;

import java.util.*;

public class MomwEpisode {

    private int id;
    private List<MOMW> sequence;
    private Set<MOMW> set;
    private String scenario;

    public MomwEpisode(int id, String scenario, List<int[]> sequence, List<Double> weights) {
        assert(sequence.size() == weights.size());
        this.id = id;
        this.scenario = scenario;
        this.sequence = new ArrayList<>();
        this.set = new HashSet<>();
        for (int i = 0; i < sequence.size(); ++i) {
            MOMW momw = new MOMW(sequence.get(i), weights.get(i));
            this.sequence.add(momw);
            this.set.add(momw);
        }
    }

    public int getId() {
        return id;
    }

    public List<MOMW> getSequence() {
        return sequence;
    }

    public Set<MOMW> getSet() {
        return set;
    }

    public String getScenario() {
        return scenario;
    }

    public class MOMW {
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
