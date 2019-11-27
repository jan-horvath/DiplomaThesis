import java.util.List;

public class Shingles {

    static int[] createMultisetOfShingles(List<Integer> list, int minInt, int maxInt, int shingleSize) {
        int count = maxInt - minInt + 1;
        int setSize = (int) Math.pow(count, shingleSize);
        int[] shingles = new int[setSize];

        for (int i = 0; i < list.size() - shingleSize + 1; ++i) {
            int shingleIndex = 0;
            for (int e = 0; e < shingleSize; ++e) {
                shingleIndex += ((int) Math.pow(count, shingleSize - e - 1)) * (list.get(i + e) - minInt);
            }
            ++shingles[shingleIndex];
        }
        return shingles;
    }

    static boolean[] createSetOfShingles(List<Integer> list, int minInt, int maxInt, int shingleSize) {
        int count = maxInt - minInt + 1;
        int setSize = (int) Math.pow(count, shingleSize);
        boolean[] shingles = new boolean[setSize];

        for (int i = 0; i < list.size() - shingleSize + 1; ++i) {
            int shingleIndex = 0;
            for (int e = 0; e < shingleSize; ++e) {
                shingleIndex += ((int) Math.pow(count, shingleSize - e - 1)) * (list.get(i + e) - minInt);
            }
            shingles[shingleIndex] = true;
        }
        return shingles;
    }
}
