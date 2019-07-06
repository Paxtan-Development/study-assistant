package com.pcchin.studyassistant;

import com.pcchin.studyassistant.functions.SecurityFunctions;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.Cipher;

/** Test local hashing & encryption/decryption functions. **/
public class SecurityTest {
    private static final int TEST_COUNT = 10000;

    /** Check if the hashing function for locking notes works. **/
    @Test
    public void testNotesHash() {
        // Normal input
        String responseString = TestFunctions.randomString(TEST_COUNT);
        responseString = SecurityFunctions.notesHash(responseString);
        Assert.assertNotNull(responseString);

        // Minimal input
        responseString = TestFunctions.randomString(2);
        responseString = SecurityFunctions.notesHash(responseString);
        Assert.assertNotNull(responseString);
    }

    /** Check if the AES algorithm is working. **/
    @Test
    public void testAES() {
        byte[] original = TestFunctions.randomString(TEST_COUNT).getBytes();
        SecurityFunctions.aes(original, TestFunctions
                .randomString(TEST_COUNT).getBytes(), Cipher.ENCRYPT_MODE);
        Assert.assertNotNull(original);

        // Minimal input
        original = TestFunctions.randomString(2).getBytes();
        SecurityFunctions.aes(original, TestFunctions
            .randomString(2).getBytes(), Cipher.ENCRYPT_MODE);
        Assert.assertNotNull(original);
    }

    /** Check if the blowfish algorithm is working. **/
    @Test
    public void testBlowfish() {
        byte[] original = TestFunctions.randomString(TEST_COUNT).getBytes();
        SecurityFunctions.blowfish(original, TestFunctions
                .randomString(TEST_COUNT).getBytes(), Cipher.ENCRYPT_MODE);
        Assert.assertNotNull(original);

        // Minimal input
        original = TestFunctions.randomString(2).getBytes();
        SecurityFunctions.blowfish(original, TestFunctions
                .randomString(2).getBytes(), Cipher.ENCRYPT_MODE);
        Assert.assertNotNull(original);
    }
}

