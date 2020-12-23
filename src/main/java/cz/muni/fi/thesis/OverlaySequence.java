package cz.muni.fi.thesis;

import java.util.*;

public class OverlaySequence {

    /*TODO
    ADD ID
    ADD List<MOMW5> and keep the Map
     */

    private Map<MOMW5, Double> MOMWs = new HashMap<>();

    public Map<MOMW5, Double> getMOMWs() {
        return MOMWs;
    }

    public OverlaySequence(List<int[]> motionWords, List<Double> weights) {
        assert(motionWords.size() == weights.size());
        for (int i = 0; i < motionWords.size(); ++i) {
            MOMWs.put(new MOMW5(motionWords.get(i)), weights.get(i));
        }
    }

    public class MOMW5 {
        public final int[] motionWord;

        MOMW5(int[] motionWord) {
            this.motionWord = motionWord;
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
