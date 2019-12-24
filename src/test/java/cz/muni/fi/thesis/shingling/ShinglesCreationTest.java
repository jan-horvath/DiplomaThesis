package cz.muni.fi.thesis.shingling;

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

    @Test
    public void oneShingleSetsTest() {
        Map<Integer, boolean[]> sets = Shingles.createSetsOfShingles(data, 42, 191, 1);

        assertThat(Jaccard.computeJaccard(sets.get(1), sets.get(2))).isCloseTo(2.0/3, Offset.offset(0.001));
        assertThat(Jaccard.computeJaccard(sets.get(1), sets.get(3))).isCloseTo(1.0, Offset.offset(0.001));
        assertThat(Jaccard.computeJaccard(sets.get(2), sets.get(3))).isCloseTo(2.0/3, Offset.offset(0.001));
    }

    @Test
    public void oneShingleMultisetsTest() {
        Map<Integer, int[]> multisets = Shingles.createMultisetsOfShingles(data, 42, 191, 1);

        assertThat(Jaccard.computeJaccardOnMultisets(multisets.get(1), multisets.get(2))).isCloseTo(1.0/3, Offset.offset(0.001));
        assertThat(Jaccard.computeJaccardOnMultisets(multisets.get(1), multisets.get(3))).isCloseTo(4.0/11, Offset.offset(0.001));
        assertThat(Jaccard.computeJaccardOnMultisets(multisets.get(2), multisets.get(3))).isCloseTo(2.0/11, Offset.offset(0.001));
    }

    @Test
    public void twoShingleSetsTest() {
        Map<Integer, boolean[]> sets = Shingles.createSetsOfShingles(data, 42, 191, 2);

        assertThat(Jaccard.computeJaccard(sets.get(1), sets.get(2))).isCloseTo(1.0/3, Offset.offset(0.001));
        assertThat(Jaccard.computeJaccard(sets.get(1), sets.get(3))).isCloseTo(3.0/5, Offset.offset(0.001));
        assertThat(Jaccard.computeJaccard(sets.get(2), sets.get(3))).isCloseTo(0.0, Offset.offset(0.001));
    }
}
