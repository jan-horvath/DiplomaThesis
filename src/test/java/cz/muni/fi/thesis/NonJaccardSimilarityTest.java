package cz.muni.fi.thesis;

import static org.assertj.core.api.Assertions.*;

import com.google.common.collect.BiMap;
import cz.muni.fi.thesis.evaluation.KNN;
import cz.muni.fi.thesis.similarity.NonJaccardSimilarity;
import cz.muni.fi.thesis.similarity.MatrixType;
import cz.muni.fi.thesis.similarity.SimilarityMatrix;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class NonJaccardSimilarityTest {

    public List<Sequence> sequences;
    public BiMap<Shingle, Integer> shingleIds;

    @Before
    public void setUp() {
        Map<Integer, List<Integer>> motionWords = new HashMap<>();
        motionWords.put(0, Arrays.asList(5,5,5,6,6,6));
        motionWords.put(10, Arrays.asList(7,7,6,6,5,5));
        motionWords.put(20, Arrays.asList(5,5,5,5,5));
        motionWords.put(30, Arrays.asList(8,7,6));

        /*Map<Integer, List<Integer>> groundTruth = new HashMap<>();
        groundTruth.put(0, Collections.singletonList(99));
        groundTruth.put(10, Collections.singletonList(99));
        groundTruth.put(20, Collections.singletonList(99));
        groundTruth.put(30, Collections.singletonList(99));*/

        Map<Integer, String> scenarios = new HashMap<>();
        scenarios.put(0, "01-01");
        scenarios.put(10, "01-01");
        scenarios.put(20, "01-02");
        scenarios.put(30, "01-02");

        Sequence.setUp(motionWords, 1, 1, 99, 99);
        sequences = SequenceUtility.createSequences(motionWords, scenarios);
        shingleIds = Sequence.getShingleIds();
    }

    @Test
    public void CSTest1() {
        double[] vec1 = new double[] {2.0,3.0,4.0};
        double[] vec2 = new double[] {10.0, 20.0, 30.0};
        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(vec1, vec2))
                .isCloseTo(1.0 - 0.00741667, Offset.offset(0.0000001));
    }

    @Test
    public void CSTest2() {
        double[] vec1 = new double[] {2.0,3.0,4.0};
        double[] vec2 = new double[] {20.0, 30.0, 40.0};
        assertThat(NonJaccardSimilarity.cosineSimilarity(vec1, vec2))
                .isCloseTo(1.0, Offset.offset(0.0000001));
    }

    @Test
    public void cosineSimilarityBetweenAllPairsTest() {
        Offset<Double> offset = Offset.offset(0.0001);
        sequences.sort(Comparator.comparingInt(Sequence::getId));

        for (Sequence seq : sequences) {
            double[] tfIdf = seq.toTfIdfWeights(true);
            assertThat(NonJaccardSimilarity.cosineSimilarity(tfIdf, tfIdf)).isCloseTo(1.0, offset);
        }

        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(0).toTfIdfWeights(true), sequences.get(1).toTfIdfWeights(true))).isCloseTo(0.506197, offset);
        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(1).toTfIdfWeights(true), sequences.get(0).toTfIdfWeights(true))).isCloseTo(0.506197, offset);

        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(0).toTfIdfWeights(true), sequences.get(2).toTfIdfWeights(true))).isCloseTo(0.707107, offset);
        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(2).toTfIdfWeights(true), sequences.get(0).toTfIdfWeights(true))).isCloseTo(0.707107, offset);

        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(0).toTfIdfWeights(true), sequences.get(3).toTfIdfWeights(true))).isCloseTo(0.271057, offset);
        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(3).toTfIdfWeights(true), sequences.get(0).toTfIdfWeights(true))).isCloseTo(0.271057, offset);

        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(1).toTfIdfWeights(true), sequences.get(2).toTfIdfWeights(true))).isCloseTo(0.357936, offset);
        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(2).toTfIdfWeights(true), sequences.get(1).toTfIdfWeights(true))).isCloseTo(0.357936, offset);

        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(1).toTfIdfWeights(true), sequences.get(3).toTfIdfWeights(true))).isCloseTo(0.933746, offset);
        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(3).toTfIdfWeights(true), sequences.get(1).toTfIdfWeights(true))).isCloseTo(0.933746, offset);

        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(2).toTfIdfWeights(true), sequences.get(3).toTfIdfWeights(true))).isCloseTo(0.0, offset);
        Assertions.assertThat(NonJaccardSimilarity.cosineSimilarity(sequences.get(3).toTfIdfWeights(true), sequences.get(2).toTfIdfWeights(true))).isCloseTo(0.0, offset);
    }

    @Test
    public void twoNearestNeighboursTest() {
        SimilarityMatrix matrix = SimilarityMatrix.createMatrix(sequences, MatrixType.TFIDF_TFIDF_IGNORE);
        Map<Integer, int[]> twoNN = KNN.bulkExtractKNNIndices(matrix, 2);

        assertThat(twoNN.get(0)[0]).isEqualTo(20);
        assertThat(twoNN.get(0)[1]).isEqualTo(10);

        assertThat(twoNN.get(10)[0]).isEqualTo(30);
        assertThat(twoNN.get(10)[1]).isEqualTo(0);

        assertThat(twoNN.get(20)[0]).isEqualTo(0);
        assertThat(twoNN.get(20)[1]).isEqualTo(10);

        assertThat(twoNN.get(30)[0]).isEqualTo(10);
        assertThat(twoNN.get(30)[1]).isEqualTo(0);
    }
}
