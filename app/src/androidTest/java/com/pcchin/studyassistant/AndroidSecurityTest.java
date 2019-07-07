package com.pcchin.studyassistant;

import com.pcchin.studyassistant.functions.SecurityFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/** Test hashing and encryption/decryption functions that require
 * external dependencies such as PBKDF2. **/
public class AndroidSecurityTest {
    private static final int TEST_COUNT = 10000;

    /** Check if the encryption function for the .subject files work. **/
    @Test
    public void testSubjectEncrypt() {
        // Normal input
        String responseString = AndroidTestFunctions.randomString(TEST_COUNT);
        byte[] responseArray = SecurityFunctions.subjectEncrypt(responseString,
                AndroidTestFunctions.randomString(TEST_COUNT), AndroidTestFunctions.randomArray(TEST_COUNT));
        Assert.assertNotNull(responseArray);

        // Minimal input
        responseArray = SecurityFunctions.subjectEncrypt(AndroidTestFunctions.randomString(2),
                AndroidTestFunctions.randomString(2), null);
        Assert.assertNotNull(responseArray);
    }

    /** Check if the encryption & decryption function for the .subject files work. **/
    @Test
    public void testSubjectEncryptDecrypt() {
        // Normal input
        String testTitle = AndroidTestFunctions.randomString(TEST_COUNT);
        String testPassword = AndroidTestFunctions.randomString(TEST_COUNT);
        ArrayList<ArrayList<String>> testContents = AndroidTestFunctions.randomArray(TEST_COUNT);
        byte[] testOutput = SecurityFunctions.subjectEncrypt(testTitle, testPassword, testContents);
        Assert.assertNotNull(testOutput);

        ArrayList<ArrayList<String>> testResponse = SecurityFunctions.subjectDecrypt(testTitle,
                testPassword, testOutput);
        Assert.assertEquals(testContents, testResponse);
    }

    /** Check if the PBKDF2 algorithm is working. **/
    @Test
    public void testPbkdf2() {
        byte[] responseByte = SecurityFunctions.pbkdf2(AndroidTestFunctions.randomString(TEST_COUNT),
                AndroidTestFunctions.randomString(TEST_COUNT).getBytes());
        Assert.assertNotNull(responseByte);

        // Minimal input
        responseByte = SecurityFunctions.pbkdf2(AndroidTestFunctions.randomString(2),
                AndroidTestFunctions.randomString(2).getBytes());
        Assert.assertNotNull(responseByte);
    }
}
