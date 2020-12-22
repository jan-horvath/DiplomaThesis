package cz.muni.fi.thesis;

import java.math.BigInteger;

/**
 * This class represents a hash function of the form ax + b mod n
 * Another useful way to look at this class is that it represents a pseudorandom permutation of numbers {0,...,n-1}
 */
public class HashFunction {
    private int modulus;
    private int mult;
    private int add;

    public HashFunction(int modulus, int mult, int add) throws ArithmeticException {
        BigInteger b1 = BigInteger.valueOf(modulus);
        BigInteger b2 = BigInteger.valueOf(mult);
        if (b1.gcd(b2).intValue() != 1) {
            throw new ArithmeticException("The greatest common divisor for \"modulus\" and \"mult\" is not 1!");
        }

        this.modulus = modulus;
        this.mult = mult;
        this.add = add;
    }

    public int hash(int number) {
        return (number * mult + add) % modulus;
    }
}
