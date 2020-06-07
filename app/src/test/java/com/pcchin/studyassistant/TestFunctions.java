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

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.functions.SecurityFunctions;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/** Functions used in tests **/
class TestFunctions {
    /** Generate a string with random characters.
     * @param count defines the max number of characters in the string. **/
    @NonNull
    static String randomString(int count) {
        return RandomStringUtils.random(new Random().nextInt(count));
    }

    /** Assert that all attributes in the ArrayList except for their noteId are equal.
     * The lastEdited and alertDate cannot be checked properly due to weirdness in the
     * date conversion functions. **/
    static void customNotesListAssert(@NonNull ArrayList<NotesContent> a, @NonNull ArrayList<NotesContent> b) {
        Assert.assertEquals("List Size", a.size(), b.size());
        for (int i = 0; i < a.size(); i++) {
            NotesContent c = a.get(i), d = b.get(i);
            Assert.assertEquals("noteTitle", c.noteTitle, d.noteTitle);
            Assert.assertEquals("noteContent", c.noteContent, d.noteContent);
            Assert.assertEquals("lockedSalt", c.lockedSalt, d.lockedSalt);
            Assert.assertEquals("lockedPass", c.lockedPass, d.lockedPass);
            if (c.alertDate == null) {
                Assert.assertNull("alertDateNull", d.alertDate);
            } else {
                Assert.assertNotNull("alertDateNotNull", d.alertDate);
            }
            Assert.assertEquals("alertCode", c.alertCode, d.alertCode);
        }
    }

    /** Generates a random ArrayList of NotesContent based on the given subjectId. **/
    @NonNull
    static ArrayList<NotesContent> generateRandomNotes(int testCount, int subjectId) {
        ArrayList<NotesContent> originalList = new ArrayList<>();
        int noteId;
        Date lastEdited = new Date(), alertDate = new Date();
        String title, contents, lockedSalt, lockedPass;
        Integer alertCode;
        Random rand = new Random();
        // Generate notes attributes
        for (int j = 0; j < rand.nextInt(testCount); j++) {
            noteId = rand.nextInt();
            title = TestFunctions.randomString(10000);
            contents = TestFunctions.randomString(1000000);
            lastEdited.setTime(rand.nextLong());
            lockedSalt = TestFunctions.randomString(50);
            lockedPass = rand.nextBoolean() ? "" : SecurityFunctions.passwordHash(TestFunctions.randomString(100), lockedSalt);
            alertDate.setTime(rand.nextLong());
            alertCode = rand.nextBoolean() ? null : rand.nextInt();
            originalList.add(new NotesContent(noteId, subjectId, title, contents, lastEdited, lockedSalt, lockedPass,
                    rand.nextBoolean() ? null : alertDate, alertCode));
        }
        return originalList;
    }

    /** Generates a random ArrayList of Integers based on the given bound. **/
    @NonNull
    static List<Integer> generateIdList(@NonNull Random rand, int bound) {
        List<Integer> idList = new ArrayList<>();
        int fakeId;
        // Populate notesIdList with mock notes
        for (int j = 0; j < rand.nextInt(bound); j++) {
            fakeId = rand.nextInt();
            while (idList.contains(fakeId)) fakeId = rand.nextInt();
            idList.add(fakeId);
        }
        return idList;
    }
}
