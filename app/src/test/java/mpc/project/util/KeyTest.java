package mpc.project.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

public class KeyTest extends TestCase {
    public void testToPKCS1PublicString() {
        Key key = new Key();
        key.setN(BigInteger.valueOf(57761551163L));
        try {
            System.out.println(key.toPKCS1PublicString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}