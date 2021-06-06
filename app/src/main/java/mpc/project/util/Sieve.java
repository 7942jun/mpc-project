package mpc.project.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class Sieve {
    private final int bitNum;
    private BigInteger fixValue = BigInteger.ONE;
    private BigInteger M = BigInteger.ONE;

    public BigInteger getM() {
        return M;
    }

    public BigInteger getRandomFactor(Random rnd) {
        return MathUtility.genRandBig(BigInteger.TWO.pow(bitNum).divide(M), rnd);
    }

    public Sieve(int clusterSize, int bitNum) {
        // Todo: Implement distributed sieving
        this.bitNum = bitNum;
        BigInteger sievingBound = BigInteger.TWO.pow(bitNum - 1);

        BigInteger[] primeTable = MathUtility.generatePrimeNumberTable(BigInteger.valueOf(150000));

        int index = Arrays.binarySearch(primeTable, BigInteger.valueOf(clusterSize));

        if (index < 0) { // clusterSize is in the prime table
            index = -(index + 1);
        } else {
            index++; // clusterSize is in the prime table
        }

        this.fixValue = MathUtility.arrayProduct(Arrays.copyOfRange(primeTable, 0, index));

        boolean needToCreateNewPrimeTable = true;

        while (true) {
            for (BigInteger i : Arrays.copyOfRange(primeTable, index, primeTable.length)) {
                BigInteger temp = this.M.multiply(i);
                if (temp.compareTo(sievingBound) > 0) {
                    needToCreateNewPrimeTable = false;
                    break;
                } else {
                    this.M = temp;
                }
            }
            if (!needToCreateNewPrimeTable) {
                break;
            }
            primeTable = MathUtility.generatePrimeNumberTable(
                    primeTable[primeTable.length - 1].add(BigInteger.valueOf(100)),
                    primeTable);
        }
    }

    public BigInteger generateSievedNumber(Random rnd) {
        BigInteger a = null;
        boolean foundGoodCandidate = false;
        do {
            BigInteger r = MathUtility.genRandBig(M, rnd);
            for (int i = 0; i < 31; i++) {
                BigInteger target = r.add(BigInteger.valueOf(i));
                if (target.gcd(M).equals(BigInteger.ONE) &&
                        target.gcd(fixValue).equals(BigInteger.ONE)) {
                    foundGoodCandidate = true;
                    a = target;
                    break;
                }
            }
        } while (!foundGoodCandidate);

        return a;
    }
}
