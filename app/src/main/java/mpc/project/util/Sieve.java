package mpc.project.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class Sieve {
    private final int bitNum;
    private final BigInteger fixValue;
    private BigInteger M = BigInteger.ONE;

    public BigInteger getM() {
        return M;
    }

    public BigInteger getRandomFactor(Random rnd) {
        return MathUtility.genRandBig(BigInteger.TWO.pow(bitNum).divide(M), rnd);
    }

    private Long[] primeTable = {
            2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L, 43L, 47L, 53L, 59L, 61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L
    };

    public Sieve(int clusterSize, int bitNum) {
        // Todo: Implement distributed sieving
        this.bitNum = bitNum;
        BigInteger sievingBound = BigInteger.TWO.pow(bitNum - 1);

        // Todo: finish implementing this. It's currently just "usable"
        int index = Arrays.binarySearch(primeTable, (long) clusterSize);
        if (index < 0) {
            index = -(index + 1);
            this.fixValue = BigInteger.valueOf(
                    MathUtility.arrayProduct(Arrays.copyOfRange(primeTable, 0, index)));
//            boolean isPrimeTableDrained = true;
            BigInteger currentPrime;
            for (Long i : Arrays.copyOfRange(primeTable, index, primeTable.length)) {
                currentPrime = BigInteger.valueOf(i);
                BigInteger temp = this.M.multiply(currentPrime);
                if (temp.compareTo(sievingBound) > 0) {
//                    isPrimeTableDrained = false;
                    break;
                } else {
                    this.M = temp;
                }
            }
//            if (isPrimeTableDrained) {
//                while (true) {
//                    currentPrime = MathUtility.getNextPrime(currentPrime)
//                    BigInteger temp = this.M.multiply(currentPrime);
//                    if (temp.compareTo(sievingBound) > 0) {
//                        break;
//                    } else {
//                        this.M = temp;
//                    }
//                }
//            }
//        } else if (index == primeTable.length) {
//            this.fixValue = MathUtility.arrayProduct(
//                    MathUtility.generatePrimeNumberTable(BigInteger.valueOf(clusterSize)));
//            BigInteger[] newPrimeTable = MathUtility.generatePrimeNumberTable(
//                    BigInteger.valueOf(primeTable[primeTable.length - 1]),
//                    BigInteger.valueOf(clusterSize));
//            for (BigInteger i : newPrimeTable) {
//                BigInteger temp = this.M.multiply(i);
//                if (temp.compareTo(sievingBound) > 0) {
//                    break;
//                } else {
//                    this.M = temp;
//                }
//            }
        } else { // Fixme: currently it assumes we'll never have more than 97 servers
            this.fixValue = BigInteger.valueOf(
                    MathUtility.arrayProduct(Arrays.copyOfRange(primeTable, 0, index + 1)));
            BigInteger currentPrime;
            for (Long i : Arrays.copyOfRange(primeTable, index, primeTable.length)) {
                currentPrime = BigInteger.valueOf(i);
                BigInteger temp = this.M.multiply(currentPrime);
                if (temp.compareTo(sievingBound) > 0) {
//                    isPrimeTableDrained = false;
                    break;
                } else {
                    this.M = temp;
                }
            }
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
