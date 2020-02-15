package cz.muni.fi.thesis.shingling;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class StridedShinglesTest {

    private Map<Integer, List<Integer>> data;

    private boolean areClose(double x, double y) {
        return Math.abs(x - y) < 0.001;
    }

    private boolean containsJaccardEntry(List<SimilarityMatrix.SimilarityEntry> list, int recordId, double jaccardValue){
        return list.stream().anyMatch(o -> (o.recordID == recordId) && (areClose(jaccardValue, o.jaccardValue)));
    }

    @Before
    public void dataSetUp() {
        data = new HashMap<>();
        //Jac 1:2 -> 3/15 = 20%
        //Jac 1:3 ->
        data.put(1, Arrays.asList( 5,10,15,20,25, 6,11,16,21,26, 7,12,17,22,27));
        data.put(2, Arrays.asList( 5,15,25,35,45, 6,16,26,36,46, 0, 0, 0));
        data.put(3, Arrays.asList(25,16,16,99,98,26, 0, 0));

        ShingleUtility.bulkAddToMapWithStride(data, 2);
    }

    @Test
    public void jaccardOnSetsTest() {
        Map<Integer, boolean[]> setsOfStridedShingles = ShingleUtility.createSetsOfStridedShingles(data, 2);
        SimilarityMatrix similarityMatrix = SimilarityMatrix.createMatrixFromSets(setsOfStridedShingles, false);
        Map<Integer, List<SimilarityMatrix.SimilarityEntry>> matrix = similarityMatrix.getMatrix();

        assertThat(containsJaccardEntry(matrix.get(1), 1, 1.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 2, 1.0)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 3, 1.0)).isTrue();

        assertThat(containsJaccardEntry(matrix.get(1), 2, 0.2)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 3, 0.25)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 3, 1.0/11)).isTrue();
    }

    @Test
    public void jaccardOnMultisetsTest() {
        Map<Integer, int[]> multisetsOfStridedShingles = ShingleUtility.createMultisetsOfStridedShingles(data, 2);
        SimilarityMatrix similarityMatrix = SimilarityMatrix.createMatrixFromMultisets(multisetsOfStridedShingles);
        Map<Integer, List<SimilarityMatrix.SimilarityEntry>> matrix = similarityMatrix.getMatrix();

        assertThat(containsJaccardEntry(matrix.get(1), 1, 0.5)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 2, 0.5)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(3), 3, 0.5)).isTrue();

        assertThat(containsJaccardEntry(matrix.get(1), 2, 1.0/6)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(2), 3, 2.0/11)).isTrue();
        assertThat(containsJaccardEntry(matrix.get(1), 3, 1.0/13)).isTrue();
    }
}
