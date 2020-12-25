package cz.muni.fi.thesis.sequences;

import java.util.*;

public class OverlaySequence {

    private int id;
    private List<MOMW5> motionWords;
    private String scenario;

    public OverlaySequence(int id, String scenario, List<int[]> motionWords, List<Double> weights) {
        assert(motionWords.size() == weights.size());
        this.id = id;
        this.scenario = scenario;
        this.motionWords = new ArrayList<>();
        for (int i = 0; i < motionWords.size(); ++i) {
            this.motionWords.add(new MOMW5(motionWords.get(i), weights.get(i)));
        }
    }

    public int getId() {
        return id;
    }

    public List<MOMW5> getMotionWords() {
        return motionWords;
    }

    public String getScenario() {
        return scenario;
    }

    public class MOMW5 {
        private int[] motionWord;
        double weight;

        MOMW5(int[] motionWord, double weight) {
            this.motionWord = motionWord;
            this.weight = weight;
        }

        public int[] getMotionWord() {
            return motionWord;
        }

        public double getWeight() {
            return weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MOMW5 momw5 = (MOMW5) o;
            return Arrays.equals(motionWord, momw5.motionWord);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(motionWord);
        }
    }
}
