package cz.muni.fi.thesis.shingling;

import cz.muni.fi.thesis.shingling.evaluation.KNN;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class VariableKNNTest {

    private SimilarityMatrix nonPartiallyOverlappingMatrix;
    private SimilarityMatrix partiallyOverlappingMatrix;

    @Before
    public void jaccardMatrixSetup() {
        Map<Integer, boolean[]> nonPartiallyOverlappingSets = new HashMap<>();

        nonPartiallyOverlappingSets.put(0, new boolean[]{true, true, true, true, false, false, false, false});
        nonPartiallyOverlappingSets.put(1, new boolean[]{false, false, false, false, true, true, false, false});
        nonPartiallyOverlappingSets.put(2, new boolean[]{false, false, false, false, true, true, false, false});
        nonPartiallyOverlappingSets.put(3, new boolean[]{false, false, false, false, false, false, false, true});
        nonPartiallyOverlappingSets.put(4, new boolean[]{false, false, false, false, false, false, false, true});
        nonPartiallyOverlappingSets.put(5, new boolean[]{false, false, false, false, false, false, false, true});

        nonPartiallyOverlappingMatrix = SimilarityMatrix.createMatrixFromSets(nonPartiallyOverlappingSets, false);


        Map<Integer, boolean[]> pariallyOverlappingSets = new HashMap<>();

        pariallyOverlappingSets.put(0, new boolean[]{ true,  true, false, false, false, false, false, false});

        pariallyOverlappingSets.put(1, new boolean[]{false, false,  true, false, false, false, false, false});
        pariallyOverlappingSets.put(2, new boolean[]{false, false, false,  true, false, false, false, false});
        pariallyOverlappingSets.put(3, new boolean[]{false, false,  true,  true,  true, false, false, false});

        pariallyOverlappingSets.put(4, new boolean[]{false, false, false, false, false,  true,  true, false});
        pariallyOverlappingSets.put(5, new boolean[]{false, false, false, false, false,  true,  true, false});
        pariallyOverlappingSets.put(6, new boolean[]{false, false, false, false, false, false,  true,  true});
        pariallyOverlappingSets.put(7, new boolean[]{false, false, false, false, false, false,  true,  true});

        partiallyOverlappingMatrix = SimilarityMatrix.createMatrixFromSets(pariallyOverlappingSets, false);
    }

    @Test
    public void variableKNNwithNonPartialOverlappingTest() {
        Map<Integer, Integer> variableK = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(nonPartiallyOverlappingMatrix, 0.999);
        assertThat(variableK.get(0)).isEqualTo(1);
        assertThat(variableK.get(1)).isEqualTo(2);
        assertThat(variableK.get(2)).isEqualTo(2);
        assertThat(variableK.get(3)).isEqualTo(3);
        assertThat(variableK.get(4)).isEqualTo(3);
        assertThat(variableK.get(5)).isEqualTo(3);

        Map<Integer, int[]> variableKNN = KNN.bulkExtractVariableKNNIndices(nonPartiallyOverlappingMatrix, variableK);
        assertThat(variableKNN.get(0).length).isEqualTo(0);

        assertThat(variableKNN.get(1).length).isEqualTo(1);
        assertThat(variableKNN.get(2).length).isEqualTo(1);
        assertThat(variableKNN.get(1)[0]).isEqualTo(2);
        assertThat(variableKNN.get(2)[0]).isEqualTo(1);

        int[] K3 = variableKNN.get(3);
        int[] K4 = variableKNN.get(4);
        int[] K5 = variableKNN.get(5);
        assertThat(K3.length).isEqualTo(2);
        assertThat(K4.length).isEqualTo(2);
        assertThat(K5.length).isEqualTo(2);
        assertThat((K3[0] == 4 && K3[1] == 5) || (K3[0] == 5 && K3[1] == 4)).isTrue();
        assertThat((K4[0] == 3 && K4[1] == 5) || (K4[0] == 5 && K4[1] == 3)).isTrue();
        assertThat((K5[0] == 3 && K5[1] == 4) || (K5[0] == 4 && K5[1] == 3)).isTrue();
    }

    @Test
    public void variableKNNwithPartialOverlappingTest() {
        Map<Integer, Integer> variableK = KNN.getNumberOfEntriesWithValueAtLeastNForEachRow(partiallyOverlappingMatrix, 0.001);
        assertThat(variableK.get(0)).isEqualTo(1);
        assertThat(variableK.get(1)).isEqualTo(2);
        assertThat(variableK.get(2)).isEqualTo(2);
        assertThat(variableK.get(3)).isEqualTo(3);
        assertThat(variableK.get(4)).isEqualTo(4);
        assertThat(variableK.get(5)).isEqualTo(4);
        assertThat(variableK.get(6)).isEqualTo(4);
        assertThat(variableK.get(7)).isEqualTo(4);



        Map<Integer, int[]> variableKNN = KNN.bulkExtractVariableKNNIndices(partiallyOverlappingMatrix, variableK);
        assertThat(variableKNN.get(0).length).isEqualTo(0);

        int[] K3 = variableKNN.get(3);
        assertThat(variableKNN.get(1).length).isEqualTo(1);
        assertThat(variableKNN.get(2).length).isEqualTo(1);
        assertThat(K3.length).isEqualTo(2);
        assertThat(variableKNN.get(1)[0]).isEqualTo(3);
        assertThat(variableKNN.get(2)[0]).isEqualTo(3);
        assertThat((K3[0] == 1 && K3[1] == 2) || (K3[0] == 2 && K3[1] == 1)).isTrue();



        int[] K4 = variableKNN.get(4);
        int[] K5 = variableKNN.get(5);
        int[] K6 = variableKNN.get(6);
        int[] K7 = variableKNN.get(7);

        assertThat(K4.length).isEqualTo(3);
        assertThat(K5.length).isEqualTo(3);
        assertThat(K6.length).isEqualTo(3);
        assertThat(K7.length).isEqualTo(3);

        assertThat(K4[0]).isEqualTo(5);
        assertThat(K5[0]).isEqualTo(4);
        assertThat(K6[0]).isEqualTo(7);
        assertThat(K7[0]).isEqualTo(6);

        assertThat((K4[1] == 6 && K3[2] == 7) || (K4[1] == 7 && K4[2] == 6)).isTrue();
        assertThat((K5[1] == 6 && K5[2] == 7) || (K5[1] == 7 && K5[2] == 6)).isTrue();
        assertThat((K6[1] == 4 && K6[2] == 5) || (K6[1] == 5 && K6[2] == 4)).isTrue();
        assertThat((K7[1] == 4 && K7[2] == 5) || (K7[1] == 5 && K7[2] == 4)).isTrue();

    }
}
