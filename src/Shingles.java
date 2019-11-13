import java.util.List;

public class Shingles {

    public static int[] createSetOfShingles(List<Integer> list, int minInt, int maxInt, int shingleSize) {
        int count = maxInt - minInt + 1;
        int setSize = (int) Math.pow(count, shingleSize);
        int[] shingles = new int[setSize];

        for (int i = 0; i < list.size() - shingleSize + 1; ++i) {
            int shingleIndex = 0;
            for (int e = 0; e < shingleSize; ++e) {
                shingleIndex += ((int) Math.pow(count, shingleSize - e - 1)) * (list.get(i + e) - minInt);
            }
            System.out.print(shingleIndex + " | ");
            ++shingles[shingleIndex];
        }
        System.out.println();
        return shingles;
    }
}
