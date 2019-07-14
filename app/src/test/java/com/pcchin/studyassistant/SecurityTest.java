/*
 * Copyright 2019 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.studyassistant;

import com.pcchin.studyassistant.functions.SecurityFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import javax.crypto.Cipher;

/** Test local hashing & encryption/decryption functions. **/
public class SecurityTest {
    // TODO: PBKDF2
    private static final int TEST_COUNT = 10000;

    /** Check if the encryption function for the .subject files work. **/
    @Test
    public void testSubjectEncrypt() {
        // Normal input
        byte[] responseArray = SecurityFunctions.subjectEncrypt(
                TestFunctions.randomString(TEST_COUNT), TestFunctions.randomArray(TEST_COUNT));
        Assert.assertNotNull(responseArray);

        // Minimal input
        responseArray = SecurityFunctions.subjectEncrypt(
                TestFunctions.randomString(2), null);
        Assert.assertNotNull(responseArray);
    }

    /** Check if the encryption & decryption function for the .subject files work. **/
    @Test
    public void testSubjectEncryptDecrypt() {
        // Normal input
        String testPassword = TestFunctions.randomString(TEST_COUNT);
        ArrayList<ArrayList<String>> testContents = TestFunctions.randomArray(TEST_COUNT);
        byte[] testOutput = SecurityFunctions.subjectEncrypt(testPassword, testContents);
        Assert.assertNotNull(testOutput);

        ArrayList<ArrayList<String>> testResponse = SecurityFunctions.subjectDecrypt(testPassword,
                testOutput);
        Assert.assertEquals(testContents, testResponse);
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

