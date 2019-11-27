import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * This class contains hash functions and computes min hash for any array of booleans using these functions
 */
public class MinHashCreator {
    private int modulo;
    private int hashFunctionCount;
    private List<HashFunction> hashFunctions;

    MinHashCreator(int modulo, int hashFunctionCount) {
        regenerateHashFunctions(modulo, hashFunctionCount);
    }

    /**
     * Throws away current hash function and generates new ones.
     * @param modulo the hash functions will be able to generate numbers in range (0, modulo-1)
     * @param hashFunctionCount how many hash functions should be created
     */
    public void regenerateHashFunctions(int modulo, int hashFunctionCount) {
        this.modulo = modulo;
        this.hashFunctionCount = hashFunctionCount;
        regenerateHashFunctions();
    }

    /**
     * Throws away current hash function and generates the same amount of new ones with the same modulo parameter.
     */
    public void regenerateHashFunctions() {
        hashFunctions = new ArrayList<>();
        int j = 1;
        Random rand = new Random();

        for (int i = 0; i < hashFunctionCount; ++i) {
            if (gcd(modulo, j) == 1) {
                hashFunctions.add(new HashFunction(modulo, j, rand.nextInt(modulo)));
            } else {
                --i;
            }
            ++j;
        }
    }

    /**
     * Computed greates common divisor
     */
    private static int gcd(int a, int b) {
        if (a % b == 0) return b;
        if (a < b) return gcd(b, a);
        return gcd(b, a % b);
    }

    /**
     * Creates a minhash for the input
     * @param input array of booleans which represents a set
     * @return minhash for the input
     */
    public int[] createMinHash(boolean[] input) {
        int[] minhash = new int[hashFunctionCount];
        Arrays.fill(minhash, Integer.MAX_VALUE);
        for (int i = 0; i < input.length; ++i) {
            if (input[i]) { //Update hashes
                for (int j = 0; j < hashFunctions.size(); ++j) {
                    int nextHash = hashFunctions.get(j).hash(i);
                    if (nextHash < minhash[j]) {
                        minhash[j] = nextHash;
                    }
                }
            }
        }
        return minhash;
    }

    public int getModulo() {
        return modulo;
    }

    public int getHashFunctionCount() {
        return hashFunctionCount;
    }
}
