package mpc.project.util;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MathTest {
    @Test
    public void testGenRandBig() {
        int bitNum = 32;
        Random rnd = new Random();
        BigInteger n = MathUtility.genRandBig(bitNum, rnd);
        System.out.println(n);
    }

    @Test
    public void testGeneratePrimeTable() {
        Long[] primeNumberUnder100Long = {
                2L, 3L, 5L, 7L, 11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L, 43L, 47L, 53L, 59L, 61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L
        };

        BigInteger[] primeNumberUnder100BigInt = new BigInteger[primeNumberUnder100Long.length];
        for (int i = 0; i < primeNumberUnder100Long.length; i++) {
            primeNumberUnder100BigInt[i] = BigInteger.valueOf(primeNumberUnder100Long[i]);
        }

        BigInteger[] p2 = MathUtility.generatePrimeNumberTable(BigInteger.valueOf(100));
        assertArrayEquals(primeNumberUnder100BigInt, p2);

        System.out.println(Arrays.toString(MathUtility.generatePrimeNumberTable(500)));
    }

    @Test
    public void testArraySumToN() {
        BigInteger[] arr = MathUtility.generateRandomArraySumToN(10, BigInteger.valueOf(1000), new Random());
        System.out.println(Arrays.toString(arr));
        for (BigInteger bigInteger : arr) {
            assert bigInteger.signum() >= 0;
        }
        BigInteger sum = BigInteger.ZERO;
        for (BigInteger i : arr) {
            sum = sum.add(i);
        }
        assertEquals(sum, BigInteger.valueOf(1000));
    }

}
