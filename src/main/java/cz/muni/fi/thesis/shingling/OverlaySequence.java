package cz.muni.fi.thesis.shingling;

import java.util.List;

public class OverlaySequence {

    public final List<int[]> motionWords;
    public final List<Double> weights;

    public OverlaySequence(List<int[]> motionWords, List<Double> weights) {
        this.motionWords = motionWords;
        this.weights = weights;
    }
}
