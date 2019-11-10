import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinHash {
    private int modulo;
    private int hashFunctionCount;
    private List<HashFunction> hashFunctions;

    MinHash(int modulo, int hashFunctionCount) {
        regenerateHashFunctions(modulo, hashFunctionCount);
    }

    public void regenerateHashFunctions(int modulo, int hashFunctionCount) {
        this.modulo = modulo;
        this.hashFunctionCount = hashFunctionCount;
        regenerateHashFunctions();
    }

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

    private static int gcd(int a, int b) {
        if (a % b == 0) return b;
        if (a < b) return gcd(b, a);
        return gcd(b, a % b);
    }

    public int[] createMinHash(boolean[] input) {
        int[] minhash = new int[hashFunctionCount];
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
