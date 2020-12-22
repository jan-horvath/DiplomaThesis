package cz.muni.fi.thesis;

import com.google.common.collect.BiMap;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import static org.assertj.core.api.Assertions.*;

public class SequenceTest {

    public List<Sequence> sequences;
    public BiMap<Shingle, Integer> shingleIds;

    @Before
    public void setUp() {
        Map<Integer, List<Integer>> motionWords = new HashMap<>();
        motionWords.put(1, Arrays.asList(5,5,5,6,6,6));
        motionWords.put(2, Arrays.asList(7,7,6,6,5,5));
        motionWords.put(3, Arrays.asList(5,5,5,5,5));

        /*Map<Integer, List<Integer>> groundTruth = new HashMap<>();
        groundTruth.put(1, Collections.singletonList(99));
        groundTruth.put(2, Collections.singletonList(99));
        groundTruth.put(3, Collections.singletonList(99));*/

        Map<Integer, String> scenarios = new HashMap<>();
        scenarios.put(1, "01-01");
        scenarios.put(2, "01-01");
        scenarios.put(3, "01-01");

        Sequence.setUp(motionWords, 1, 1, 99, 99);
        sequences = SequenceUtility.createSequences(motionWords, scenarios);
        shingleIds = Sequence.getShingleIds();
    }

    @Test
    public void TFIDF_TFIDF_Test() {
        Sequence seq1 = null, seq2 = null, seq3 = null;
        for (int i = 0; i < 3; ++i) {
            Sequence sequence = sequences.get(i);
            if (sequence.getId() == 1) {seq1 = sequence;}
            if (sequence.getId() == 2) {seq2 = sequence;}
            if (sequence.getId() == 3) {seq3 = sequence;}
        }

        int index5 = shingleIds.get(new Shingle(Collections.singletonList(5)));
        int index6 = shingleIds.get(new Shingle(Collections.singletonList(6)));
        int index7 = shingleIds.get(new Shingle(Collections.singletonList(7)));

        double[] weights1 = seq1.toTfIdfWeights(false);
        assertThat(weights1[index5]).isCloseTo(0.0, Offset.offset(0.001));
        assertThat(weights1[index6]).isCloseTo(3*Math.log(1.5), Offset.offset(0.001));
        assertThat(weights1[index7]).isCloseTo(0.0, Offset.offset(0.001));

        double[] weights2 = seq2.toTfIdfWeights(false);
        assertThat(weights2[index5]).isCloseTo(0.0, Offset.offset(0.001));
        assertThat(weights2[index6]).isCloseTo(2*Math.log(1.5), Offset.offset(0.001));
        assertThat(weights2[index7]).isCloseTo(2*Math.log(3.0), Offset.offset(0.001));

        double[] weights3 = seq3.toTfIdfWeights(false);
        assertThat(weights3[index5]).isCloseTo(0.0, Offset.offset(0.001));
        assertThat(weights3[index6]).isCloseTo(0.0, Offset.offset(0.001));
        assertThat(weights3[index7]).isCloseTo(0.0, Offset.offset(0.001));
    }
}
