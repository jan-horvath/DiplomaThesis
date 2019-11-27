import java.util.List;

public class Shingles {

    /**
     * Creates multiset of shingles from the input list.
     * @param list input
     * @param minInt minimal integer contained in the list
     * @param maxInt maximal integer contained in the list
     * @param shingleSize how big "sliding window" should be considered
     * @return array of int where
     * (output[i] = k) means the shingle with index i appeared in the input list k times
     */
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

    /**
     * Creates set of shingles from the input list.
     * @param list input
     * @param minInt minimal integer contained in the list
     * @param maxInt maximal integer contained in the list
     * @param shingleSize how big "sliding window" should be considered
     * @return array of booleans where
     * (output[i] = true) means the shingle with index i appeared somewhere in the input
     */
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
