package cz.muni.fi.thesis;

import cz.muni.fi.thesis.similarity.JaccardSimilarity;
import org.assertj.core.data.Offset;
import org.assertj.core.data.Percentage;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;

public class JaccardTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    //JaccardSimilarity on sets
    private boolean[] buildSetFiveTrues() {
        return new boolean[]{true, true, true, true, true};
    }

    private boolean[] buildSetTTTFF() {
        return new boolean[]{true, true, true, false, false};
    }

    private boolean[] buildSetFTFTF() {
        return new boolean[]{false, true, false, true, false};
    }

    private boolean[] buildSetTFTFT() {
        return new boolean[]{true, false, true, false, true};
    }

    private boolean[] buildSmallerSet() {
        return new boolean[]{true, true, false, false};
    }

    @Test
    public void jaccardOnDifferentSetSizes() {
        boolean[] set1 = buildSetFiveTrues();
        boolean[] set2 = buildSmallerSet();

        expectedException.expect(IllegalArgumentException.class);
        JaccardSimilarity.computeJaccard(set1, set2);
    }

    @Test
    public void computeJaccardOnSameSetsTest() {
        boolean[] set1 = buildSetFiveTrues();
        boolean[] set2 = buildSetFiveTrues();
        assertThat(JaccardSimilarity.computeJaccard(set1, set2)).isCloseTo(1.0, Percentage.withPercentage(0.1));

        set1 = buildSetFTFTF();
        set2 = buildSetFTFTF();
        assertThat(JaccardSimilarity.computeJaccard(set1, set2)).isCloseTo(1.0, Percentage.withPercentage(0.1));
    }

    @Test
    public void computeJaccardOnComplementarySetsTest() {
        boolean[] set1 = buildSetFTFTF();
        boolean[] set2 = buildSetTFTFT();
        assertThat(JaccardSimilarity.computeJaccard(set1, set2)).isCloseTo(0.0, Offset.offset(0.001));
    }

    @Test
    public void computeJaccardOnProperSubsetTest() {
        boolean[] set1 = buildSetFiveTrues();
        boolean[] set2 = buildSetTFTFT();
        assertThat(JaccardSimilarity.computeJaccard(set1, set2)).isCloseTo(0.6, Offset.offset(0.001));
    }

    @Test
    public void computeJaccardOnIntersectingSetsTest() {
        boolean[] set1 = buildSetTTTFF();
        boolean[] set2 = buildSetTFTFT();
        assertThat(JaccardSimilarity.computeJaccard(set1, set2)).isCloseTo(0.5, Offset.offset(0.001));
    }

    //JaccardSimilarity on minhashes
    private int[] buildMinhash11111() {
        return new int[]{1,1,1,1,1};
    }

    private int[] buildMinhash01210() {
        return new int[]{0,1,2,1,0};
    }

    private int[] buildMinhash22222() {
        return new int[]{2,2,2,2,2};
    }

    private int[] buildSmallerMinhash() {
        return new int[]{1,2,3};
    }

    @Test
    public void computeJaccardOnDifferentSizedMinhashesTest() {
        int[] minhash1 = buildMinhash11111();
        int[] minhash2 = buildSmallerMinhash();

        expectedException.expect(IllegalArgumentException.class);
        JaccardSimilarity.computeJaccardOnMinhashes(minhash1, minhash2);
    }

    @Test
    public void computeJaccardOnEqualMinhashesTest() {
        int[] minhash1 = buildMinhash11111();
        int[] minhash2 = buildMinhash11111();
        assertThat(JaccardSimilarity.computeJaccardOnMinhashes(minhash1, minhash2)).isCloseTo(1.0, Offset.offset(0.001));
    }

    @Test
    public void computeJaccardOnSimilarMinhashesTest() {
        int[] minhash1 = buildMinhash11111();
        int[] minhash2 = buildMinhash01210();
        assertThat(JaccardSimilarity.computeJaccardOnMinhashes(minhash1, minhash2)).isCloseTo(0.4, Offset.offset(0.001));
    }

    @Test
    public void computeJaccardOnNonSimilarMinhashesTest() {
        int[] minhash1 = buildMinhash11111();
        int[] minhash2 = buildMinhash22222();
        assertThat(JaccardSimilarity.computeJaccardOnMinhashes(minhash1, minhash2)).isCloseTo(0.0, Offset.offset(0.001));
    }

    //JaccardSimilarity on multisets
    private int[] buildMultiset01234() {
        return new int[]{0,1,2,3,4};
    }

    private int[] buildMultiset43210() {
        return new int[]{4,3,2,1,0};
    }

    private int[] buildMultiset50000() {
        return new int[]{5,0,0,0,0};
    }

    private int[] buildSmallerMultiset() {
        return new int[]{1,2,3};
    }

    @Test
    public void computeJaccardOnMutlisetsOfDifferentSizesTest() {
        int[] multiset1 = buildMultiset50000();
        int[] multiset2 = buildSmallerMultiset();

        expectedException.expect(IllegalArgumentException.class);
        JaccardSimilarity.computeJaccardOnMultisets(multiset1, multiset2);
    }

    @Test
    public void computeJaccardOnTheSameMultisetsTest() {
        int[] multiset1 = buildMultiset01234();
        int[] multiset2 = buildMultiset01234();
        assertThat(JaccardSimilarity.computeJaccardOnMultisets(multiset1, multiset2)).isCloseTo(0.5, Offset.offset(0.0001));
    }

    @Test
    public void computeJaccardOnOverlappingMultisets() {
        int[] multiset1 = buildMultiset01234();
        int[] multiset2 = buildMultiset43210();
        assertThat(JaccardSimilarity.computeJaccardOnMultisets(multiset1, multiset2)).isCloseTo(0.2, Offset.offset(0.0001));
    }

    @Test
    public void computeJaccardOnNonOverlappingMultisets() {
        int[] multiset1 = buildMultiset01234();
        int[] multiset2 = buildMultiset50000();
        assertThat(JaccardSimilarity.computeJaccardOnMultisets(multiset1, multiset2)).isCloseTo(0.0, Offset.offset(0.0001));
    }
}
