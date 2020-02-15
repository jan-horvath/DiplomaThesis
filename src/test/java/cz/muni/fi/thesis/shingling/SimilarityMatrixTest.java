package cz.muni.fi.thesis.shingling;

import org.junit.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class SimilarityMatrixTest {

    private boolean areClose(double x, double y) {
        return Math.abs(x - y) < 0.001;
    }

    private boolean containsJaccardEntry(List<SimilarityMatrix.SimilarityEntry> list, int recordId, double jaccardValue){
        return list.stream().anyMatch(o -> (o.recordID == recordId) && (areClose(jaccardValue, o.jaccardValue)));
    }

    @Test
    public void matrixFromSets() {
        Map<Integer, boolean[]> sets = new HashMap<>();
        sets.put(1, new boolean[]{true, true, true, false, false});
        sets.put(2, new boolean[]{false, false, true, true, false});
        sets.put(3, new boolean[]{false, false, false, true, true});

        Map<Integer, List<SimilarityMatrix.SimilarityEntry>> matrix = SimilarityMatrix.createMatrixFromSets(sets, false).getMatrix();

        for (int i = 1; i <= 3; ++i) {
            assertThat(matrix.get(i).size()).isEqualTo(3);
        }

        assertThat(containsJaccardEntry(matrix.get(1), 1, 1.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 2, 0.25)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 3, 0.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 1, 0.25)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 2, 1.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 3, 0.33333)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 1, 0.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 2, 0.3333)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 3, 1.0)).isTrue();

    }

    @Test
    public void matrixFromMultisets() {
        Map<Integer, int[]> multisets = new HashMap<>();
        multisets.put(1, new int[]{10,5,5,0,0});
        multisets.put(2, new int[]{0,5,5,10,0});
        multisets.put(3, new int[]{0,0,0,2,3});

        Map<Integer, List<SimilarityMatrix.SimilarityEntry>> matrix = SimilarityMatrix.createMatrixFromMultisets(multisets).getMatrix();

        for (int i = 1; i <= 3; ++i) {
            assertThat(matrix.get(i).size()).isEqualTo(3);
        }

        assertThat(containsJaccardEntry(matrix.get(1), 1, 0.5)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 2, 0.25)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 3, 0.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 1, 0.25)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 2, 0.5)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 3, 0.08)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 1, 0.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 2, 0.08)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 3, 0.5)).isTrue();
    }

    @Test
    public void matrixFromMinhashes() {
        Map<Integer, int[]> minhashes = new HashMap<>();
        minhashes.put(1, new int[]{2,2,2,3,3});
        minhashes.put(2, new int[]{0,1,2,3,4});
        minhashes.put(3, new int[]{0,0,0,0,5});

        Map<Integer, List<SimilarityMatrix.SimilarityEntry>> matrix = SimilarityMatrix.createMatrixFromMinhashes(minhashes).getMatrix();

        for (int i = 1; i <= 3; ++i) {
            assertThat(matrix.get(i).size()).isEqualTo(3);
        }

        assertThat(containsJaccardEntry(matrix.get(1), 1, 1.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 2, 0.4)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 3, 0.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 1, 0.4)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 2, 1.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 3, 0.2)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 1, 0.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 2, 0.2)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 3, 1.0)).isTrue();
    }
}
