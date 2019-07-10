package com.pcchin.studyassistant;

import com.pcchin.studyassistant.functions.SecurityFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import javax.crypto.Cipher;

/** Test local hashing & encryption/decryption functions. **/
public class SecurityTest {
    private static final int TEST_COUNT = 10000;

    /** Check if the encryption function for the .subject files work. **/
    @Test
    public void testSubjectEncrypt() {
        // Normal input
        String responseString = TestFunctions.randomString(TEST_COUNT);
        byte[] responseArray = SecurityFunctions.subjectEncrypt(responseString,
                TestFunctions.randomString(TEST_COUNT), TestFunctions.randomArray(TEST_COUNT));
        Assert.assertNotNull(responseArray);

        // Minimal input
        responseArray = SecurityFunctions.subjectEncrypt(TestFunctions.randomString(2),
                TestFunctions.randomString(2), null);
        Assert.assertNotNull(responseArray);
    }

    /** Check if the encryption & decryption function for the .subject files work. **/
    @Test
    public void testSubjectEncryptDecrypt() {
        // Normal input
        String testTitle = TestFunctions.randomString(TEST_COUNT);
        String testPassword = TestFunctions.randomString(TEST_COUNT);
        ArrayList<ArrayList<String>> testContents = TestFunctions.randomArray(TEST_COUNT);
        byte[] testOutput = SecurityFunctions.subjectEncrypt(testTitle, testPassword, testContents);
        Assert.assertNotNull(testOutput);

        ArrayList<ArrayList<String>> testResponse = SecurityFunctions.subjectDecrypt(testTitle,
                testPassword, testOutput);
        Assert.assertEquals(testContents, testResponse);
    }

    /** Check if the PBKDF2 algorithm is working. **/
    @Test
    public void testPbkdf2() {
        byte[] responseByte = SecurityFunctions.pbkdf2(TestFunctions.randomString(TEST_COUNT),
                TestFunctions.randomString(TEST_COUNT).getBytes());
        Assert.assertNotNull(responseByte);

        // Minimal input
        responseByte = SecurityFunctions.pbkdf2(TestFunctions.randomString(2),
                TestFunctions.randomString(2).getBytes());
        Assert.assertNotNull(responseByte);
    }

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

