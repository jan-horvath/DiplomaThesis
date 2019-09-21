import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Main {
    /*static final char START = 'A';
    static final int CHARACTERS = 4;
    static final int SHINGLE_SIZE = 2;
    static final int PERMUTATIONS = (int)Math.pow(CHARACTERS, SHINGLE_SIZE);

    public static boolean[] createSparseSet(String line) {
        boolean[] sparseVector = new boolean[PERMUTATIONS];
        Arrays.fill(sparseVector, false);

        for (int i = 0; i < line.length() - SHINGLE_SIZE + 1; ++i) {
            System.out.println(line.substring(i, i + SHINGLE_SIZE));
            int encodedShingle = 0;
            for (int j = 0; j < SHINGLE_SIZE; ++j) {
                int base = (int)Math.pow(CHARACTERS, (SHINGLE_SIZE - 1 - j));
                encodedShingle += (line.charAt(j+i) - START) * base;
            }
            sparseVector[encodedShingle] = true;
        }
        return sparseVector;
    }



    public static void main(String[] args) throws IOException {
        File inputFile = new File("testfile.txt");
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line;

        ArrayList<HashFunction> hashFunctions = new ArrayList<>();
        hashFunctions.add(new HashFunction(PERMUTATIONS, 1, 9));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 3, 8));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 5, 7));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 7, 6));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 9, 4));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 11, 2));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 13, 9));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 17, 18));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 19, 17));
        hashFunctions.add(new HashFunction(PERMUTATIONS, 21, 0));

        ArrayList<ArrayList<Integer>> minhashTable = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            System.out.println(line);
            boolean[] sparseSet = createSparseSet(line);
            ArrayList<Integer> minHashes = new ArrayList<>(Collections.nCopies(hashFunctions.size(), Integer.MAX_VALUE));

            for (int i = 0; i < sparseSet.length; ++i) { //permutation indices
                if (sparseSet[i]) { //Update minhashes
                    for (int j = 0; j < hashFunctions.size(); ++j) {
                        int nextHash = hashFunctions.get(j).hash(i);
                        if (nextHash < minHashes.get(j)) {
                            minHashes.set(j, nextHash);
                        }
                    }
                }
            }
            minhashTable.add(minHashes);
        }
        System.out.println(minhashTable.toString());
    }*/
    public static void main(String[] args) throws IOException {
        MinHashTable mht = new MinHashTable("TestFiles/testfile.txt", 'A', 5, 3, 100, true);
        System.out.print(mht.getMinHashSimilarity(0, 0) * 100 + "%     ");
        System.out.println(mht.getJaccardCoefficient(0, 0) * 100 + "%");

        System.out.print(mht.getMinHashSimilarity(0, 1) * 100 + "%     ");
        System.out.println(mht.getJaccardCoefficient(0, 1) * 100 + "%");

        System.out.print(mht.getMinHashSimilarity(0, 2) * 100 + "%     ");
        System.out.println(mht.getJaccardCoefficient(0, 2) * 100 + "%");

        System.out.print(mht.getMinHashSimilarity(0, 3) * 100 + "%     ");
        System.out.println(mht.getJaccardCoefficient(0, 3) * 100 + "%");

        System.out.print(mht.getMinHashSimilarity(0, 4) * 100 + "%     ");
        System.out.println(mht.getJaccardCoefficient(0, 4) * 100 + "%");
    }
}

//TODO implement minhash similarity (compare minhashes)
//TODO implement MinHashTable class