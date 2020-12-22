package cz.muni.fi.thesis;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShingleCreationUsingTFIDFTest {

    @Before
    public void setUp() {
        BiMap<Shingle, Integer> shingleIDs = HashBiMap.create();
        shingleIDs.put(new Shingle(Arrays.asList(10)), 0);
        shingleIDs.put(new Shingle(Arrays.asList(20)), 1);
        shingleIDs.put(new Shingle(Arrays.asList(30)), 2);
        shingleIDs.put(new Shingle(Arrays.asList(10, 10)), 3);
        shingleIDs.put(new Shingle(Arrays.asList(10, 20)), 4);
        shingleIDs.put(new Shingle(Arrays.asList(20, 30)), 5);
        shingleIDs.put(new Shingle(Arrays.asList(30, 30)), 6);
        shingleIDs.put(new Shingle(Arrays.asList(30, 10)), 7);
        shingleIDs.put(new Shingle(Arrays.asList(88,88)), 8);
        shingleIDs.put(new Shingle(Arrays.asList(99,99)), 9);
        ShingleUtility.setShingleIDs(shingleIDs);

        Map<Integer, Double> IDF = new HashMap<>();
        IDF.put(0, 2.0);
        IDF.put(1, 5.0);
        IDF.put(2, 4.0);
        IDF.put(3, 3.0);
        IDF.put(4, 6.0);
        IDF.put(5, 10.0);
        IDF.put(6, 0.0);
        IDF.put(7, 11.0);
        IDF.put(8, 888.0);
        IDF.put(9, 999.0);
        ShingleUtility.setIDF(IDF);
    }

    @Test
    public void ShingleCreation25PercentTest() {
        Map<Integer, List<Integer>> data = new HashMap<>();
        data.put(100, Arrays.asList(10,10,10,20,30,30,10));
        Map<Integer, boolean[]> setsOfShingles = ShingleUtility.createSetsOfShinglesUsingTFIDF(data, 1, 2, 25);

        assertThat(setsOfShingles.size()).isEqualTo(1);
        boolean[] set = setsOfShingles.get(100);
        assertThat(set.length).isEqualTo(10);
        assertThat(set[0]).isFalse();
        assertThat(set[1]).isFalse();
        assertThat(set[2]).isFalse();
        assertThat(set[3]).isFalse();
        assertThat(set[4]).isFalse();
        assertThat(set[5]).isTrue();
        assertThat(set[6]).isFalse();
        assertThat(set[7]).isTrue();
        assertThat(set[8]).isFalse();
    }

    @Test
    public void ShingleCreation50PercentTest() {
        Map<Integer, List<Integer>> data = new HashMap<>();
        data.put(100, Arrays.asList(10,10,10,20,30,30,10));
        Map<Integer, boolean[]> setsOfShingles = ShingleUtility.createSetsOfShinglesUsingTFIDF(data, 1, 2, 50);

        assertThat(setsOfShingles.size()).isEqualTo(1);
        boolean[] set = setsOfShingles.get(100);
        assertThat(set.length).isEqualTo(10);
        assertThat(set[0]).isTrue();
        assertThat(set[1]).isFalse();
        assertThat(set[2]).isTrue();
        assertThat(set[3]).isFalse();
        assertThat(set[4]).isFalse();
        assertThat(set[5]).isTrue();
        assertThat(set[6]).isFalse();
        assertThat(set[7]).isTrue();
        assertThat(set[8]).isFalse();
    }

    @Test
    public void ShingleCreation75PercentTest() {
        Map<Integer, List<Integer>> data = new HashMap<>();
        data.put(100, Arrays.asList(10,10,10,20,30,30,10));
        Map<Integer, boolean[]> setsOfShingles = ShingleUtility.createSetsOfShinglesUsingTFIDF(data, 1, 2, 75);

        assertThat(setsOfShingles.size()).isEqualTo(1);
        boolean[] set = setsOfShingles.get(100);
        assertThat(set.length).isEqualTo(10);
        assertThat(set[0]).isTrue();
        assertThat(set[1]).isFalse();
        assertThat(set[2]).isTrue();
        assertThat(set[3]).isTrue();
        assertThat(set[4]).isTrue();
        assertThat(set[5]).isTrue();
        assertThat(set[6]).isFalse();
        assertThat(set[7]).isTrue();
        assertThat(set[8]).isFalse();
    }

    @Test
    public void ShingleCreation100PercentTest() {
        Map<Integer, List<Integer>> data = new HashMap<>();
        data.put(100, Arrays.asList(10,10,10,20,30,30,10));
        Map<Integer, boolean[]> setsOfShingles = ShingleUtility.createSetsOfShinglesUsingTFIDF(data, 1, 2, 100);

        assertThat(setsOfShingles.size()).isEqualTo(1);
        boolean[] set = setsOfShingles.get(100);
        assertThat(set.length).isEqualTo(10);
        assertThat(set[0]).isTrue();
        assertThat(set[1]).isTrue();
        assertThat(set[2]).isTrue();
        assertThat(set[3]).isTrue();
        assertThat(set[4]).isTrue();
        assertThat(set[5]).isTrue();
        assertThat(set[6]).isTrue();
        assertThat(set[7]).isTrue();
        assertThat(set[8]).isFalse();
    }

}
