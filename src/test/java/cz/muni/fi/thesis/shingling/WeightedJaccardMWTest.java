package cz.muni.fi.thesis.shingling;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class WeightedJaccardMWTest {

    String filename = "MW_testfile2.txt";

    private boolean areClose(double x, double y) {
        return Math.abs(x - y) < 0.001;
    }

    private boolean containsJaccardEntry(List<SimilarityMatrix.JaccardEntry> list, int recordId, double jaccardValue){
        return list.stream().anyMatch(o -> (o.recordID == recordId) && (areClose(jaccardValue, o.jaccardValue)));
    }

    @Before
    public void createInputFile() throws IOException {
        File file = new File(filename);
        FileWriter fw = new FileWriter(file);

        fw.write("#objectKey messif.objects.keys.AbstractObjectKey 100_0_0_0\n4;mcdr.objects.impl.ObjectMotionWord\n");
        fw.write("10\n20\n30\n40\n");
        fw.write("#objectKey messif.objects.keys.AbstractObjectKey 101_0_0_0\n4;mcdr.objects.impl.ObjectMotionWord\n");
        fw.write("40\n30\n20\n10\n");
        fw.write("#objectKey messif.objects.keys.AbstractObjectKey 102_0_0_0\n3;mcdr.objects.impl.ObjectMotionWord\n");
        fw.write("10\n20\n10\n");
        fw.close();
    }

    @Test
    public void weightedJaccardNotIgnoringMaxIDFTest() throws IOException {
        Map<Integer, List<Integer>> data = DataLoader.parseDataFile(filename);
        ShingleUtility.computeInverseDocumentFrequencyForShingles(data, 1, 2, false);
        Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfShingles(data, 1,2);
        Map<Integer, List<SimilarityMatrix.JaccardEntry>> dataMatrix =
                SimilarityMatrix.createMatrixFromSets(dataShingles, true).getMatrix();

        double A = Math.log(1.5);
        double B = Math.log(3);

        assertThat(containsJaccardEntry(dataMatrix.get(100), 101, 2*A/(4*A+4*B))).isTrue();
        assertThat(containsJaccardEntry(dataMatrix.get(101), 102,  A/(4*A+2*B))).isTrue();
        assertThat(containsJaccardEntry(dataMatrix.get(100), 102,  A/(4*A+2*B))).isTrue();
    }

    @Test
    public void weightedJaccardIgnoringMaxIDFTest() throws IOException {
        Map<Integer, List<Integer>> data = DataLoader.parseDataFile(filename);
        ShingleUtility.computeInverseDocumentFrequencyForShingles(data, 1, 2, true);
        Map<Integer, boolean[]> dataShingles = ShingleUtility.createSetsOfShingles(data, 1,2);
        Map<Integer, List<SimilarityMatrix.JaccardEntry>> dataMatrix =
                SimilarityMatrix.createMatrixFromSets(dataShingles, true).getMatrix();

        double A = Math.log(1.5);

        assertThat(containsJaccardEntry(dataMatrix.get(100), 101, 2*A/(4*A))).isTrue();
        assertThat(containsJaccardEntry(dataMatrix.get(101), 102,  A/(4*A))).isTrue();
        assertThat(containsJaccardEntry(dataMatrix.get(100), 102,  A/(4*A))).isTrue();
    }
}
