import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MinHashTable {
    private final char FIRST_CHAR;
    private int CHAR_COUNT;
    private int SHINGLE_SIZE;
    private int PERMUTATIONS;

    private List<HashFunction> hashFunctions = new ArrayList<>();
    private List<List<Integer>> minhashTable = new ArrayList<>();

    private boolean COMPUTE_JACCARD;
    private List<Boolean[]> sparseSets;
    private List<List<Double>> jaccardCoefficients;


    MinHashTable(String filename, char firstChar, int charCount, int shingleSize, int hashFunctionCount, boolean computeJaccard) throws IOException {
        FIRST_CHAR = firstChar;
        CHAR_COUNT = charCount;
        SHINGLE_SIZE = shingleSize;
        PERMUTATIONS = (int)Math.pow(CHAR_COUNT, SHINGLE_SIZE);
        COMPUTE_JACCARD = computeJaccard;

        if (COMPUTE_JACCARD) {
            sparseSets = new ArrayList<>();
            jaccardCoefficients = new ArrayList<>();
        }

        generateHashFunctions(hashFunctions, hashFunctionCount);

        File inputFile = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line;

        while ((line = br.readLine()) != null) {

            Boolean[] sparseSet = createSparseSet(line);
            if (COMPUTE_JACCARD) {
                sparseSets.add(sparseSet);
                List<Double> NewJaccardCoefficients = computeJaccardCoefficientForSet(sparseSet);
                jaccardCoefficients.add(NewJaccardCoefficients);
            }

            ArrayList<Integer> minHashes = new ArrayList<>(Collections.nCopies(hashFunctions.size(), Integer.MAX_VALUE));
            getMinHashForLine(sparseSet, minHashes);
            minhashTable.add(minHashes);
        }
    }

    MinHashTable(String filename, char firstChar, int charCount, int shingleSize, int hashFunctionCount) throws IOException {
            this(filename, firstChar, charCount, shingleSize, hashFunctionCount, false);
    }

    private void generateHashFunctions(List<HashFunction> list, int count) {
        int j = 1;
        Random rand = new Random();

        for (int i = 0; i < count; ++i) {
            if (gcd(PERMUTATIONS, j) == 1) {
                list.add(new HashFunction(PERMUTATIONS, j, rand.nextInt(PERMUTATIONS)));
            } else {
                --i;
            }
            ++j;
        }
    }

    private static int gcd(int a, int b) {
        if (a % b == 0) return b;
        if (a < b) return gcd(b, a);
        return gcd(b, a % b);
    }

    private Boolean[] createSparseSet(String line) {
        Boolean[] sparseVector = new Boolean[PERMUTATIONS];
        Arrays.fill(sparseVector, false);

        for (int i = 0; i < line.length() - SHINGLE_SIZE + 1; ++i) {
            int encodedShingle = 0;
            for (int j = 0; j < SHINGLE_SIZE; ++j) {
                int base = (int)Math.pow(CHAR_COUNT, (SHINGLE_SIZE - 1 - j));
                encodedShingle += (line.charAt(j+i) - FIRST_CHAR) * base;
            }
            sparseVector[encodedShingle] = true;
        }
        return sparseVector;
    }

    private void getMinHashForLine(Boolean[] sparseSet, ArrayList<Integer> minHashes) {
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
    }

    private List<Double> computeJaccardCoefficientForSet(Boolean[] newSet) {
        List<Double> jaccardCoefficients = new ArrayList<>();
        for (Boolean[] set : sparseSets) {
            assert(set.length == newSet.length);

            int total = 0;
            int common = 0;
            for (int i = 0; i < set.length; ++i) {
                if (set[i] || newSet[i]) {
                    ++total;
                    if (set[i] && newSet[i]) {
                        ++common;
                    }
                }
            }
            jaccardCoefficients.add(((double) common) / total);
        }
        return jaccardCoefficients;
    }

    /**
     * Computes similarity from minhashes.
     * @param col1 - index of the first item to be compared
     * @param col2 - index of the second item to be compared
     * @return approximation of Jaccard similarity based on the similarity of minhashes
     */
    double getMinHashSimilarity(int col1, int col2) {
        double matchingHashes = 0;
        for (int i = 0; i < hashFunctions.size(); ++i) {
            if (minhashTable.get(col1).get(i) == minhashTable.get(col2).get(i)) {
                matchingHashes += 1.0;
            }
        }
        return matchingHashes/hashFunctions.size();
    }

    double getJaccardCoefficient(int col1, int col2) {
        if (col1 > col2) return jaccardCoefficients.get(col1).get(col2);
        else return jaccardCoefficients.get(col2).get(col1);
    }
}
