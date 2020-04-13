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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.NotesSubjectMigration;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

/** Check whether all functions used in the NotesSubject notes is correct. **/
public class NotesDatabaseTest {
    private static final int TEST_COUNT = 1000;
    private static final String TEST_DB_NAME = AndroidTestFunctions.randomString(TEST_COUNT);
    private final String testUser = AndroidTestFunctions.randomString(TEST_COUNT);
    private final String testContents = ConverterFunctions.doubleArrayToJson(AndroidTestFunctions.randomArray(TEST_COUNT));

    @Rule
    public final MigrationTestHelper testHelper;

    public NotesDatabaseTest() {
        testHelper = new MigrationTestHelper(
                        InstrumentationRegistry.getInstrumentation(),
                        SubjectDatabase.class.getCanonicalName(),
                        new FrameworkSQLiteOpenHelperFactory());
    }

    /** Check the data integrity between versions 1 to 2. **/
    @Test
    public void migration_1to2_data_integrity() throws NoSuchFieldException, IOException {
        SupportSQLiteDatabase db_v1 = testHelper.createDatabase(TEST_DB_NAME, 1);
        Random rand = new Random();
        for (int i = 0; i < rand.nextInt(TEST_COUNT); i++) {
            String testTitle = AndroidTestFunctions.randomString(TEST_COUNT);
            if (!Objects.equals(testTitle, testUser)) {
                db_v1.execSQL("INSERT INTO notesSubject (_title, contents) VALUES ("
                + AndroidTestFunctions.randomString(TEST_COUNT) + ", "
                + AndroidTestFunctions.randomString(TEST_COUNT) + ");");
            }
        }
        db_v1.execSQL("INSERT INTO notesSubject (_title, contents) VALUES ("
                + testUser + ", " + testContents + ");");
        for (int i = 0; i < rand.nextInt(TEST_COUNT); i++) {
            String testTitle = AndroidTestFunctions.randomString(TEST_COUNT);
            if (!Objects.equals(testTitle, testUser)) {
                db_v1.execSQL("INSERT INTO notesSubject (_title, contents) VALUES ("
                        + AndroidTestFunctions.randomString(TEST_COUNT) + ", "
                        + AndroidTestFunctions.randomString(TEST_COUNT) + ");");
            }
        }
        db_v1.close();

        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 2, true,
                NotesSubjectMigration.MIGRATION_1_2);

        SubjectDatabase afterDatabase = AndroidTestFunctions.getNotesDatabaseAftMigration(testHelper,
                TEST_DB_NAME, NotesSubjectMigration.MIGRATION_1_2);
        NotesSubject subject = afterDatabase.SubjectDao().search(testUser);
        if (subject != null) {
            Assert.assertEquals(subject.contents, ConverterFunctions.doubleJsonToArray(testContents));
            Assert.assertEquals(subject.sortOrder, TEST_COUNT);
        } else {
            throw new NoSuchFieldException("User not found in notes.");
        }
    }
}
