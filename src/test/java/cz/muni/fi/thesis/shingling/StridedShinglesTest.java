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

    private boolean containsJaccardEntry(List<JaccardMatrix.JaccardEntry> list, int recordId, double jaccardValue){
        return list.stream().anyMatch(o -> (o.recordID == recordId) && (areClose(jaccardValue, o.jaccardValue)));
    }

    @Before
    public void dataSetUp() {
        data = new HashMap<>();
        //Jac 1:2 -> 3/15 = 20%
        //Jac 1:3 ->
        data.put(1, Arrays.asList(5,10,15,20,25,6,11,16,21,26,7,12,17,22,27));
        data.put(2, Arrays.asList(5,15,25,35,45,6,16,26,36,46,0,0,0));
        data.put(3, Arrays.asList(25, 16,16,100,101,26,0,0));

        Shingles.bulkAddToMapWithStride(data, 2);
    }

    @Test
    public void jaccardOnSetsTest() {
        Map<Integer, boolean[]> setsOfStridedShingles = Shingles.createSetsOfStridedShingles(data, 2);
        JaccardMatrix jaccardMatrix = JaccardMatrix.createMatrixFromSets(setsOfStridedShingles);
        Map<Integer, List<JaccardMatrix.JaccardEntry>> matrix = jaccardMatrix.getMatrix();

        assertThat(containsJaccardEntry(matrix.get(1), 1, 1.0));
        assertThat(containsJaccardEntry(matrix.get(2), 2, 1.0));
        assertThat(containsJaccardEntry(matrix.get(3), 3, 1.0));

        assertThat(containsJaccardEntry(matrix.get(1), 2, 0.2));
        assertThat(containsJaccardEntry(matrix.get(2), 3, 0.2));
        assertThat(containsJaccardEntry(matrix.get(1), 3, 1.0/11));
    }

    @Test
    public void jaccardOnMultisetsTest() {
        Map<Integer, int[]> multisetsOfStridedShingles = Shingles.createMultisetsOfStridedShingles(data, 2);
        JaccardMatrix jaccardMatrix = JaccardMatrix.createMatrixFromMultisets(multisetsOfStridedShingles);
        Map<Integer, List<JaccardMatrix.JaccardEntry>> matrix = jaccardMatrix.getMatrix();

        assertThat(containsJaccardEntry(matrix.get(1), 1, 0.5));
        assertThat(containsJaccardEntry(matrix.get(2), 2, 0.5));
        assertThat(containsJaccardEntry(matrix.get(3), 3, 0.5));

        assertThat(containsJaccardEntry(matrix.get(1), 2, 1.0/6));
        assertThat(containsJaccardEntry(matrix.get(2), 3, 2.0/13));
        assertThat(containsJaccardEntry(matrix.get(1), 3, 2.0/11));
    }
}
