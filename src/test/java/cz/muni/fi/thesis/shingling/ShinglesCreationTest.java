package cz.muni.fi.thesis.shingling;

import cz.muni.fi.thesis.shingling.similarity.JaccardSimilarity;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import static org.assertj.core.api.Assertions.*;

public class ShinglesCreationTest {

    private Map<Integer, List<Integer>> data;

    @Before
    public void setUp() {
        data = new HashMap<>();
        data.put(1, Arrays.asList(42,42,79,79,191,191));
        data.put(2, Arrays.asList(42,42,42,191,191,191));
        data.put(3, Arrays.asList(42,79,79,79,191));
    }

    public void setUpMap(int k) {
        ShingleUtility.resetMap();
        ShingleUtility.addToMap(data.get(1), k);
        ShingleUtility.addToMap(data.get(2), k);
        ShingleUtility.addToMap(data.get(3), k);
    }

    @Test
    public void oneShingleSetsTest() {
        setUpMap(1);
        Map<Integer, boolean[]> sets = ShingleUtility.createSetsOfShingles(data, 1, 1);

        assertThat(JaccardSimilarity.computeJaccard(sets.get(1), sets.get(2))).isCloseTo(2.0/3, Offset.offset(0.001));
        assertThat(JaccardSimilarity.computeJaccard(sets.get(1), sets.get(3))).isCloseTo(1.0, Offset.offset(0.001));
        assertThat(JaccardSimilarity.computeJaccard(sets.get(2), sets.get(3))).isCloseTo(2.0/3, Offset.offset(0.001));
    }

    @Test
    public void oneShingleMultisetsTest() {
        setUpMap(1);
        Map<Integer, int[]> multisets = ShingleUtility.createMultisetsOfShingles(data,  1);

        assertThat(JaccardSimilarity.computeJaccardOnMultisets(multisets.get(1), multisets.get(2))).isCloseTo(1.0/3, Offset.offset(0.001));
        assertThat(JaccardSimilarity.computeJaccardOnMultisets(multisets.get(1), multisets.get(3))).isCloseTo(4.0/11, Offset.offset(0.001));
        assertThat(JaccardSimilarity.computeJaccardOnMultisets(multisets.get(2), multisets.get(3))).isCloseTo(2.0/11, Offset.offset(0.001));
    }

    @Test
    public void twoShingleSetsTest() {
        setUpMap(2);
        Map<Integer, boolean[]> sets = ShingleUtility.createSetsOfShingles(data,  2, 2);

        assertThat(JaccardSimilarity.computeJaccard(sets.get(1), sets.get(2))).isCloseTo(1.0/3, Offset.offset(0.001));
        assertThat(JaccardSimilarity.computeJaccard(sets.get(1), sets.get(3))).isCloseTo(3.0/5, Offset.offset(0.001));
        assertThat(JaccardSimilarity.computeJaccard(sets.get(2), sets.get(3))).isCloseTo(0.0, Offset.offset(0.001));
    }
}
