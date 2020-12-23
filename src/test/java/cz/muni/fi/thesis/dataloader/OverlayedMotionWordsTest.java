package cz.muni.fi.thesis.dataloader;

import cz.muni.fi.thesis.dataloader.MoCapDataLoader;
import cz.muni.fi.thesis.similarity.OverlaySimilarity;
import cz.muni.fi.thesis.similarity.SimilarityMatrix;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class OverlayedMotionWordsTest {

    private boolean areClose(double x, double y) {
        return Math.abs(x - y) < 0.001;
    }

    private boolean containsJaccardEntry(List<SimilarityMatrix.SimilarityEntry> list, int recordId, double jaccardValue){
        return list.stream().anyMatch(o -> (o.recordID == recordId) && (areClose(jaccardValue, o.jaccardValue)));
    }

    String filename = "MW_testfile.txt";

    @Before
    public void createInputFile() throws IOException {
        File file = new File(filename);
        FileWriter fw = new FileWriter(file);

        fw.write("#objectKey messif.objects.keys.AbstractObjectKey 100_0_0_0\n3;mcdr.objects.impl.ObjectMotionWord\n");
        fw.write("0,10,20,30,40\n10,20,30,40,50\n20,30,40,50,60\n");
        fw.write("#objectKey messif.objects.keys.AbstractObjectKey 11_0_0_0\n1;mcdr.objects.impl.ObjectMotionWord\n");
        fw.write("11,12,13,14,15\n");
        fw.write("#objectKey messif.objects.keys.AbstractObjectKey 444_0_0_0\n2;mcdr.objects.impl.ObjectMotionWord\n");
        fw.write("444,445,446,447,448\n555,556,557,558,559\n");
        fw.close();
    }

    @Test
    public void overlayDataLoadingTest() throws IOException {
        Map<Integer, List<int[]>> parsedData = MoCapDataLoader.parseMomwDataFile(filename);

        assertThat(parsedData.size()).isEqualTo(3);
        assertThat(parsedData.get(100).size()).isEqualTo(3);
        assertThat(parsedData.get(11).size()).isEqualTo(1);
        assertThat(parsedData.get(444).size()).isEqualTo(2);

        List<int[]> rec_100 = parsedData.get(100);
        for (int i = 0; i < 3; ++i) {
            int[] rec_100_i = rec_100.get(i);
            assertThat(rec_100_i.length).isEqualTo(5);
            for (int j = 0; j < 5; ++j) {
                assertThat(rec_100_i[j]).isEqualTo(j*10+i*10);
            }
        }

        List<int[]> rec_11 = parsedData.get(11);
        int[] rec_11_i = rec_11.get(0);
        assertThat(rec_11_i.length).isEqualTo(5);
        for (int j = 0; j < 5; ++j) {
            assertThat(rec_11_i[j]).isEqualTo(11+j);
        }

        List<int[]> rec_444 = parsedData.get(444);
        for (int i = 0; i < 2; ++i) {
            int[] rec_444_i = rec_444.get(i);
            assertThat(rec_444_i.length).isEqualTo(5);
            for (int j = 0; j < 5; ++j) {
                assertThat(rec_444_i[j]).isEqualTo(111*(i+4)+j);
            }
        }
    }

//    @Test
//    public void overlayDataMultisetSimilarityCalculation() {
//        Map<Integer, List<int[]>> parsedData = new HashMap<>();
//
//        parsedData.put(1, Arrays.asList(new int[]{0,1,2,3,4}, new int[]{0,10,20,30,40}));
//        parsedData.put(2, Arrays.asList(new int[]{0,1,4,9,16}, new int[]{6,6,6,6,6}, new int[]{100,200,300,400,500}));
//        parsedData.put(3, Arrays.asList(new int[]{0,5,4,5,5}, new int[]{5,5,5,5,5}));
//
//        SimilarityMatrix oneOfFiveSM = SimilarityMatrix.createMatrixFromOverlayData(parsedData, 1, OverlaySimilarity.MULTISET_EQUIVALENT);
//        assertThat(containsJaccardEntry(oneOfFiveSM.getMatrix().get(1), 2, 1.0/3)).isTrue();
//        assertThat(containsJaccardEntry(oneOfFiveSM.getMatrix().get(1), 3, 1.0/2)).isTrue();
//        assertThat(containsJaccardEntry(oneOfFiveSM.getMatrix().get(2), 3, 1.0/6)).isTrue();
//
//        SimilarityMatrix twoOfFiveSM = SimilarityMatrix.createMatrixFromOverlayData(parsedData, 2, OverlaySimilarity.MULTISET_EQUIVALENT);
//        assertThat(containsJaccardEntry(twoOfFiveSM.getMatrix().get(1), 2, 1.0/6)).isTrue();
//        assertThat(containsJaccardEntry(twoOfFiveSM.getMatrix().get(1), 3, 0.0)).isTrue();
//        assertThat(containsJaccardEntry(twoOfFiveSM.getMatrix().get(2), 3, 1.0/6)).isTrue();
//    }

    @Test
    public void countEachOnce_overlayJaccardTest() {
        List<int[]> rec1 = Arrays.asList(new int[]{0, 1, 2, 3, 4}, new int[]{0, 5, 20, 30, 40}, new int[]{9, 9, 9, 9, 9});
        List<int[]> rec2 = Arrays.asList(new int[]{0, 1, 5, 5, 5}, new int[]{0, 5, 5, 5, 4});
        double value = OverlaySimilarity.overlayJaccard3(rec1, rec2, 2);
        assertThat(value).isCloseTo(0.8, Offset.offset(0.0001));
    }
}
