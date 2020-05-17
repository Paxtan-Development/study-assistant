/*
 * Copyright 2020 PC Chin. All rights reserved.
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

/** Test local hashing & encryption/decryption functions. **/
public class SecurityTest {
    private static int TEST_COUNT;

    /** Default constructor. **/
    public SecurityTest() {
        if (BuildConfig.IS_LOCAL) {
            TEST_COUNT = 8000;
        } else {
            TEST_COUNT = 500;
        }
    }

    /** Check if the encryption function for the .subject files work. **/
    @Test
    public void testSubjectEncrypt() {
        // Normal input
        byte[] responseArray = SecurityFunctions.subjectEncrypt(
                TestFunctions.randomString(TEST_COUNT), TestFunctions.randomString(TEST_COUNT),
                TestFunctions.randomArray(TEST_COUNT));
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
        String testTitle = TestFunctions.randomString(TEST_COUNT),
                testPassword = TestFunctions.randomString(TEST_COUNT);
        ArrayList<ArrayList<String>> testContents = TestFunctions.randomArray(TEST_COUNT);
        byte[] testOutput = SecurityFunctions.subjectEncrypt(testTitle, testPassword, testContents);
        Assert.assertNotNull(testOutput);

        ArrayList<ArrayList<String>> testResponse = SecurityFunctions.subjectDecrypt(testTitle,
                testPassword, testOutput);
        Assert.assertEquals(testContents, testResponse);

        // Normal input
        testTitle = TestFunctions.randomString(2);
        testPassword = TestFunctions.randomString(2);
        testContents = TestFunctions.randomArray(2);
        testOutput = SecurityFunctions.subjectEncrypt(testTitle, testPassword, testContents);
        Assert.assertNotNull(testOutput);

        testResponse = SecurityFunctions.subjectDecrypt(testTitle,
                testPassword, testOutput);
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

    /** Check if the hashing function for protecting projects works. **/
    @Test
    public void testProjectHash() {
        // Normal input
        String responseString;
        responseString = SecurityFunctions.projectHash(TestFunctions.randomString(TEST_COUNT),
                TestFunctions.randomString(TEST_COUNT));
        Assert.assertNotNull(responseString);

        // Minimal input
        responseString = SecurityFunctions.projectHash(TestFunctions.randomString(TEST_COUNT),
                TestFunctions.randomString(TEST_COUNT));
        Assert.assertNotNull(responseString);
    }

    /** Check if the hashing function for project roles works. **/
    @Test
    public void testRoleHash() {
        // Normal input
        String responseString;
        responseString = SecurityFunctions.roleHash(TestFunctions.randomString(TEST_COUNT),
                TestFunctions.randomString(TEST_COUNT));
        Assert.assertNotNull(responseString);

        // Minimal input
        responseString = SecurityFunctions.roleHash(TestFunctions.randomString(2),
                TestFunctions.randomString(2));
        Assert.assertNotNull(responseString);
    }

    /** Check if the hashing function for project members works. **/
    @Test
    public void testMemberHash() {
        // Normal input
        String responseString;
        responseString = SecurityFunctions.memberHash(TestFunctions.randomString(TEST_COUNT),
                TestFunctions.randomString(TEST_COUNT), TestFunctions.randomString(TEST_COUNT));
        Assert.assertNotNull(responseString);

        // Minimal input
        responseString = SecurityFunctions.memberHash(TestFunctions.randomString(2),
                TestFunctions.randomString(2), TestFunctions.randomString(2));
        Assert.assertNotNull(responseString);
    }

    /** Check if the AES algorithm is working. **/
    @Test
    public void testAES() {
        byte[] original = TestFunctions.randomString(TEST_COUNT).getBytes();
        byte[] key = TestFunctions.randomString(TEST_COUNT).getBytes();
        byte[] iv = TestFunctions.randomString(TEST_COUNT).getBytes();
        byte[] response = SecurityFunctions.aes(original, key, iv, true);
        Assert.assertNotNull(original);
        Assert.assertNotNull(response);
        Assert.assertArrayEquals(original, SecurityFunctions.aes(response, key, iv, false));

        // Minimal input
        original = TestFunctions.randomString(2).getBytes();
        key = TestFunctions.randomString(2).getBytes();
        iv = TestFunctions.randomString(2).getBytes();
        response = SecurityFunctions.aes(original, key, iv, true);
        Assert.assertNotNull(original);
        Assert.assertNotNull(response);
        Assert.assertArrayEquals(original, SecurityFunctions.aes(response, key, iv, false));
    }

    /** Check if the blowfish algorithm is working. **/
    @Test
    public void testBlowfish() {
        byte[] original = TestFunctions.randomString(TEST_COUNT).getBytes();
        byte[] key = TestFunctions.randomString(TEST_COUNT).getBytes();
        byte[] response = SecurityFunctions.blowfish(original, key, true);
        Assert.assertNotNull(original);
        Assert.assertNotNull(response);
        Assert.assertArrayEquals(original, SecurityFunctions.blowfish(response, key, false));

        // Minimal input
        original = TestFunctions.randomString(2).getBytes();
        key = TestFunctions.randomString(2).getBytes();
        response = SecurityFunctions.blowfish(original, key, true);
        Assert.assertNotNull(original);
        Assert.assertNotNull(response);
        Assert.assertArrayEquals(original, SecurityFunctions.blowfish(response, key, false));
    }

    /** Check if the PBKDF2 algorithm is working. **/
    @Test
    public void testPbkdf2() {
        byte[] response = SecurityFunctions.pbkdf2(TestFunctions.randomString(TEST_COUNT).getBytes(),
                TestFunctions.randomString(TEST_COUNT).getBytes(), TEST_COUNT * 10);
        Assert.assertNotNull(response);

        response = SecurityFunctions.pbkdf2(TestFunctions.randomString(2).getBytes(),
                TestFunctions.randomString(2).getBytes(), TEST_COUNT);
        Assert.assertNotNull(response);
    }
}

