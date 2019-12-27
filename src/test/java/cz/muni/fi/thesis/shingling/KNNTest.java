package cz.muni.fi.thesis.shingling;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class KNNTest {

    private Map<Integer, int[]> KNN_2;
    private Map<Integer, int[]> KNN_3;

    @Before
    public void jaccardMatrixSetup() {
        Map<Integer, boolean[]> sets = new HashMap<>();
        sets.put( 0, new boolean[]{true, true, true, true, false, false, false, false});
        sets.put(10, new boolean[]{false, false, true, true, true, true, false, false});
        sets.put(20, new boolean[]{false, false, false, false, true, true, true, true});
        sets.put(30, new boolean[]{true, true, false, false, false, false, true, true});
        sets.put(40, new boolean[]{true, false, true, true, true, true, true, true});
        sets.put(50, new boolean[]{false, false, true, true, false, false, false, false});

        JaccardMatrix jaccardMatrix = JaccardMatrix.createMatrixFromSets(sets);
        KNN_2 = KNN.bulkExtractKNNIndices(jaccardMatrix, 2);
        KNN_3 = KNN.bulkExtractKNNIndices(jaccardMatrix, 3);
    }

    @Test
    public void twoMostSimilarSetsTest() {
        for (Map.Entry<Integer, int[]> entry : KNN_2.entrySet()) {
            assertThat(entry.getValue().length).isEqualTo(2);
        }

        //0
        assertThat(KNN_2.get(0)[0]).isEqualTo(50);
        assertThat(KNN_2.get(0)[1]).isEqualTo(40);

        //10
        assertThat(KNN_2.get(10)[0]).isEqualTo(40);
        assertThat(KNN_2.get(10)[1]).isEqualTo(50);

        //20
        assertThat(KNN_2.get(20)[0]).isEqualTo(40);
        assertThat((KNN_2.get(20)[1] == 10) || (KNN_2.get(20)[1] == 30)).isTrue();

        //30
        assertThat(KNN_2.get(30)[0]).isEqualTo(40);
        assertThat((KNN_2.get(30)[1] == 0) || (KNN_2.get(30)[1] == 20)).isTrue();

        //40
        int[] twoNearest_40 = KNN_2.get(40);
        assertThat(twoNearest_40[0]).isNotEqualTo(twoNearest_40[1]);
        assertThat((twoNearest_40[0] == 10) || (twoNearest_40[0] == 20)).isTrue();
        assertThat((twoNearest_40[1] == 10) || (twoNearest_40[1] == 20)).isTrue();

        //50
        int[] twoNearest_50 = KNN_2.get(50);
        assertThat(twoNearest_50[0]).isNotEqualTo(twoNearest_50[1]);
        assertThat((twoNearest_50[0] == 10) || (twoNearest_50[0] == 0)).isTrue();
        assertThat((twoNearest_50[1] == 10) || (twoNearest_50[1] == 0)).isTrue();
    }

    @Test
    public void threeMostSimilarSetsTest() {
        for (Map.Entry<Integer, int[]> entry : KNN_3.entrySet()) {
            assertThat(entry.getValue().length).isEqualTo(3);
        }

        //0
        assertThat(KNN_3.get(0)[0]).isEqualTo(50);
        assertThat(KNN_3.get(0)[1]).isEqualTo(40);
        assertThat((KNN_3.get(0)[2] == 10) || (KNN_3.get(0)[2] == 30)).isTrue();

        //10
        assertThat(KNN_3.get(10)[0]).isEqualTo(40);
        assertThat(KNN_3.get(10)[1]).isEqualTo(50);
        assertThat((KNN_3.get(10)[2] == 0) || (KNN_3.get(10)[2] == 20)).isTrue();

        //20
        assertThat(KNN_3.get(20)[0]).isEqualTo(40);
        assertThat(KNN_3.get(20)[1]).isNotEqualTo(KNN_3.get(20)[2]);
        assertThat((KNN_3.get(20)[1] == 10) || (KNN_3.get(20)[1] == 30)).isTrue();
        assertThat((KNN_3.get(20)[2] == 10) || (KNN_3.get(20)[2] == 30)).isTrue();

        //30
        assertThat(KNN_3.get(30)[0]).isEqualTo(40);
        assertThat(KNN_3.get(30)[1]).isNotEqualTo(KNN_3.get(30)[2]);
        assertThat((KNN_3.get(30)[1] == 0) || (KNN_3.get(30)[1] == 20)).isTrue();
        assertThat((KNN_3.get(30)[2] == 0) || (KNN_3.get(30)[2] == 20)).isTrue();

        //40
        assertThat(KNN_3.get(40)[0]).isNotEqualTo(KNN_3.get(40)[1]);
        assertThat((KNN_3.get(40)[0] == 10) || (KNN_3.get(40)[0] == 20)).isTrue();
        assertThat((KNN_3.get(40)[1] == 10) || (KNN_3.get(40)[1] == 20)).isTrue();
        assertThat((KNN_3.get(40)[2] == 30) || (KNN_3.get(40)[2] == 0)).isTrue();

        //50
        assertThat(KNN_3.get(50)[0]).isNotEqualTo(KNN_3.get(50)[1]);
        assertThat((KNN_3.get(50)[0] == 10) || (KNN_3.get(50)[0] == 0)).isTrue();
        assertThat((KNN_3.get(50)[1] == 10) || (KNN_3.get(50)[1] == 0)).isTrue();
        assertThat(KNN_3.get(50)[2]).isEqualTo(40);
    }

}
