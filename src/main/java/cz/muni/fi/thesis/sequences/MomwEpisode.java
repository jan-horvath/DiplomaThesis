package cz.muni.fi.thesis.sequences;

import java.util.*;

public class MomwEpisode {

    private int id;
    private List<MOMW> sequence;
    private String scenario;

    public MomwEpisode(int id, String scenario, List<int[]> sequence, List<Double> weights) {
        assert(sequence.size() == weights.size());
        this.id = id;
        this.scenario = scenario;
        this.sequence = new ArrayList<>();
        for (int i = 0; i < sequence.size(); ++i) {
            this.sequence.add(new MOMW(sequence.get(i), weights.get(i)));
        }
    }

    public int getId() {
        return id;
    }

    public List<MOMW> getSequence() {
        return sequence;
    }

    public String getScenario() {
        return scenario;
    }

    public class MOMW {
        private int[] motionWord;
        double weight;

        MOMW(int[] motionWord, double weight) {
            this.motionWord = motionWord;
            this.weight = weight;
        }

        public int[] getMW() {
            return motionWord;
        }

        public double getWeight() {
            return weight;
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
