import java.io.*;
import java.util.*;

public class MinHashTable {
    private final char FIRST_CHAR;
    private int CHAR_COUNT;
    private int SHINGLE_SIZE;
    private int PERMUTATIONS;

    private List<HashFunction> hashFunctions = new ArrayList<>();
    //private List<List<Integer>> minhashTable = new ArrayList<>();
    private Map<String, List<Integer>> minhashTable = new HashMap<>();

    private boolean COMPUTE_JACCARD;
    private Map<String, boolean[]> sparseSets;
    /*private List<boolean[]> sparseSets;
    private List<List<Double>> jaccardCoefficients;*/


    MinHashTable(String folderName, AsciiCharactersLimitation limitation, int shingleSize, int hashFunctionCount, boolean computeJaccard) throws IOException {
        switch (limitation) {
            case ALL_CHARACTERS:
                FIRST_CHAR = ' ';
                CHAR_COUNT = '}' - FIRST_CHAR + 1;
                break;

            case NUMBERS_AND_LETTERS:
                FIRST_CHAR = '0';
                CHAR_COUNT = 'z' - FIRST_CHAR + 1;
                break;

            case LETTERS_LOWERCASE:
                FIRST_CHAR = 'a';
                CHAR_COUNT = 'z' - FIRST_CHAR + 1;
                break;

            default:
                throw new IllegalArgumentException("This should not have happened. The AsciiCharactersLimitation must have been changed.");
        }

        SHINGLE_SIZE = shingleSize;
        PERMUTATIONS = (int)Math.pow(CHAR_COUNT, SHINGLE_SIZE);
        COMPUTE_JACCARD = computeJaccard;

        if (COMPUTE_JACCARD) {
            sparseSets = new HashMap<>();
        }

        generateHashFunctions(hashFunctions, hashFunctionCount);

        processFilesInFolder(folderName, limitation);
    }

    MinHashTable(String folderName, AsciiCharactersLimitation limitation, int shingleSize, int hashFunctionCount) throws IOException {
            this(folderName, limitation, shingleSize, hashFunctionCount, false);
    }

    private void processFilesInFolder(String folderName, AsciiCharactersLimitation limitation) throws IOException {
        File folder = new File(folderName);

        for (File file : folder.listFiles()) {
            boolean[] sparseSet = new boolean[PERMUTATIONS]; //is filled with false by default
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                line = modifyLine(line, limitation);
                fillSparseSet(sparseSet, line);
            }

            if (COMPUTE_JACCARD) {
                sparseSets.put(file.getName(), sparseSet);
            }

            ArrayList<Integer> minHashes = new ArrayList<>(Collections.nCopies(hashFunctions.size(), Integer.MAX_VALUE));
            getMinHashForLine(sparseSet, minHashes);
            minhashTable.put(file.getName(), minHashes);
        }
    }

    private String modifyLine(String line, AsciiCharactersLimitation limitation) {
        switch (limitation) {
            case ALL_CHARACTERS:
                return line;

            case NUMBERS_AND_LETTERS:
                return line.replaceAll("[^\\w]", "");

            case LETTERS_LOWERCASE:
                return line.replaceAll("[^a-zA-Z]", "").toLowerCase();

            default:
                throw new IllegalArgumentException("This should not have happened. The AsciiCharactersLimitation must have been changed.");
        }
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

    private void fillSparseSet(boolean[] sparseSet, String line) {
        for (int i = 0; i < line.length() - SHINGLE_SIZE + 1; ++i) {
            int encodedShingle = 0;
            for (int j = 0; j < SHINGLE_SIZE; ++j) {
                int base = (int)Math.pow(CHAR_COUNT, (SHINGLE_SIZE - 1 - j));
                encodedShingle += (line.charAt(j+i) - FIRST_CHAR) * base;
            }
            sparseSet[encodedShingle] = true;
        }
    }

    private void getMinHashForLine(boolean[] sparseSet, ArrayList<Integer> minHashes) {
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

    /**
     * Computes similarity from minhashes.
     * @param file1 - name of the first file to be compared
     * @param file2 - name of the second file to be compared
     * @return approximation of Jaccard similarity based on the similarity of minhashes
     */
    double getMinHashSimilarity(String file1, String file2) {
        double matchingHashes = 0;
        for (int i = 0; i < hashFunctions.size(); ++i) {
            if (minhashTable.get(file1).get(i).equals(minhashTable.get(file2).get(i))) {
                matchingHashes += 1.0;
            }
        }
        return matchingHashes/hashFunctions.size();
    }

    double getJaccardCoefficient(String file1, String file2) {
        boolean[] set1 = sparseSets.get(file1);
        boolean[] set2 = sparseSets.get(file2);

        int total = 0;
        int common = 0;
        for (int i = 0; i < set1.length; ++i) {
            if (set1[i] || set2[i]) {
                ++total;
                if (set1[i] && set2[i]) {
                    ++common;
                }
            }
        }
        return ((double) common)/total;
    }
}
