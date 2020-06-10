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

import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.functions.SecurityFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Test local hashing & encryption/decryption functions. **/
public class SecurityTest {
    private static int TEST_COUNT;

    /** Default constructor. **/
    public SecurityTest() {
        if (BuildConfig.IS_LOCAL) {
            TEST_COUNT = 8000;
        } else {
            TEST_COUNT = 400;
        }
    }

    /** Test if the encryption for subjects is working. **/
    @Test
    public void testSubjectEncrypt() {
        Random rand = new Random();
        int subjectId = rand.nextInt();
        String password = TestFunctions.randomString(1000);
        byte[] salt = new byte[32];
        rand.nextBytes(salt);
        List<NotesContent> originalList = TestFunctions.generateRandomNotes(TEST_COUNT, subjectId);
        byte[] encrypted = SecurityFunctions.subjectEncrypt(password, salt, originalList);
        Assert.assertNotNull(encrypted);
    }

    /** Test if the encryption and the decryption for subjects is working. **/
    @Test
    public void testSubjectEncryptDecrypt() {
        Random rand = new Random();
        int subjectId = rand.nextInt();
        String password = TestFunctions.randomString(1000);
        byte[] salt = new byte[32];
        rand.nextBytes(salt);
        ArrayList<NotesContent> originalList = TestFunctions.generateRandomNotes(TEST_COUNT, subjectId);
        List<Integer> notesIdList = TestFunctions.generateIdList(rand, 50);
        List<NotesContent> convertedList = SecurityFunctions.subjectDecrypt(notesIdList,
                new NotesSubject(subjectId, TestFunctions.randomString(10000), 1),
                salt, password, SecurityFunctions.subjectEncrypt(password, salt, originalList));
        TestFunctions.customNotesListAssert(originalList, (ArrayList<NotesContent>) convertedList);
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

